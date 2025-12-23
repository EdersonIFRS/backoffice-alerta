package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.BusinessImpactResponse;
import com.backoffice.alerta.dto.RiskDecisionRequest;
import com.backoffice.alerta.dto.RiskDecisionResponse;
import com.backoffice.alerta.dto.AIAdvisoryResponse;
import com.backoffice.alerta.rules.*;
import com.backoffice.alerta.repository.RiskDecisionAuditRepository;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço responsável por criar e gerenciar registros de auditoria
 * Integra dados de múltiplos serviços para criar trilha completa
 */
@Service
public class RiskDecisionAuditService {

    private static final Logger logger = LoggerFactory.getLogger(RiskDecisionAuditService.class);
    private static final String POLICY_VERSION = "RiskPolicy v2.1";

    private final RiskDecisionAuditRepository auditRepository;
    private final BusinessRuleIncidentRepository incidentRepository;

    public RiskDecisionAuditService(RiskDecisionAuditRepository auditRepository,
                                   BusinessRuleIncidentRepository incidentRepository) {
        this.auditRepository = auditRepository;
        this.incidentRepository = incidentRepository;
    }

    /**
     * Cria registro de auditoria a partir de uma decisão de risco
     * @param decisionRequest Requisição original da decisão
     * @param decisionResponse Resposta do motor de decisão
     * @param impactResponse Análise de impacto de negócio
     * @return Registro de auditoria criado
     */
    public RiskDecisionAudit createAudit(RiskDecisionRequest decisionRequest,
                                        RiskDecisionResponse decisionResponse,
                                        BusinessImpactResponse impactResponse) {
        logger.info("Criando registro de auditoria para PR: {}", decisionResponse.getPullRequestId());

        // Extrai nomes das regras impactadas
        List<String> impactedRuleNames = extractImpactedRuleNames(impactResponse);

        // Constrói sumário de incidentes
        Map<IncidentSeverity, Integer> incidentSummary = buildIncidentSummary(impactResponse);

        // Calcula riskScore baseado no RiskLevel
        Integer riskScore = calculateRiskScore(impactResponse.getOverallBusinessRisk(), incidentSummary);

        // Cria registro de auditoria
        RiskDecisionAudit audit = new RiskDecisionAudit(
            decisionResponse.getPullRequestId(),
            decisionRequest.getEnvironment(),
            decisionResponse.getRiskLevel(),
            riskScore,
            decisionResponse.getFinalDecision(),
            impactedRuleNames,
            incidentSummary,
            decisionResponse.getRequiredActions(),
            false, // IA não consultada neste momento
            null,  // Sem análise de IA ainda
            POLICY_VERSION
        );

        // Persiste
        RiskDecisionAudit saved = auditRepository.save(audit);
        logger.info("Auditoria criada com sucesso: ID={}", saved.getId());

        return saved;
    }

    /**
     * Atualiza registro de auditoria com análise de IA
     * Como a entidade é imutável, cria novo registro com dados da IA
     * @param originalAudit Auditoria original
     * @param aiResponse Resposta da análise de IA
     * @return Novo registro de auditoria com dados da IA
     */
    public RiskDecisionAudit enrichWithAI(RiskDecisionAudit originalAudit,
                                         AIAdvisoryResponse aiResponse) {
        logger.info("Enriquecendo auditoria com análise de IA para PR: {}", 
                   originalAudit.getPullRequestId());

        // Cria sumário da IA
        String aiSummary = buildAISummary(aiResponse);

        // Cria novo registro com dados da IA
        RiskDecisionAudit enrichedAudit = new RiskDecisionAudit(
            originalAudit.getPullRequestId(),
            originalAudit.getEnvironment(),
            originalAudit.getRiskLevel(),
            originalAudit.getRiskScore(),
            originalAudit.getFinalDecision(),
            originalAudit.getImpactedBusinessRules(),
            originalAudit.getIncidentSummary(),
            originalAudit.getRestrictions(),
            true, // IA consultada
            aiSummary,
            originalAudit.getPolicySnapshot()
        );

        return auditRepository.save(enrichedAudit);
    }

    /**
     * Busca todos os registros de auditoria
     * @return Lista de todos os registros
     */
    public List<RiskDecisionAudit> findAll() {
        return auditRepository.findAll();
    }

    /**
     * Busca registros de auditoria por Pull Request
     * @param pullRequestId ID do Pull Request
     * @return Lista de registros para o PR
     */
    public List<RiskDecisionAudit> findByPullRequestId(String pullRequestId) {
        return auditRepository.findByPullRequestIdOrderByCreatedAtDesc(pullRequestId);
    }

    /**
     * Busca registros de auditoria por decisão final
     * @param decision Decisão final
     * @return Lista de registros com a decisão
     */
    public List<RiskDecisionAudit> findByFinalDecision(FinalDecision decision) {
        return auditRepository.findByFinalDecisionOrderByCreatedAtDesc(decision);
    }

    /**
     * Extrai nomes das regras de negócio impactadas
     */
    private List<String> extractImpactedRuleNames(BusinessImpactResponse impactResponse) {
        if (impactResponse.getImpactedBusinessRules() == null) {
            return List.of();
        }

        return impactResponse.getImpactedBusinessRules().stream()
            .map(rule -> rule.getName())
            .collect(Collectors.toList());
    }

    /**
     * Constrói sumário de incidentes por severidade
     */
    private Map<IncidentSeverity, Integer> buildIncidentSummary(BusinessImpactResponse impactResponse) {
        Map<IncidentSeverity, Integer> summary = new HashMap<>();

        if (impactResponse.getImpactedBusinessRules() == null) {
            return summary;
        }

        // Para cada regra impactada, conta incidentes por severidade
        impactResponse.getImpactedBusinessRules().forEach(rule -> {
            String ruleId = rule.getBusinessRuleId();
            UUID ruleUuid = UUID.fromString(ruleId);
            List<BusinessRuleIncident> incidents = incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(ruleUuid);

            incidents.forEach(incident -> {
                IncidentSeverity severity = incident.getSeverity();
                summary.merge(severity, 1, Integer::sum);
            });
        });

        return summary;
    }

    /**
     * Constrói sumário textual da análise de IA
     */
    private String buildAISummary(AIAdvisoryResponse aiResponse) {
        if (aiResponse == null) {
            return null;
        }

        // Concatena insights principais da IA
        StringBuilder summary = new StringBuilder();

        if (aiResponse.getExecutiveInsight() != null && !aiResponse.getExecutiveInsight().isBlank()) {
            summary.append(aiResponse.getExecutiveInsight().trim());
        }

        if (aiResponse.getHistoricalPatternAlert() != null && !aiResponse.getHistoricalPatternAlert().isBlank()) {
            if (summary.length() > 0) {
                summary.append(" ");
            }
            summary.append(aiResponse.getHistoricalPatternAlert().trim());
        }

        return summary.length() > 0 ? summary.toString() : null;
    }

    /**
     * Calcula pontuação numérica de risco baseado em RiskLevel e incidentes
     */
    private Integer calculateRiskScore(RiskLevel riskLevel, Map<IncidentSeverity, Integer> incidentSummary) {
        // Pontuação base por RiskLevel
        int baseScore = switch (riskLevel) {
            case BAIXO -> 25;
            case MEDIO -> 50;
            case ALTO -> 75;
            case CRITICO -> 90;
        };

        // Adiciona pontos por incidentes
        int incidentPenalty = 0;
        for (Map.Entry<IncidentSeverity, Integer> entry : incidentSummary.entrySet()) {
            incidentPenalty += entry.getKey().getRiskWeight() * entry.getValue();
        }

        // Score final (máximo 100)
        return Math.min(100, baseScore + incidentPenalty);
    }
}

