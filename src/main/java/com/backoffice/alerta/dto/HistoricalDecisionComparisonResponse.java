package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Environment;
import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskLevel;

/**
 * Comparação com uma decisão histórica similar
 * 
 * US#41 - Comparação Histórica de Decisões de Risco
 */
public class HistoricalDecisionComparisonResponse {
    
    private final String pullRequestId;
    private final int similarityScore;
    private final FinalDecision decision;
    private final RiskLevel riskLevel;
    private final Environment environment;
    private final FeedbackOutcome outcome;
    private final IncidentSeverity incidentSeverity;
    private final boolean slaBreached;
    private final String summary;
    
    public HistoricalDecisionComparisonResponse(String pullRequestId,
                                               int similarityScore,
                                               FinalDecision decision,
                                               RiskLevel riskLevel,
                                               Environment environment,
                                               FeedbackOutcome outcome,
                                               IncidentSeverity incidentSeverity,
                                               boolean slaBreached,
                                               String summary) {
        this.pullRequestId = pullRequestId;
        this.similarityScore = similarityScore;
        this.decision = decision;
        this.riskLevel = riskLevel;
        this.environment = environment;
        this.outcome = outcome;
        this.incidentSeverity = incidentSeverity;
        this.slaBreached = slaBreached;
        this.summary = summary;
    }
    
    public String getPullRequestId() {
        return pullRequestId;
    }
    
    public int getSimilarityScore() {
        return similarityScore;
    }
    
    public FinalDecision getDecision() {
        return decision;
    }
    
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    public Environment getEnvironment() {
        return environment;
    }
    
    public FeedbackOutcome getOutcome() {
        return outcome;
    }
    
    public IncidentSeverity getIncidentSeverity() {
        return incidentSeverity;
    }
    
    public boolean isSlaBreached() {
        return slaBreached;
    }
    
    public String getSummary() {
        return summary;
    }
    
    /**
     * Enum para severidade de incidentes
     */
    public enum IncidentSeverity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }
    
    /**
     * Enum para resultado do feedback
     */
    public enum FeedbackOutcome {
        CORRECT_APPROVAL,
        CORRECT_REJECTION,
        FALSE_POSITIVE_BLOCK,
        FALSE_NEGATIVE_RISK
    }
}
