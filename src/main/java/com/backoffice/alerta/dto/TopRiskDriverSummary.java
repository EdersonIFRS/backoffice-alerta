package com.backoffice.alerta.dto;

/**
 * Sumário de um driver de risco (regra de negócio problemática)
 * 
 * Baseado em tendências detectadas pelo sistema de métricas (US#23)
 */
public class TopRiskDriverSummary {
    
    private final String businessRuleId;
    private final String ruleName;
    private final int incidentCount;
    private final int falseNegativeCount;
    private final int falsePositiveCount;
    private final double impactRate;        // 0-100
    private final String primaryIssue;      // Descrição do problema principal

    public TopRiskDriverSummary(String businessRuleId,
                               String ruleName,
                               int incidentCount,
                               int falseNegativeCount,
                               int falsePositiveCount,
                               double impactRate,
                               String primaryIssue) {
        this.businessRuleId = businessRuleId;
        this.ruleName = ruleName;
        this.incidentCount = incidentCount;
        this.falseNegativeCount = falseNegativeCount;
        this.falsePositiveCount = falsePositiveCount;
        this.impactRate = Math.min(100.0, Math.max(0.0, impactRate));
        this.primaryIssue = primaryIssue;
    }

    public String getBusinessRuleId() {
        return businessRuleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public int getIncidentCount() {
        return incidentCount;
    }

    public int getFalseNegativeCount() {
        return falseNegativeCount;
    }

    public int getFalsePositiveCount() {
        return falsePositiveCount;
    }

    public double getImpactRate() {
        return impactRate;
    }

    public String getPrimaryIssue() {
        return primaryIssue;
    }
}
