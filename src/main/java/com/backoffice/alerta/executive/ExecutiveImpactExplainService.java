package com.backoffice.alerta.executive;

import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.repository.ProjectRepository;

import com.backoffice.alerta.dto.BusinessImpactGraphResponse;
import com.backoffice.alerta.dto.BusinessImpactRequest;
import com.backoffice.alerta.dto.ImpactGraphNodeResponse;
import com.backoffice.alerta.rules.*;
import com.backoffice.alerta.service.BusinessImpactGraphService;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para geração de explicação executiva de impacto sistêmico
 * 
 * US#38 - Explicação Executiva Inteligente
 * 
 * IMPORTANTE - Regras de Governança:
 * ❌ NÃO recalcular risco
 * ❌ NÃO persistir dados
 * ❌ NÃO criar auditoria
 * ❌ NÃO criar SLA
 * ❌ NÃO enviar notificações
 * ❌ NÃO modificar entidades
 * 
 * ✅ Read-only
 * ✅ Determinístico
 * ✅ Consultivo
 */
@Service
public class ExecutiveImpactExplainService {
    
    private static final Logger log = LoggerFactory.getLogger(ExecutiveImpactExplainService.class);
    
    private final BusinessImpactGraphService graphService;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final ProjectRepository projectRepository;
    
    public ExecutiveImpactExplainService(BusinessImpactGraphService graphService,
                                        BusinessRuleIncidentRepository incidentRepository,
                                        ProjectRepository projectRepository) {
        this.graphService = graphService;
        this.incidentRepository = incidentRepository;
        this.projectRepository = projectRepository;
    }
    
    /**
     * Gera explicação executiva do impacto sistêmico
     * 
     * Interpreta dados existentes sem alterar estado do sistema.
     */
    public ExecutiveImpactExplainResponse generateExplanation(ExecutiveImpactExplainRequest request) {
        log.info("Gerando explicação executiva para PR {} (ambiente: {}, tipo: {}, foco: {})",
                request.getPullRequestId(), request.getEnvironment(), 
                request.getChangeType(), request.getFocus());
        
        // US#50: Escopo de projeto (opcional)
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Projeto não encontrado: " + request.getProjectId()));
            log.info("🔎 Análise escopada para Projeto: {} ({})", 
                    project.getName(), request.getProjectId());
        } else {
            log.info("🌐 Análise GLOBAL (sem escopo de projeto)");
        }
        
        try {
            // Obter grafo de impacto (reutiliza US#37)
            BusinessImpactRequest graphRequest = new BusinessImpactRequest(
                    request.getPullRequestId(), 
                    Collections.emptyList() // Usar mapeamentos existentes
            );
            graphRequest.setProjectId(request.getProjectId());
            BusinessImpactGraphResponse graph = graphService.generateImpactGraph(graphRequest);
            
            // Calcular nível de risco geral
            RiskLevel overallRisk = calculateOverallRisk(graph, request.getEnvironment());
            
            // Calcular nível de confiança
            ConfidenceLevel confidence = calculateConfidence(graph);
            
            // Gerar sumário executivo
            ExecutiveSummary summary = buildExecutiveSummary(graph, request, overallRisk);
            
            log.info("Explicação executiva gerada - Risco: {}, Confiança: {}, Regras: {}",
                    overallRisk, confidence, graph.getNodes().size());
            
            return new ExecutiveImpactExplainResponse(
                    request.getPullRequestId(),
                    overallRisk,
                    summary,
                    confidence,
                    Instant.now(),
                    project != null 
                        ? ProjectContext.scoped(project.getId(), project.getName())
                        : ProjectContext.global()
            );
        } catch (Exception e) {
            log.error("Erro ao gerar explicação executiva para PR {}: {}", 
                    request.getPullRequestId(), e.getMessage(), e);
            
            // Retornar resposta resiliente mesmo em caso de erro
            return createFallbackResponse(request, project);
        }
    }
    
    /**
     * Cria resposta de fallback em caso de erro
     */
    private ExecutiveImpactExplainResponse createFallbackResponse(ExecutiveImpactExplainRequest request, Project project) {
        ExecutiveSummary fallbackSummary = new ExecutiveSummary(
                "Análise temporariamente indisponível para " + request.getPullRequestId(),
                "Não foi possível analisar o impacto completo desta mudança no momento. " +
                "Recomenda-se revisão manual por um engenheiro sênior.",
                Collections.emptyList(),
                "Análise de histórico não disponível.",
                "Não foi possível calcular o nível de risco automaticamente. " +
                "Proceda com cautela adicional e realize revisão manual.",
                "Aguarde alguns minutos e tente novamente. Se o problema persistir, " +
                "contate o time de Platform Engineering."
        );
        
        return new ExecutiveImpactExplainResponse(
                request.getPullRequestId(),
                RiskLevel.MEDIO, // Assumir MEDIO em caso de falha (princípio da precaução)
                fallbackSummary,
                ConfidenceLevel.LOW,
                Instant.now(),
                project != null 
                    ? ProjectContext.scoped(project.getId(), project.getName())
                    : ProjectContext.global()
        );
    }
    
    /**
     * Calcula nível de risco geral baseado no grafo e ambiente
     */
    private RiskLevel calculateOverallRisk(BusinessImpactGraphResponse graph, Environment environment) {
        List<ImpactGraphNodeResponse> nodes = graph.getNodes();
        
        if (nodes == null || nodes.isEmpty()) {
            return RiskLevel.BAIXO;
        }
        
        // Verifica presença de regras críticas
        boolean hasCritical = nodes.stream()
                .anyMatch(n -> n.getCriticality() == Criticality.CRITICA);
        
        boolean hasHigh = nodes.stream()
                .anyMatch(n -> n.getCriticality() == Criticality.ALTA);
        
        boolean hasCascade = nodes.stream()
                .anyMatch(n -> n.getImpactLevel() == ImpactGraphNodeResponse.ImpactLevel.CASCADE);
        
        // Lógica de risco baseada em criticidade e ambiente
        if (hasCritical && environment == Environment.PRODUCTION) {
            return RiskLevel.CRITICO;
        }
        
        if (hasCritical || (hasHigh && environment == Environment.PRODUCTION)) {
            return RiskLevel.ALTO;
        }
        
        if (hasCascade || hasHigh || environment == Environment.PRODUCTION) {
            return RiskLevel.MEDIO;
        }
        
        return RiskLevel.BAIXO;
    }
    
    /**
     * Calcula nível de confiança da análise
     */
    private ConfidenceLevel calculateConfidence(BusinessImpactGraphResponse graph) {
        if (graph == null || graph.getNodes() == null) {
            return ConfidenceLevel.LOW;
        }
        
        List<ImpactGraphNodeResponse> nodes = graph.getNodes();
        
        if (nodes.isEmpty()) {
            return ConfidenceLevel.LOW;
        }
        
        // Confiança baseada na quantidade de dados disponíveis
        long nodesWithIncidents = nodes.stream()
                .filter(ImpactGraphNodeResponse::isHasIncidents)
                .count();
        
        long nodesWithOwnership = nodes.stream()
                .filter(n -> n.getOwnerships() != null && !n.getOwnerships().isEmpty())
                .count();
        
        double dataCompleteness = (double) (nodesWithIncidents + nodesWithOwnership) / (nodes.size() * 2);
        
        if (dataCompleteness >= 0.7) {
            return ConfidenceLevel.HIGH;
        } else if (dataCompleteness >= 0.4) {
            return ConfidenceLevel.MEDIUM;
        } else {
            return ConfidenceLevel.LOW;
        }
    }
    
    /**
     * Constrói sumário executivo
     */
    private ExecutiveSummary buildExecutiveSummary(BusinessImpactGraphResponse graph, 
                                                   ExecutiveImpactExplainRequest request,
                                                   RiskLevel overallRisk) {
        List<ImpactGraphNodeResponse> nodes = graph.getNodes();
        
        if (nodes == null || nodes.isEmpty()) {
            return new ExecutiveSummary(
                    "Mudança sem regras de negócio mapeadas",
                    "Esta mudança não possui regras de negócio identificadas no sistema. " +
                    "Pode ser uma mudança técnica pura ou em áreas não mapeadas.",
                    Collections.emptyList(),
                    "Sem histórico de incidentes para análise.",
                    "Risco BAIXO identificado. Mudança em áreas não mapeadas requer validação manual.",
                    "Verificar se a mudança realmente não impacta regras de negócio ou se há gaps no mapeamento."
            );
        }
        
        String headline = buildHeadline(nodes, request.getEnvironment());
        String businessImpact = buildBusinessImpact(nodes, request.getChangeType());
        List<String> areasAffected = buildAreasAffected(nodes);
        String historicalContext = buildHistoricalContext(nodes);
        String riskInterpretation = buildRiskInterpretation(overallRisk, request.getEnvironment(), nodes);
        String recommendation = buildRecommendation(overallRisk, request.getEnvironment(), nodes, request.getChangeType());
        
        return new ExecutiveSummary(
                headline,
                businessImpact,
                areasAffected,
                historicalContext,
                riskInterpretation,
                recommendation
        );
    }
    
    private String buildHeadline(List<ImpactGraphNodeResponse> nodes, Environment environment) {
        long criticalCount = nodes.stream()
                .filter(n -> n.getCriticality() == Criticality.CRITICA)
                .count();
        
        boolean hasCascade = nodes.stream()
                .anyMatch(n -> n.getImpactLevel() == ImpactGraphNodeResponse.ImpactLevel.CASCADE);
        
        String envText = environment.name();
        String cascadeText = hasCascade ? "impacto em cascata detectado" : "";
        String criticalText = criticalCount > 0 ? ", incluindo regra crítica" : "";
        
        if (hasCascade) {
            return String.format("Mudança em %s: %s (%d regras afetadas)%s",
                    envText, cascadeText, nodes.size(), criticalText);
        } else {
            return String.format("Mudança em %s: %d regras impactadas%s",
                    envText, nodes.size(), criticalText);
        }
    }
    
    private String buildBusinessImpact(List<ImpactGraphNodeResponse> nodes, ChangeType changeType) {
        Set<Domain> domains = nodes.stream()
                .map(ImpactGraphNodeResponse::getDomain)
                .collect(Collectors.toSet());
        
        String domainText = domains.stream()
                .map(this::translateDomain)
                .collect(Collectors.joining(", "));
        
        String changeTypeContext = switch (changeType) {
            case FEATURE -> "Nova funcionalidade pode introduzir comportamentos não previstos em fluxos existentes.";
            case HOTFIX -> "Hotfix requer atenção especial devido à urgência e potencial para efeitos colaterais.";
            case REFACTOR -> "Refatoração pode afetar estabilidade de componentes dependentes.";
            case CONFIG -> "Mudança de configuração pode afetar comportamento do sistema.";
        };
        
        return String.format("Esta mudança afeta diretamente %s. %s", domainText, changeTypeContext);
    }
    
    private String translateDomain(Domain domain) {
        return switch (domain) {
            case PAYMENT -> "processamento de pagamentos";
            case BILLING -> "faturamento e cobrança";
            case USER -> "gestão de usuários";
            case ORDER -> "processamento de pedidos";
            case GENERIC -> "operações gerais";
        };
    }
    
    private List<String> buildAreasAffected(List<ImpactGraphNodeResponse> nodes) {
        Set<String> areas = new HashSet<>();
        
        for (ImpactGraphNodeResponse node : nodes) {
            areas.add(node.getDomain().name());
            
            if (node.getOwnerships() != null) {
                node.getOwnerships().forEach(o -> {
                    areas.add(o.getTeamName());
                });
            }
        }
        
        return new ArrayList<>(areas);
    }
    
    private String buildHistoricalContext(List<ImpactGraphNodeResponse> nodes) {
        long rulesWithIncidents = nodes.stream()
                .filter(ImpactGraphNodeResponse::isHasIncidents)
                .count();
        
        if (rulesWithIncidents == 0) {
            return "Sem histórico relevante de incidentes nas regras impactadas. " +
                   "Isso indica estabilidade histórica, mas não elimina riscos de novas mudanças.";
        } else {
            return String.format("Histórico mostra %d regras com incidentes prévios. " +
                   "Recomenda-se cautela extra e monitoramento intensivo pós-deploy.", rulesWithIncidents);
        }
    }
    
    private String buildRiskInterpretation(RiskLevel risk, Environment environment, List<ImpactGraphNodeResponse> nodes) {
        String baseInterpretation = switch (risk) {
            case CRITICO -> "IMPACTO CRÍTICO: Risco financeiro alto devido a possível indisponibilidade de sistemas essenciais. " +
                           "Risco operacional severo com potencial paralisação de processos core. " +
                           "Risco reputacional significativo em caso de falha visível aos clientes.";
            case ALTO -> "IMPACTO ALTO: Risco financeiro moderado a alto. " +
                        "Risco operacional considerável com possível degradação de serviços. " +
                        "Risco reputacional moderado dependendo da visibilidade da falha.";
            case MEDIO -> "IMPACTO MÉDIO: Risco financeiro limitado a cenários específicos. " +
                         "Risco operacional gerenciável com monitoramento adequado. " +
                         "Risco reputacional baixo, impacto provavelmente interno.";
            case BAIXO -> "IMPACTO BAIXO: Risco financeiro mínimo. " +
                         "Risco operacional negligível. " +
                         "Risco reputacional irrelevante.";
        };
        
        String environmentWarning = environment == Environment.PRODUCTION ?
                " ATENÇÃO: Ambiente de produção amplifica todos os riscos." : "";
        
        return String.format("Risco %s identificado. %s%s", risk.name(), baseInterpretation, environmentWarning);
    }
    
    private String buildRecommendation(RiskLevel risk, Environment environment, 
                                      List<ImpactGraphNodeResponse> nodes, ChangeType changeType) {
        List<String> recommendations = new ArrayList<>();
        
        // Recomendações baseadas em risco
        switch (risk) {
            case CRITICO -> {
                recommendations.add("Realizar revisão cruzada com pelo menos 2 senior engineers de times diferentes");
                recommendations.add("Agendar deploy fora do horário comercial com equipe de plantão disponível");
                recommendations.add("Implementar feature flag para rollback instantâneo sem redeploy");
                recommendations.add("Executar testes de carga e stress antes do deploy");
                recommendations.add("Monitoramento em tempo real por no mínimo 4 horas pós-deploy");
                recommendations.add("Plano de comunicação preparado para stakeholders e clientes");
            }
            case ALTO -> {
                recommendations.add("Aprovação de 2 senior engineers necessária");
                recommendations.add("Deploy em horário controlado com time disponível");
                recommendations.add("Testes de integração completos em ambiente de staging");
                recommendations.add("Monitoramento intensivo por 2 horas pós-deploy");
                recommendations.add("Rollback plan documentado e testado");
            }
            case MEDIO -> {
                recommendations.add("Aprovação de pelo menos 1 senior engineer necessária");
                recommendations.add("Testes de integração nos fluxos impactados");
                recommendations.add("Monitoramento padrão por 1 hora pós-deploy");
                recommendations.add("Documentação de mudanças atualizada");
            }
            case BAIXO -> {
                recommendations.add("Revisão de código padrão suficiente");
                recommendations.add("Testes unitários e de integração básicos");
                recommendations.add("Deploy pode seguir processo normal");
            }
        }
        
        // Recomendações específicas por tipo de mudança
        if (changeType == ChangeType.HOTFIX) {
            recommendations.add("HOTFIX: Garantir que a correção não introduz novos problemas");
            recommendations.add("Considerar backport para branches de manutenção se aplicável");
        }
        
        // Recomendações baseadas em características do grafo
        boolean hasCascade = nodes.stream()
                .anyMatch(n -> n.getImpactLevel() == ImpactGraphNodeResponse.ImpactLevel.CASCADE);
        
        if (hasCascade) {
            recommendations.add("Impacto em cascata detectado: validar comportamento de todas as regras dependentes");
        }
        
        long rulesWithIncidents = nodes.stream()
                .filter(ImpactGraphNodeResponse::isHasIncidents)
                .count();
        
        if (rulesWithIncidents > 0) {
            recommendations.add("Regras com histórico de incidentes: revisar root causes anteriores antes do deploy");
        }
        
        return String.join("; ", recommendations) + ".";
    }
}
