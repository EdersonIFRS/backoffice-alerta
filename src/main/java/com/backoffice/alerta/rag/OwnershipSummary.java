package com.backoffice.alerta.rag;

/**
 * Resumo de ownership para resposta RAG
 */
public class OwnershipSummary {
    
    private String ruleId;
    private String ruleName;
    private String owner;
    private String team;
    private String contact;
    
    public OwnershipSummary() {}
    
    public OwnershipSummary(String ruleId, String ruleName, String owner, String team) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.owner = owner;
        this.team = team;
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
    
    public String getOwner() {
        return owner;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public String getTeam() {
        return team;
    }
    
    public void setTeam(String team) {
        this.team = team;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
}
