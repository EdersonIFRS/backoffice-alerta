package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.RiskAdjustmentAnalysisRequest;
import com.backoffice.alerta.rules.*;
import com.backoffice.alerta.repository.RiskDecisionFeedbackRepository;
import com.backoffice.alerta.repository.RiskDecisionAuditRepository;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de aprendizado organizacional para ajuste de risco
 * 
 * IMPORTANTE: Este serviço é APENAS CONSULTIVO
 * - NÃO modifica regras de negócio
 * - NÃO recalcula riscos passados
 * - NÃO altera auditorias
 * - Apenas gera sugestões baseadas em evidências
 */
@Service
public class RiskLearningAdjustmentService {

    private static final Logger logger = LoggerFactory.getLogger(RiskLearningAdjustmentService.class);

    private final RiskDecisionFeedbackRepository feedbackRepository;
    private final RiskDecisionAuditRepository auditRepository;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final BusinessRuleRepository businessRuleRepository;

    public RiskLearningAdjustmentService(RiskDecisionFeedbackRepository feedbackRepository,
                                        RiskDecisionAuditRepository auditRepository,
                                        BusinessRuleIncidentRepository incidentRepository,
                                        BusinessRuleRepository businessRuleRepository) {
        this.feedbackRepository = feedbackRepository;
        this.auditRepository = auditRepository;
        this.incidentRepository = incidentRepository;
        this.businessRuleRepository = businessRuleRepository;
    }

    /**
     * Analisa histórico e gera sugestões de ajuste de risco
     * @param request Parâmetros da análise
     * @return Lista de sugestões geradas
     */
    public List<RiskAdjustmentSuggestion> analyzeLearning(RiskAdjustmentAnalysisRequest request) {
        logger.info("Iniciando análise de aprendizado - janela: {} dias, confiança mínima: {}%",
                   request.getTimeWindowDays(), request.getMinimumConfidence());

        // Validações
        validateRequest(request);

        // Define janela de tempo
        Instant cutoffTime = Instant.now().minus(request.getTimeWindowDays(), ChronoUnit.DAYS);

        // Coleta dados dentro da janela
        List<RiskDecisionFeedback> feedbacks = filterByTimeWindow(
            feedbackRepository.findAll(), cutoffTime
        );
        List<RiskDecisionAudit> audits = filterAuditsByTimeWindow(
            auditRepository.findAll(), cutoffTime
        );
        List<BusinessRuleIncident> incidents = filterIncidentsByTimeWindow(
            incidentRepository.findAll(), cutoffTime
        );

        logger.info("Dados coletados - Feedbacks: {}, Auditorias: {}, Incidentes: {}",
                   feedbacks.size(), audits.size(), incidents.size());

        // Agrupa por regra de negócio
        Map<String, RuleEvidence> evidenceByRule = groupEvidenceByRule(feedbacks, audits, incidents);

        // Gera sugestões
        List<RiskAdjustmentSuggestion> suggestions = new ArrayList<>();
        
        for (Map.Entry<String, RuleEvidence> entry : evidenceByRule.entrySet()) {
            String ruleId = entry.getKey();
            RuleEvidence evidence = entry.getValue();
            
            // Analisa padrões
            List<RiskAdjustmentSuggestion> ruleSuggestions = analyzeRuleEvidence(ruleId, evidence);
            
            // Filtra por confiança mínima
            ruleSuggestions.stream()
                .filter(s -> s.getConfidenceLevel() >= request.getMinimumConfidence())
                .forEach(suggestions::add);
        }

        logger.info("Análise concluída - {} sugestões geradas", suggestions.size());
        return suggestions;
    }

    /**
     * Valida requisição
     */
    private void validateRequest(RiskAdjustmentAnalysisRequest request) {
        if (request.getTimeWindowDays() == null || request.getTimeWindowDays() <= 0) {
            throw new IllegalArgumentException("timeWindowDays deve ser maior que 0");
        }
        if (request.getMinimumConfidence() == null || 
            request.getMinimumConfidence() < 0 || 
            request.getMinimumConfidence() > 100) {
            throw new IllegalArgumentException("minimumConfidence deve estar entre 0 e 100");
        }
    }

    /**
     * Filtra feedbacks por janela de tempo
     */
    private List<RiskDecisionFeedback> filterByTimeWindow(List<RiskDecisionFeedback> feedbacks, 
                                                          Instant cutoffTime) {
        return feedbacks.stream()
            .filter(f -> f.getCreatedAt().isAfter(cutoffTime))
            .collect(Collectors.toList());
    }

    /**
     * Filtra auditorias por janela de tempo
     */
    private List<RiskDecisionAudit> filterAuditsByTimeWindow(List<RiskDecisionAudit> audits,
                                                             Instant cutoffTime) {
        return audits.stream()
            .filter(a -> a.getCreatedAt().isAfter(cutoffTime))
            .collect(Collectors.toList());
    }

    /**
     * Filtra incidentes por janela de tempo
     */
    private List<BusinessRuleIncident> filterIncidentsByTimeWindow(List<BusinessRuleIncident> incidents,
                                                                   Instant cutoffTime) {
        return incidents.stream()
            .filter(i -> i.getOccurredAt().isAfter(cutoffTime))
            .collect(Collectors.toList());
    }

    /**
     * Agrupa evidências por regra de negócio
     */
    private Map<String, RuleEvidence> groupEvidenceByRule(List<RiskDecisionFeedback> feedbacks,
                                                          List<RiskDecisionAudit> audits,
                                                          List<BusinessRuleIncident> incidents) {
        Map<String, RuleEvidence> evidenceMap = new HashMap<>();

        // Processa feedbacks
        for (RiskDecisionFeedback feedback : feedbacks) {
            // Busca auditoria correspondente para obter regras impactadas
            Optional<RiskDecisionAudit> auditOpt = audits.stream()
                .filter(a -> a.getId().equals(feedback.getAuditId()))
                .findFirst();
            
            if (auditOpt.isPresent()) {
                RiskDecisionAudit audit = auditOpt.get();
                for (String ruleName : audit.getImpactedBusinessRules()) {
                    // Busca ID da regra pelo nome
                    Optional<String> ruleId = findRuleIdByName(ruleName);
                    if (ruleId.isPresent()) {
                        evidenceMap.computeIfAbsent(ruleId.get(), k -> new RuleEvidence())
                            .addFeedback(feedback, audit);
                    }
                }
            }
        }

        // Processa incidentes
        for (BusinessRuleIncident incident : incidents) {
            evidenceMap.computeIfAbsent(incident.getBusinessRuleId().toString(), k -> new RuleEvidence())
                .addIncident(incident);
        }

        return evidenceMap;
    }

    /**
     * Busca ID da regra pelo nome
     */
    private Optional<String> findRuleIdByName(String ruleName) {
        return businessRuleRepository.findAll().stream()
            .filter(r -> r.getName().equals(ruleName))
            .map(BusinessRule::getId)
            .findFirst();
    }

    /**
     * Analisa evidências de uma regra e gera sugestões
     */
    private List<RiskAdjustmentSuggestion> analyzeRuleEvidence(String ruleId, RuleEvidence evidence) {
        List<RiskAdjustmentSuggestion> suggestions = new ArrayList<>();

        Optional<BusinessRule> ruleOpt = businessRuleRepository.findById(ruleId);
        if (ruleOpt.isEmpty()) {
            return suggestions;
        }

        BusinessRule rule = ruleOpt.get();

        // Padrão 1: Falsos negativos (sistema muito permissivo)
        if (evidence.getFalseNegativeCount() >= 2) {
            suggestions.add(createCriticalityIncreaseSuggestion(rule, evidence));
        }

        // Padrão 2: Falsos positivos (sistema muito conservador)
        if (evidence.getFalsePositiveCount() >= 2) {
            suggestions.add(createCriticalityDecreaseSuggestion(rule, evidence));
        }

        // Padrão 3: Incidentes após aprovação
        if (evidence.getIncidentsAfterApproval() >= 2) {
            suggestions.add(createIncidentBasedSuggestion(rule, evidence));
        }

        return suggestions;
    }

    /**
     * Cria sugestão de aumento de criticidade (falsos negativos)
     */
    private RiskAdjustmentSuggestion createCriticalityIncreaseSuggestion(BusinessRule rule, 
                                                                        RuleEvidence evidence) {
        Criticality current = rule.getCriticality();
        Criticality suggested = suggestHigherCriticality(current);
        
        int confidence = calculateConfidence(
            evidence.getFalseNegativeCount(),
            evidence.getTotalIncidentSeverity(),
            evidence.hasProductionIncidents()
        );

        String evidenceSummary = String.format(
            "Detectados %d caso(s) de falso negativo nos últimos períodos. " +
            "Mudanças aprovadas com risco BAIXO nesta regra causaram %d incidente(s). " +
            "Sistema está subestimando o risco real.",
            evidence.getFalseNegativeCount(),
            evidence.getIncidentCount()
        );

        return new RiskAdjustmentSuggestion(
            rule.getId(),
            rule.getName(),
            AdjustmentSuggestionType.CRITICALITY_LEVEL,
            current.name(),
            suggested.name(),
            confidence,
            LearningSignal.FALSE_NEGATIVE_TREND,
            evidenceSummary
        );
    }

    /**
     * Cria sugestão de redução de criticidade (falsos positivos)
     */
    private RiskAdjustmentSuggestion createCriticalityDecreaseSuggestion(BusinessRule rule,
                                                                        RuleEvidence evidence) {
        Criticality current = rule.getCriticality();
        Criticality suggested = suggestLowerCriticality(current);
        
        int confidence = calculateConfidence(
            evidence.getFalsePositiveCount(),
            0, // Sem incidentes
            false
        );

        String evidenceSummary = String.format(
            "Detectados %d caso(s) de falso positivo. " +
            "Mudanças com risco ALTO nesta regra foram aprovadas e não causaram problemas. " +
            "Sistema pode estar sendo muito conservador.",
            evidence.getFalsePositiveCount()
        );

        return new RiskAdjustmentSuggestion(
            rule.getId(),
            rule.getName(),
            AdjustmentSuggestionType.CRITICALITY_LEVEL,
            current.name(),
            suggested.name(),
            confidence,
            LearningSignal.FALSE_POSITIVE_TREND,
            evidenceSummary
        );
    }

    /**
     * Cria sugestão baseada em incidentes
     */
    private RiskAdjustmentSuggestion createIncidentBasedSuggestion(BusinessRule rule,
                                                                   RuleEvidence evidence) {
        Criticality current = rule.getCriticality();
        Criticality suggested = suggestHigherCriticality(current);
        
        int confidence = calculateConfidence(
            evidence.getIncidentsAfterApproval(),
            evidence.getTotalIncidentSeverity(),
            evidence.hasProductionIncidents()
        );

        String evidenceSummary = String.format(
            "Detectados %d incidente(s) após aprovação de mudanças nesta regra. " +
            "Severidade total: %d pontos. %s " +
            "Sugere-se elevar criticidade para aumentar rigor nas aprovações.",
            evidence.getIncidentsAfterApproval(),
            evidence.getTotalIncidentSeverity(),
            evidence.hasProductionIncidents() ? "Inclui incidentes em PRODUÇÃO." : ""
        );

        return new RiskAdjustmentSuggestion(
            rule.getId(),
            rule.getName(),
            AdjustmentSuggestionType.CRITICALITY_LEVEL,
            current.name(),
            suggested.name(),
            confidence,
            LearningSignal.INCIDENT_AFTER_APPROVAL,
            evidenceSummary
        );
    }

    /**
     * Sugere criticidade mais alta
     */
    private Criticality suggestHigherCriticality(Criticality current) {
        return switch (current) {
            case BAIXA -> Criticality.MEDIA;
            case MEDIA -> Criticality.ALTA;
            case ALTA, CRITICA -> Criticality.CRITICA;
        };
    }

    /**
     * Sugere criticidade mais baixa
     */
    private Criticality suggestLowerCriticality(Criticality current) {
        return switch (current) {
            case CRITICA -> Criticality.ALTA;
            case ALTA -> Criticality.MEDIA;
            case MEDIA, BAIXA -> Criticality.BAIXA;
        };
    }

    /**
     * Calcula nível de confiança da sugestão (0-100)
     */
    private int calculateConfidence(int evidenceCount, int severityPoints, boolean hasProductionImpact) {
        int baseConfidence = Math.min(evidenceCount * 25, 75); // 25% por evidência, máx 75%
        int severityBonus = Math.min(severityPoints, 15); // Até 15% por severidade
        int productionBonus = hasProductionImpact ? 10 : 0; // 10% se teve impacto em produção
        
        return Math.min(100, baseConfidence + severityBonus + productionBonus);
    }

    /**
     * Classe interna para agrupar evidências de uma regra
     */
    private static class RuleEvidence {
        private final List<RiskDecisionFeedback> feedbacks = new ArrayList<>();
        private final List<RiskDecisionAudit> relatedAudits = new ArrayList<>();
        private final List<BusinessRuleIncident> incidents = new ArrayList<>();

        void addFeedback(RiskDecisionFeedback feedback, RiskDecisionAudit audit) {
            feedbacks.add(feedback);
            relatedAudits.add(audit);
        }

        void addIncident(BusinessRuleIncident incident) {
            incidents.add(incident);
        }

        int getFalseNegativeCount() {
            return (int) feedbacks.stream()
                .filter(f -> f.getOutcome() == FeedbackOutcome.FALSE_NEGATIVE_RISK)
                .count();
        }

        int getFalsePositiveCount() {
            return (int) feedbacks.stream()
                .filter(f -> f.getOutcome() == FeedbackOutcome.FALSE_POSITIVE_RISK)
                .count();
        }

        int getIncidentsAfterApproval() {
            return (int) feedbacks.stream()
                .filter(f -> f.getOutcome() == FeedbackOutcome.INCIDENT || 
                           f.getOutcome() == FeedbackOutcome.ROLLBACK)
                .count();
        }

        int getIncidentCount() {
            return incidents.size();
        }

        int getTotalIncidentSeverity() {
            return incidents.stream()
                .mapToInt(i -> i.getSeverity().getRiskWeight())
                .sum();
        }

        boolean hasProductionIncidents() {
            // Verifica se alguma auditoria relacionada foi em PRODUCTION
            return relatedAudits.stream()
                .anyMatch(a -> a.getEnvironment() == Environment.PRODUCTION);
        }
    }
}

