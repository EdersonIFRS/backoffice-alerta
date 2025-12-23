package com.backoffice.alerta.service;

import com.backoffice.alerta.ai.LLMClient;
import com.backoffice.alerta.dto.*;
import com.backoffice.alerta.rules.*;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import com.backoffice.alerta.repository.RiskDecisionAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço responsável por orquestrar a camada consultiva de IA
 * NÃO modifica decisões, riscos ou impactos - apenas gera insights
 */
@Service
public class AIAdvisoryService {

    private static final Logger logger = LoggerFactory.getLogger(AIAdvisoryService.class);

    private final LLMClient llmClient;
    private final BusinessRuleRepository businessRuleRepository;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final RiskDecisionAuditService auditService;
    private final RiskDecisionAuditRepository auditRepository;

    public AIAdvisoryService(LLMClient llmClient,
                            BusinessRuleRepository businessRuleRepository,
                            BusinessRuleIncidentRepository incidentRepository,
                            RiskDecisionAuditService auditService,
                            RiskDecisionAuditRepository auditRepository) {
        this.llmClient = llmClient;
        this.businessRuleRepository = businessRuleRepository;
        this.incidentRepository = incidentRepository;
        this.auditService = auditService;
        this.auditRepository = auditRepository;
    }

    /**
     * Gera análise consultiva baseada em dados já processados
     * @param request Contexto da decisão já tomada
     * @return Análise consultiva estruturada
     */
    public AIAdvisoryResponse generateAdvisory(AIAdvisoryRequest request) {
        logger.info("Gerando análise consultiva para PR: {}", request.getPullRequestId());

        // Valida entrada
        validateRequest(request);

        // Enriquece dados das regras impactadas
        List<ImpactedBusinessRuleSummary> enrichedRules = enrichBusinessRules(request.getImpactedBusinessRules());

        // Constrói contexto para a IA
        Map<String, Object> context = buildContext(request, enrichedRules);

        try {
            // Gera análise consultiva via IA
            String aiGeneratedText = llmClient.generateAdvisory(context);

            // Parseia resposta estruturada
            AIAdvisoryResponse response = parseAIResponse(aiGeneratedText);

            // Enriquece auditoria existente com dados da IA (US #20)
            enrichAuditWithAI(request.getPullRequestId(), response);

            return response;

        } catch (Exception e) {
            logger.error("Erro ao gerar análise consultiva de IA: {}", e.getMessage(), e);
            // Retorna resposta de fallback em caso de falha
            return generateFallbackResponse(request);
        }
    }

    /**
     * Valida dados de entrada
     */
    private void validateRequest(AIAdvisoryRequest request) {
        if (request.getPullRequestId() == null || request.getPullRequestId().isBlank()) {
            throw new IllegalArgumentException("Pull Request ID é obrigatório");
        }
        if (request.getRiskLevel() == null) {
            throw new IllegalArgumentException("Nível de risco é obrigatório");
        }
        if (request.getFinalDecision() == null) {
            throw new IllegalArgumentException("Decisão final é obrigatória");
        }
    }

    /**
     * Enriquece dados das regras de negócio com informações adicionais
     */
    private List<ImpactedBusinessRuleSummary> enrichBusinessRules(List<ImpactedBusinessRuleSummary> rules) {
        return rules.stream()
            .map(this::enrichSingleRule)
            .collect(Collectors.toList());
    }

    private ImpactedBusinessRuleSummary enrichSingleRule(ImpactedBusinessRuleSummary summary) {
        // Busca informações adicionais da regra
        Optional<BusinessRule> ruleOpt = businessRuleRepository.findById(summary.getRuleId());
        if (ruleOpt.isEmpty()) {
            logger.warn("Regra de negócio não encontrada: {}", summary.getRuleId());
            return summary;
        }
        
        BusinessRule rule = ruleOpt.get();

        // Busca histórico de incidentes
        UUID ruleUuid = UUID.fromString(summary.getRuleId());
        List<BusinessRuleIncident> incidents = incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(ruleUuid);
        
        // Cria nova instância enriquecida
        return new ImpactedBusinessRuleSummary(
            rule.getId(),
            rule.getName(),
            rule.getCriticality(),
            summary.getImpactType(),
            incidents.size()
        );
    }

    /**
     * Constrói mapa de contexto para alimentar a IA
     */
    private Map<String, Object> buildContext(AIAdvisoryRequest request, 
                                            List<ImpactedBusinessRuleSummary> enrichedRules) {
        Map<String, Object> context = new HashMap<>();
        
        context.put("pullRequestId", request.getPullRequestId());
        context.put("environment", request.getEnvironment() != null ? request.getEnvironment().name() : "DESCONHECIDO");
        context.put("changeType", request.getChangeType() != null ? request.getChangeType().name() : "DESCONHECIDO");
        context.put("riskLevel", request.getRiskLevel().name());
        context.put("finalDecision", request.getFinalDecision().name());
        
        // Converte regras para formato mais simples
        List<Map<String, Object>> rulesData = enrichedRules.stream()
            .map(r -> {
                Map<String, Object> ruleMap = new HashMap<>();
                ruleMap.put("ruleId", r.getRuleId());
                ruleMap.put("name", r.getName());
                ruleMap.put("criticality", r.getCriticality());
                ruleMap.put("impactType", r.getImpactType());
                ruleMap.put("incidentCount", r.getIncidentCount());
                return ruleMap;
            })
            .collect(Collectors.toList());
        
        context.put("impactedBusinessRules", rulesData);
        context.put("mandatoryActions", request.getMandatoryActions() != null ? request.getMandatoryActions() : List.of());
        
        return context;
    }

    /**
     * Faz parsing da resposta da IA em estrutura definida
     */
    private AIAdvisoryResponse parseAIResponse(String aiText) {
        AIAdvisoryResponse response = new AIAdvisoryResponse();
        
        // Extrai seções do texto gerado
        response.setExecutiveInsight(extractSection(aiText, "VISÃO EXECUTIVA:", "INTERPRETAÇÃO"));
        response.setRiskInterpretation(extractSection(aiText, "INTERPRETAÇÃO DE RISCO:", "PADRÃO"));
        response.setHistoricalPatternAlert(extractSection(aiText, "PADRÃO HISTÓRICO:", "RECOMENDAÇÕES"));
        
        // Extrai lista de recomendações
        String recsSection = extractSection(aiText, "RECOMENDAÇÕES PREVENTIVAS:", "NÍVEL");
        List<String> recommendations = parseRecommendations(recsSection);
        response.setPreventiveRecommendations(recommendations);
        
        // Extrai confiança
        String confidence = extractSection(aiText, "NÍVEL DE CONFIANÇA:", "\n\n");
        response.setConfidenceLevel(confidence != null ? confidence.trim() : "Média");
        
        return response;
    }

    /**
     * Extrai seção específica do texto
     */
    private String extractSection(String text, String startMarker, String endMarker) {
        try {
            int start = text.indexOf(startMarker);
            if (start == -1) return "";
            
            start += startMarker.length();
            int end = text.indexOf(endMarker, start);
            if (end == -1) end = text.length();
            
            return text.substring(start, end).trim();
        } catch (Exception e) {
            logger.warn("Erro ao extrair seção '{}': {}", startMarker, e.getMessage());
            return "";
        }
    }

    /**
     * Parseia lista de recomendações do texto
     */
    private List<String> parseRecommendations(String recsText) {
        if (recsText == null || recsText.isBlank()) {
            return List.of();
        }
        
        return recsText.lines()
            .map(String::trim)
            .filter(line -> line.startsWith("-"))
            .map(line -> line.substring(1).trim())
            .filter(line -> !line.isEmpty())
            .collect(Collectors.toList());
    }

    /**
     * Gera resposta de fallback se a IA falhar
     */
    private AIAdvisoryResponse generateFallbackResponse(AIAdvisoryRequest request) {
        logger.warn("Gerando resposta de fallback para PR: {}", request.getPullRequestId());
        
        AIAdvisoryResponse response = new AIAdvisoryResponse();
        
        response.setExecutiveInsight(
            String.format("Esta mudança foi classificada como %s e recebeu decisão %s. " +
                        "A análise consultiva de IA não está disponível no momento, " +
                        "mas a decisão foi tomada baseada em regras e histórico.",
                        request.getRiskLevel(), request.getFinalDecision())
        );
        
        response.setRiskInterpretation(
            "O sistema avaliou automaticamente esta mudança usando critérios técnicos e histórico de incidentes."
        );
        
        response.setHistoricalPatternAlert(
            request.getImpactedBusinessRules() != null && !request.getImpactedBusinessRules().isEmpty()
                ? "Esta mudança impacta regras de negócio catalogadas. Revise o histórico."
                : "Nenhum impacto direto em regras de negócio catalogadas."
        );
        
        response.setPreventiveRecommendations(List.of(
            "Siga todas as ações obrigatórias definidas pelo sistema",
            "Mantenha comunicação com stakeholders",
            "Execute testes adequados ao nível de risco",
            "Tenha plano de rollback preparado"
        ));
        
        response.setConfidenceLevel("Baixa (sistema de fallback)");
        
        return response;
    }

    /**
     * Enriquece auditoria existente com análise de IA
     */
    private void enrichAuditWithAI(String pullRequestId, AIAdvisoryResponse aiResponse) {
        try {
            // Busca última auditoria para este PR
            List<RiskDecisionAudit> audits = auditRepository.findByPullRequestIdOrderByCreatedAtDesc(pullRequestId);
            
            if (audits.isEmpty()) {
                logger.warn("Nenhuma auditoria encontrada para PR: {}", pullRequestId);
                return;
            }

            // Pega a mais recente
            RiskDecisionAudit latestAudit = audits.get(0);

            // Se já foi consultada IA, não criar duplicata
            if (Boolean.TRUE.equals(latestAudit.getAiConsulted())) {
                logger.debug("IA já consultada para este PR, ignorando");
                return;
            }

            // Cria novo registro enriquecido com IA
            auditService.enrichWithAI(latestAudit, aiResponse);
            
        } catch (Exception e) {
            logger.error("Erro ao enriquecer auditoria com IA: {}", e.getMessage(), e);
            // Não propaga erro - IA é consultiva
        }
    }
}

