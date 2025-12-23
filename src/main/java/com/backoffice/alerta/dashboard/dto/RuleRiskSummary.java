package com.backoffice.alerta.dashboard.dto;

/**
 * Resumo de risco por regra de negócio
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
public class RuleRiskSummary {
    
    private String ruleId;
    private String ruleName;
    private long blockCount;
    private long incidentCount;
    
    public RuleRiskSummary() {
    }
    
    public RuleRiskSummary(String ruleId, String ruleName, long blockCount, long incidentCount) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.blockCount = blockCount;
        this.incidentCount = incidentCount;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public long getBlockCount() {
        return blockCount;
    }
    
    public void setBlockCount(long blockCount) {
        this.blockCount = blockCount;
    }
    
    public long getIncidentCount() {
        return incidentCount;
    }
    
    public void setIncidentCount(long incidentCount) {
        this.incidentCount = incidentCount;
    }
}
