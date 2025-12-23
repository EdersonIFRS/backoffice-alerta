package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.RiskLevelMetrics;
import com.backoffice.alerta.dto.RiskMetricsResponse;
import com.backoffice.alerta.dto.TrendIndicator;
import com.backoffice.alerta.rules.*;
import com.backoffice.alerta.repository.RiskDecisionAuditRepository;
import com.backoffice.alerta.repository.RiskDecisionFeedbackRepository;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de métricas de acurácia e confiabilidade do sistema de risco
 * 
 * Correlaciona auditorias (US#20), feedbacks (US#21) e incidentes (US#17)
 * para calcular métricas de performance das decisões de risco.
 * 
 * IMPORTANTE: Serviço READ-ONLY
 * - NÃO modifica decisões de risco
 * - NÃO recalcula scores
 * - NÃO altera regras ou políticas
 * - Apenas lê e correlaciona dados existentes
 * - Lógica 100% determinística e auditável
 */
@Service
public class RiskMetricsService {

    private static final Logger log = LoggerFactory.getLogger(RiskMetricsService.class);

    private final RiskDecisionAuditRepository auditRepository;
    private final RiskDecisionFeedbackRepository feedbackRepository;
    private final BusinessRuleIncidentRepository incidentRepository;

    public RiskMetricsService(RiskDecisionAuditRepository auditRepository,
                             RiskDecisionFeedbackRepository feedbackRepository,
                             BusinessRuleIncidentRepository incidentRepository) {
        this.auditRepository = auditRepository;
        this.feedbackRepository = feedbackRepository;
        this.incidentRepository = incidentRepository;
    }

    /**
     * Calcula métricas de acurácia com filtros opcionais
     */
    public RiskMetricsResponse calculateMetrics(LocalDate from,
                                               LocalDate to,
                                               String environment,
                                               UUID businessRuleId) {
        log.info("Calculando métricas de acurácia - from: {}, to: {}, env: {}, ruleId: {}",
                from, to, environment, businessRuleId);

        // Busca dados
        List<RiskDecisionAudit> audits = fetchAndFilterAudits(from, to, environment);
        List<RiskDecisionFeedback> feedbacks = fetchAndFilterFeedbacks(from, to);
        List<BusinessRuleIncident> incidents = fetchAndFilterIncidents(from, to, businessRuleId);

        // Correlaciona dados
        Map<UUID, RiskDecisionAudit> auditMap = audits.stream()
                .collect(Collectors.toMap(RiskDecisionAudit::getId, a -> a));
        
        Map<UUID, RiskDecisionFeedback> feedbackByAuditId = feedbacks.stream()
                .collect(Collectors.toMap(RiskDecisionFeedback::getAuditId, f -> f, (f1, f2) -> f1));

        // Calcula métricas gerais
        MetricsAccumulator accumulator = new MetricsAccumulator();
        
        for (RiskDecisionAudit audit : audits) {
            RiskDecisionFeedback feedback = feedbackByAuditId.get(audit.getId());
            List<BusinessRuleIncident> relatedIncidents = findIncidentsByPullRequest(
                    incidents, audit.getPullRequestId(), audit.getImpactedBusinessRules());
            
            accumulator.processDecision(audit, feedback, relatedIncidents);
        }

        // Calcula métricas por nível de risco
        Map<RiskLevel, RiskLevelMetrics> metricsByLevel = calculateMetricsByRiskLevel(audits, feedbackByAuditId, incidents);

        // Detecta tendências
        List<TrendIndicator> trends = detectTrends(audits, feedbacks, incidents, from, to);

        // Calcula score de confiança do sistema
        double confidenceScore = calculateSystemConfidenceScore(accumulator, from, to);

        // Monta resposta
        String appliedFilters = buildFilterDescription(from, to, environment, businessRuleId);
        
        log.info("Métricas calculadas - decisões: {}, acurácia: {}%, confiança: {}%",
                accumulator.getTotalDecisions(),
                String.format("%.1f", accumulator.getAccuracyRate()),
                String.format("%.1f", confidenceScore));

        return new RiskMetricsResponse(
                accumulator.getTotalDecisions(),
                accumulator.getTotalDeploys(),
                accumulator.getTotalIncidents(),
                accumulator.getApprovedCount(),
                accumulator.getApprovedWithRestrictionsCount(),
                accumulator.getBlockedCount(),
                accumulator.getAccuracyRate(),
                accumulator.getFalsePositiveRate(),
                accumulator.getFalseNegativeRate(),
                accumulator.getIncidentAfterApprovalRate(),
                accumulator.getSafeChangeBlockedRate(),
                metricsByLevel,
                trends,
                confidenceScore,
                from != null ? from : getOldestDate(audits),
                to != null ? to : LocalDate.now(),
                appliedFilters
        );
    }

    /**
     * Busca e filtra auditorias
     */
    private List<RiskDecisionAudit> fetchAndFilterAudits(LocalDate from, LocalDate to, String environment) {
        List<RiskDecisionAudit> audits = auditRepository.findAll();
        
        return audits.stream()
                .filter(audit -> matchesDateRange(audit.getCreatedAt(), from, to))
                .filter(audit -> environment == null || audit.getEnvironment().name().equalsIgnoreCase(environment))
                .collect(Collectors.toList());
    }

    /**
     * Busca e filtra feedbacks
     */
    private List<RiskDecisionFeedback> fetchAndFilterFeedbacks(LocalDate from, LocalDate to) {
        List<RiskDecisionFeedback> feedbacks = feedbackRepository.findAll();
        
        return feedbacks.stream()
                .filter(feedback -> matchesDateRange(feedback.getCreatedAt(), from, to))
                .collect(Collectors.toList());
    }

    /**
     * Busca e filtra incidentes
     */
    private List<BusinessRuleIncident> fetchAndFilterIncidents(LocalDate from, LocalDate to, UUID businessRuleId) {
        List<BusinessRuleIncident> incidents = incidentRepository.findAll();
        
        return incidents.stream()
                .filter(incident -> matchesDateRange(incident.getOccurredAt(), from, to))
                .filter(incident -> businessRuleId == null || incident.getBusinessRuleId().equals(businessRuleId.toString()))
                .collect(Collectors.toList());
    }

    /**
     * Verifica se timestamp está dentro do range de datas
     */
    private boolean matchesDateRange(Instant timestamp, LocalDate from, LocalDate to) {
        LocalDate date = timestamp.atZone(ZoneId.systemDefault()).toLocalDate();
        
        if (from != null && date.isBefore(from)) {
            return false;
        }
        if (to != null && date.isAfter(to)) {
            return false;
        }
        return true;
    }

    /**
     * Encontra incidentes relacionados a um pull request
     */
    private List<BusinessRuleIncident> findIncidentsByPullRequest(List<BusinessRuleIncident> incidents,
                                                                  String pullRequestId,
                                                                  List<String> impactedRules) {
        if (impactedRules == null || impactedRules.isEmpty()) {
            return Collections.emptyList();
        }

        // Correlaciona por regras de negócio impactadas
        return incidents.stream()
                .filter(incident -> impactedRules.contains(incident.getBusinessRuleId()))
                .collect(Collectors.toList());
    }

    /**
     * Calcula métricas agrupadas por nível de risco
     */
    private Map<RiskLevel, RiskLevelMetrics> calculateMetricsByRiskLevel(
            List<RiskDecisionAudit> audits,
            Map<UUID, RiskDecisionFeedback> feedbackMap,
            List<BusinessRuleIncident> incidents) {
        
        Map<RiskLevel, MetricsAccumulator> accumulatorsByLevel = new EnumMap<>(RiskLevel.class);
        
        for (RiskDecisionAudit audit : audits) {
            RiskLevel level = audit.getRiskLevel();
            MetricsAccumulator accumulator = accumulatorsByLevel.computeIfAbsent(
                    level, k -> new MetricsAccumulator());
            
            RiskDecisionFeedback feedback = feedbackMap.get(audit.getId());
            List<BusinessRuleIncident> relatedIncidents = findIncidentsByPullRequest(
                    incidents, audit.getPullRequestId(), audit.getImpactedBusinessRules());
            
            accumulator.processDecision(audit, feedback, relatedIncidents);
        }

        Map<RiskLevel, RiskLevelMetrics> result = new EnumMap<>(RiskLevel.class);
        for (Map.Entry<RiskLevel, MetricsAccumulator> entry : accumulatorsByLevel.entrySet()) {
            MetricsAccumulator acc = entry.getValue();
            result.put(entry.getKey(), new RiskLevelMetrics(
                    entry.getKey(),
                    acc.getTotalDecisions(),
                    acc.getApprovedCount(),
                    acc.getBlockedCount(),
                    acc.getDeploysWithIncidents(),
                    acc.getDeploysWithSuccess(),
                    acc.getAccuracyRate(),
                    acc.getFalsePositiveRate(),
                    acc.getFalseNegativeRate()
            ));
        }

        return result;
    }

    /**
     * Detecta tendências nos dados
     */
    private List<TrendIndicator> detectTrends(List<RiskDecisionAudit> audits,
                                              List<RiskDecisionFeedback> feedbacks,
                                              List<BusinessRuleIncident> incidents,
                                              LocalDate from,
                                              LocalDate to) {
        List<TrendIndicator> trends = new ArrayList<>();

        // Tendência de falsos positivos
        long falsePositivesCount = feedbacks.stream()
                .filter(f -> f.getOutcome() == FeedbackOutcome.FALSE_POSITIVE_RISK)
                .count();
        if (falsePositivesCount >= 3) {
            double rate = (falsePositivesCount * 100.0) / Math.max(1, feedbacks.size());
            TrendIndicator.Severity severity = rate > 20 ? TrendIndicator.Severity.HIGH :
                                               rate > 10 ? TrendIndicator.Severity.MEDIUM :
                                               TrendIndicator.Severity.LOW;
            trends.add(new TrendIndicator(
                    TrendIndicator.TrendType.FALSE_POSITIVE_INCREASE,
                    severity,
                    String.format("Detectados %d casos de falsos positivos (%.1f%% dos feedbacks)",
                            falsePositivesCount, rate),
                    "SYSTEM",
                    (int) falsePositivesCount,
                    rate
            ));
        }

        // Tendência de falsos negativos
        long falseNegativesCount = feedbacks.stream()
                .filter(f -> f.getOutcome() == FeedbackOutcome.FALSE_NEGATIVE_RISK)
                .count();
        if (falseNegativesCount >= 3) {
            double rate = (falseNegativesCount * 100.0) / Math.max(1, feedbacks.size());
            TrendIndicator.Severity severity = rate > 15 ? TrendIndicator.Severity.CRITICAL :
                                               rate > 10 ? TrendIndicator.Severity.HIGH :
                                               TrendIndicator.Severity.MEDIUM;
            trends.add(new TrendIndicator(
                    TrendIndicator.TrendType.FALSE_NEGATIVE_INCREASE,
                    severity,
                    String.format("Detectados %d casos de falsos negativos (%.1f%% dos feedbacks)",
                            falseNegativesCount, rate),
                    "SYSTEM",
                    (int) falseNegativesCount,
                    rate
            ));
        }

        // Alta taxa de incidentes
        long incidentsCount = incidents.size();
        if (incidentsCount >= 5) {
            double rate = (incidentsCount * 100.0) / Math.max(1, audits.size());
            TrendIndicator.Severity severity = rate > 30 ? TrendIndicator.Severity.CRITICAL :
                                               rate > 20 ? TrendIndicator.Severity.HIGH :
                                               TrendIndicator.Severity.MEDIUM;
            trends.add(new TrendIndicator(
                    TrendIndicator.TrendType.HIGH_INCIDENT_RATE,
                    severity,
                    String.format("Alta taxa de incidentes: %d casos (%.1f%% das decisões)",
                            incidentsCount, rate),
                    "SYSTEM",
                    (int) incidentsCount,
                    rate
            ));
        }

        // Regras de negócio problemáticas
        Map<String, Long> incidentsByRule = incidents.stream()
                .collect(Collectors.groupingBy(incident -> incident.getBusinessRuleId().toString(), Collectors.counting()));
        
        incidentsByRule.entrySet().stream()
                .filter(entry -> entry.getValue() >= 3)
                .forEach(entry -> {
                    long count = entry.getValue();
                    double rate = (count * 100.0) / Math.max(1, incidents.size());
                    TrendIndicator.Severity severity = count >= 5 ? TrendIndicator.Severity.HIGH :
                                                       TrendIndicator.Severity.MEDIUM;
                    trends.add(new TrendIndicator(
                            TrendIndicator.TrendType.PROBLEMATIC_BUSINESS_RULE,
                            severity,
                            String.format("Regra com %d incidentes recorrentes (%.1f%% do total)",
                                    count, rate),
                            entry.getKey().toString(),
                            (int) count,
                            rate
                    ));
                });

        // Degradação por ambiente
        Map<String, Long> incidentsByEnv = audits.stream()
                .collect(Collectors.groupingBy(audit -> audit.getEnvironment().name(), Collectors.counting()));
        
        incidentsByEnv.entrySet().stream()
                .filter(entry -> entry.getValue() >= 10)
                .forEach(entry -> {
                    long count = entry.getValue();
                    double rate = (count * 100.0) / Math.max(1, audits.size());
                    if (rate > 40) {
                        trends.add(new TrendIndicator(
                                TrendIndicator.TrendType.ENVIRONMENT_DEGRADATION,
                                TrendIndicator.Severity.MEDIUM,
                                String.format("Ambiente %s com alta atividade de risco: %d decisões (%.1f%%)",
                                        entry.getKey(), count, rate),
                                entry.getKey(),
                                (int) count,
                                rate
                        ));
                    }
                });

        return trends;
    }

    /**
     * Calcula score de confiança do sistema (0-100)
     * Baseado em acurácia, falsos negativos, incidentes críticos e recência
     */
    private double calculateSystemConfidenceScore(MetricsAccumulator accumulator, LocalDate from, LocalDate to) {
        if (accumulator.getTotalDecisions() == 0) {
            return 0.0;
        }

        // Base: acurácia (peso 60%)
        double baseScore = accumulator.getAccuracyRate() * 0.6;

        // Penalidade por falsos negativos (peso -20%)
        double falseNegativePenalty = accumulator.getFalseNegativeRate() * 0.2;

        // Penalidade por incidentes após aprovação (peso -15%)
        double incidentPenalty = accumulator.getIncidentAfterApprovalRate() * 0.15;

        // Bônus por dados recentes (últimos 30 dias = +5%)
        double recencyBonus = 0.0;
        if (to == null || ChronoUnit.DAYS.between(to, LocalDate.now()) <= 30) {
            recencyBonus = 5.0;
        }

        double confidenceScore = baseScore - falseNegativePenalty - incidentPenalty + recencyBonus;
        
        return Math.min(100.0, Math.max(0.0, confidenceScore));
    }

    /**
     * Constrói descrição dos filtros aplicados
     */
    private String buildFilterDescription(LocalDate from, LocalDate to, String environment, UUID businessRuleId) {
        List<String> filters = new ArrayList<>();
        
        if (from != null) filters.add("from: " + from);
        if (to != null) filters.add("to: " + to);
        if (environment != null) filters.add("environment: " + environment);
        if (businessRuleId != null) filters.add("businessRuleId: " + businessRuleId);
        
        return filters.isEmpty() ? "Sem filtros (todos os dados)" : String.join(", ", filters);
    }

    /**
     * Obtém data mais antiga das auditorias
     */
    private LocalDate getOldestDate(List<RiskDecisionAudit> audits) {
        return audits.stream()
                .map(a -> a.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now().minusDays(30));
    }

    /**
     * Acumulador interno para cálculos de métricas
     */
    private static class MetricsAccumulator {
        private int totalDecisions = 0;
        private int totalDeploys = 0;
        private int totalIncidents = 0;
        private int approvedCount = 0;
        private int approvedWithRestrictionsCount = 0;
        private int blockedCount = 0;
        
        private int correctDecisions = 0;
        private int falsePositives = 0;
        private int falseNegatives = 0;
        private int incidentsAfterApproval = 0;
        private int safeChangesBlocked = 0;
        
        private int deploysWithIncidents = 0;
        private int deploysWithSuccess = 0;

        public void processDecision(RiskDecisionAudit audit,
                                   RiskDecisionFeedback feedback,
                                   List<BusinessRuleIncident> relatedIncidents) {
            totalDecisions++;
            
            FinalDecision decision = audit.getFinalDecision();
            boolean hasIncidents = !relatedIncidents.isEmpty();
            
            // Contadores de decisão
            if (decision == FinalDecision.APROVADO) {
                approvedCount++;
            } else if (decision == FinalDecision.APROVADO_COM_RESTRICOES) {
                approvedWithRestrictionsCount++;
            } else if (decision == FinalDecision.BLOQUEADO) {
                blockedCount++;
            }

            // Se tem feedback, usa feedback para classificar
            if (feedback != null) {
                totalDeploys++;
                FeedbackOutcome outcome = feedback.getOutcome();
                
                if (outcome == FeedbackOutcome.SUCCESS) {
                    deploysWithSuccess++;
                    if (decision == FinalDecision.BLOQUEADO) {
                        // BLOQUEADO + SUCCESS = FALSO POSITIVO
                        falsePositives++;
                        safeChangesBlocked++;
                    } else {
                        // APROVADO + SUCCESS = CORRETO
                        correctDecisions++;
                    }
                } else if (outcome == FeedbackOutcome.INCIDENT || outcome == FeedbackOutcome.ROLLBACK) {
                    deploysWithIncidents++;
                    totalIncidents++;
                    if (decision == FinalDecision.APROVADO || decision == FinalDecision.APROVADO_COM_RESTRICOES) {
                        // APROVADO + INCIDENT = FALSO NEGATIVO
                        falseNegatives++;
                        incidentsAfterApproval++;
                    } else {
                        // BLOQUEADO + INCIDENT = CORRETO
                        correctDecisions++;
                    }
                } else if (outcome == FeedbackOutcome.FALSE_POSITIVE_RISK) {
                    falsePositives++;
                    safeChangesBlocked++;
                } else if (outcome == FeedbackOutcome.FALSE_NEGATIVE_RISK) {
                    falseNegatives++;
                } else {
                    // MINOR_ISSUES = conta como decisão correta
                    correctDecisions++;
                }
            } else if (hasIncidents) {
                // Sem feedback, mas tem incidentes relacionados
                totalIncidents += relatedIncidents.size();
                deploysWithIncidents++;
                
                if (decision == FinalDecision.APROVADO || decision == FinalDecision.APROVADO_COM_RESTRICOES) {
                    falseNegatives++;
                    incidentsAfterApproval++;
                } else {
                    correctDecisions++;
                }
            }
        }

        public int getTotalDecisions() { return totalDecisions; }
        public int getTotalDeploys() { return totalDeploys; }
        public int getTotalIncidents() { return totalIncidents; }
        public int getApprovedCount() { return approvedCount; }
        public int getApprovedWithRestrictionsCount() { return approvedWithRestrictionsCount; }
        public int getBlockedCount() { return blockedCount; }
        public int getDeploysWithIncidents() { return deploysWithIncidents; }
        public int getDeploysWithSuccess() { return deploysWithSuccess; }

        public double getAccuracyRate() {
            if (totalDecisions == 0) return 0.0;
            return (correctDecisions * 100.0) / totalDecisions;
        }

        public double getFalsePositiveRate() {
            if (totalDecisions == 0) return 0.0;
            return (falsePositives * 100.0) / totalDecisions;
        }

        public double getFalseNegativeRate() {
            if (totalDecisions == 0) return 0.0;
            return (falseNegatives * 100.0) / totalDecisions;
        }

        public double getIncidentAfterApprovalRate() {
            if (totalDecisions == 0) return 0.0;
            return (incidentsAfterApproval * 100.0) / totalDecisions;
        }

        public double getSafeChangeBlockedRate() {
            if (totalDecisions == 0) return 0.0;
            return (safeChangesBlocked * 100.0) / totalDecisions;
        }
    }
}

