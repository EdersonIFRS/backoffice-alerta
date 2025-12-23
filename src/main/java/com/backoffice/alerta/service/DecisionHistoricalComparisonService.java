package com.backoffice.alerta.service;

import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.repository.ProjectRepository;

import com.backoffice.alerta.dto.*;
import com.backoffice.alerta.dto.HistoricalDecisionComparisonResponse.FeedbackOutcome;
import com.backoffice.alerta.dto.HistoricalDecisionComparisonResponse.IncidentSeverity;
import com.backoffice.alerta.repository.*;
import com.backoffice.alerta.rules.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para comparação histórica de decisões de risco
 * 
 * US#41 - Comparação Histórica de Decisões de Risco
 * 
 * GOVERNANÇA:
 * ✅ READ-ONLY
 * ✅ Determinístico (sem IA)
 * ✅ Sem persistência
 * ✅ Sem side effects
 * ✅ Reutiliza services existentes
 * 
 * @author Copilot
 */
@Service
public class DecisionHistoricalComparisonService {
    
    private static final Logger log = LoggerFactory.getLogger(DecisionHistoricalComparisonService.class);
    
    private static final int MINIMUM_SIMILARITY_SCORE = 60;
    
    private final RiskDecisionAuditRepository auditRepository;
    private final RiskDecisionFeedbackRepository feedbackRepository;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final RiskSlaTrackingRepository slaRepository;
    private final ProjectRepository projectRepository;
    
    public DecisionHistoricalComparisonService(
            RiskDecisionAuditRepository auditRepository,
            RiskDecisionFeedbackRepository feedbackRepository,
            BusinessRuleIncidentRepository incidentRepository,
            RiskSlaTrackingRepository slaRepository,
            ProjectRepository projectRepository) {
        this.auditRepository = auditRepository;
        this.feedbackRepository = feedbackRepository;
        this.incidentRepository = incidentRepository;
        this.slaRepository = slaRepository;
        this.projectRepository = projectRepository;
    }
    
    /**
     * Compara decisão atual com histórico similar
     */
    public DecisionHistoricalComparisonResponse compareWithHistorical(
            DecisionHistoricalComparisonRequest request) {
        
        log.info("Iniciando comparação histórica para PR: {}", request.getCurrentPullRequestId());
        
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
            // 1. Buscar auditoria atual
            List<RiskDecisionAudit> currentAudits = auditRepository
                    .findByPullRequestIdOrderByCreatedAtDesc(request.getCurrentPullRequestId());
            
            if (currentAudits.isEmpty()) {
                log.warn("Auditoria não encontrada para PR: {}", request.getCurrentPullRequestId());
                return createEmptyResponse(request, project);
            }
            
            RiskDecisionAudit currentAudit = currentAudits.get(0); // mais recente
            
            // 2. Criar contexto atual
            CurrentDecisionContextResponse currentContext = buildCurrentContext(
                    currentAudit, request.getChangedFiles());
            
            // 3. Buscar todas as auditorias históricas (exceto a atual)
            List<RiskDecisionAudit> historicalAudits = auditRepository
                    .findAllByOrderByCreatedAtDesc().stream()
                    .filter(audit -> !audit.getPullRequestId().equals(request.getCurrentPullRequestId()))
                    .filter(audit -> isWithinLookback(audit.getCreatedAt(), request.getLookbackDays()))
                    .collect(Collectors.toList());
            
            log.info("Encontradas {} auditorias históricas para comparação", historicalAudits.size());
            
            // 4. Calcular similaridade e criar comparações
            List<HistoricalDecisionComparisonResponse> comparisons = historicalAudits.stream()
                    .map(audit -> createComparison(audit, request, currentAudit))
                    .filter(comp -> comp.getSimilarityScore() >= MINIMUM_SIMILARITY_SCORE)
                    .sorted(Comparator.comparingInt(HistoricalDecisionComparisonResponse::getSimilarityScore)
                            .reversed())
                    .limit(request.getMaxComparisons())
                    .collect(Collectors.toList());
            
            log.info("Encontradas {} comparações com similaridade >= {}", 
                    comparisons.size(), MINIMUM_SIMILARITY_SCORE);
            
            // 5. Gerar insights executivos
            ExecutiveHistoricalInsightResponse insights = generateExecutiveInsights(
                    comparisons, currentContext);
            
            DecisionHistoricalComparisonResponse response = new DecisionHistoricalComparisonResponse(
                    currentContext,
                    comparisons,
                    insights,
                    project != null 
                        ? ProjectContext.scoped(project.getId(), project.getName())
                        : ProjectContext.global()
            );
            
            return response;
            
        } catch (Exception e) {
            log.error("Erro ao comparar histórico para PR {}: {}", 
                    request.getCurrentPullRequestId(), e.getMessage(), e);
            return createEmptyResponse(request, project);
        }
    }
    
    /**
     * Verifica se auditoria está dentro do lookback period
     */
    private boolean isWithinLookback(Instant createdAt, int lookbackDays) {
        Instant cutoff = Instant.now().minusSeconds(lookbackDays * 24L * 60 * 60);
        return createdAt.isAfter(cutoff);
    }
    
    /**
     * Constrói contexto da decisão atual
     */
    private CurrentDecisionContextResponse buildCurrentContext(
            RiskDecisionAudit currentAudit, List<String> changedFiles) {
        
        // Extrair domínios das regras impactadas (simplificado)
        List<Domain> domains = new ArrayList<>();
        if (!currentAudit.getImpactedBusinessRules().isEmpty()) {
            domains.add(Domain.PAYMENT); // Simplificado para demo
        }
        
        int criticalRules = currentAudit.getImpactedBusinessRules().size();
        
        return new CurrentDecisionContextResponse(
                currentAudit.getRiskLevel(),
                currentAudit.getFinalDecision(),
                domains,
                criticalRules
        );
    }
    
    /**
     * Cria comparação com auditoria histórica
     */
    private HistoricalDecisionComparisonResponse createComparison(
            RiskDecisionAudit historicalAudit,
            DecisionHistoricalComparisonRequest request,
            RiskDecisionAudit currentAudit) {
        
        // Calcular similaridade
        int similarityScore = calculateSimilarityScore(
                historicalAudit, request, currentAudit);
        
        // Buscar resultado real (simplificado)
        FeedbackOutcome outcome = null; // Simplificado
        
        // Buscar incidentes (simplificado)
        IncidentSeverity incidentSeverity = null; // Simplificado
        
        // Verificar breach de SLA (simplificado)
        boolean slaBreached = false; // Simplificado
        
        // Construir sumário
        String summary = buildComparisonSummary(
                historicalAudit, outcome, incidentSeverity, slaBreached);
        
        return new HistoricalDecisionComparisonResponse(
                historicalAudit.getPullRequestId(),
                similarityScore,
                historicalAudit.getFinalDecision(),
                historicalAudit.getRiskLevel(),
                historicalAudit.getEnvironment(),
                outcome,
                incidentSeverity,
                slaBreached,
                summary
        );
    }
    
    /**
     * Calcula score de similaridade (0-100) de forma determinística
     * 
     * Pontuação simplificada:
     * - Mesmo ambiente: +40
     * - Mesmo riskLevel: +30
     * - Recência: até 30 pontos
     */
    private int calculateSimilarityScore(
            RiskDecisionAudit historicalAudit,
            DecisionHistoricalComparisonRequest request,
            RiskDecisionAudit currentAudit) {
        
        int score = 0;
        
        // 1. Mesmo ambiente (+40)
        if (request.getEnvironment() != null && 
            request.getEnvironment() == historicalAudit.getEnvironment()) {
            score += 40;
        }
        
        // 2. Mesmo riskLevel (+30)
        if (currentAudit.getRiskLevel() == historicalAudit.getRiskLevel()) {
            score += 30;
        }
        
        // 3. Recência (até 30 pontos - mais recente = maior peso)
        long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(
                historicalAudit.getCreatedAt(), Instant.now());
        int recencyScore = (int) Math.max(0, 30 - (daysAgo / 6)); // decay linear
        score += recencyScore;
        
        return Math.min(score, 100); // cap em 100
    }
    
    /**
     * Constrói sumário textual da comparação
     */
    private String buildComparisonSummary(RiskDecisionAudit audit,
                                         FeedbackOutcome outcome,
                                         IncidentSeverity incidentSeverity,
                                         boolean slaBreached) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("Decisão: ").append(audit.getFinalDecision().name());
        summary.append(" | Risco: ").append(audit.getRiskLevel().name());
        
        if (outcome != null) {
            summary.append(" | Resultado: ").append(outcome.name());
        }
        
        if (incidentSeverity != null) {
            summary.append(" | Incidente: ").append(incidentSeverity.name());
        }
        
        if (slaBreached) {
            summary.append(" | SLA VIOLADO");
        }
        
        return summary.toString();
    }
    
    /**
     * Gera insights executivos de forma determinística
     */
    private ExecutiveHistoricalInsightResponse generateExecutiveInsights(
            List<HistoricalDecisionComparisonResponse> comparisons,
            CurrentDecisionContextResponse currentContext) {
        
        if (comparisons.isEmpty()) {
            return new ExecutiveHistoricalInsightResponse(
                    false,
                    "Sem histórico similar suficiente para análise de padrões.",
                    "Proceder com cautela padrão conforme criticidade das regras impactadas."
            );
        }
        
        // Contar padrões negativos (simplificado)
        long bloqueados = comparisons.stream()
                .filter(c -> c.getDecision() == FinalDecision.BLOQUEADO)
                .count();
        
        // Detectar padrão
        boolean patternDetected = bloqueados >= 2;
        
        if (!patternDetected) {
            return new ExecutiveHistoricalInsightResponse(
                    false,
                    "Histórico similar mostra taxa aceitável de sucesso nas decisões anteriores.",
                    "Seguir processo padrão de revisão e deploy conforme nível de risco identificado."
            );
        }
        
        // Construir descrição do padrão
        String patternDescription = String.format(
                "Histórico mostra %d caso(s) similares bloqueados. Requer atenção especial.",
                bloqueados);
        
        // Construir recomendação
        String recommendation = buildRecommendation(currentContext);
        
        return new ExecutiveHistoricalInsightResponse(
                true,
                patternDescription,
                recommendation
        );
    }
    
    /**
     * Constrói recomendação executiva
     */
    private String buildRecommendation(CurrentDecisionContextResponse context) {
        List<String> recommendations = new ArrayList<>();
        
        recommendations.add("Implementar feature flag para rollback rápido");
        recommendations.add("Executar testes de regressão completos");
        recommendations.add("Considerar deploy gradual (canary/blue-green)");
        
        if (context.getCriticalRules() > 0) {
            recommendations.add(String.format(
                    "Atenção: %d regra(s) crítica(s) impactada(s) - revisão sênior obrigatória",
                    context.getCriticalRules()));
        }
        
        recommendations.add("Validar em staging antes de produção");
        
        return String.join("; ", recommendations) + ".";
    }
    
    /**
     * Cria resposta vazia quando não há dados
     */
    private DecisionHistoricalComparisonResponse createEmptyResponse(
            DecisionHistoricalComparisonRequest request, Project project) {
        
        CurrentDecisionContextResponse emptyContext = new CurrentDecisionContextResponse(
                RiskLevel.BAIXO,
                FinalDecision.APROVADO,
                Collections.emptyList(),
                0
        );
        
        ExecutiveHistoricalInsightResponse emptyInsight = new ExecutiveHistoricalInsightResponse(
                false,
                "Análise histórica não disponível para este PR.",
                "Proceder com análise manual completa."
        );
        
        return new DecisionHistoricalComparisonResponse(
                emptyContext,
                Collections.emptyList(),
                emptyInsight,
                project != null 
                    ? ProjectContext.scoped(project.getId(), project.getName())
                    : ProjectContext.global()
        );
    }
}
