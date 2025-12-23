package com.backoffice.alerta.dto;

import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.rules.Criticality;

import java.util.List;

/**
 * Response da análise de impacto cruzado com cadeia de dependências
 * 
 * Contém todas as regras impactadas (diretas, indiretas e em cascata)
 * e um sumário executivo da análise.
 * 
 * US#36 - Análise de Impacto Cruzado (Cadeia de Regras Afetadas)
 */
public class BusinessImpactChainResponse {
    
    private String pullRequestId;
    private List<ImpactedRuleChainResponse> directImpacts;
    private List<ImpactedRuleChainResponse> indirectImpacts;
    private List<ImpactedRuleChainResponse> cascadeImpacts;
    private ChainSummary summary;
    private ProjectContext projectContext;
    
    public BusinessImpactChainResponse() {
    }
    
    public BusinessImpactChainResponse(String pullRequestId,
                                      List<ImpactedRuleChainResponse> directImpacts,
                                      List<ImpactedRuleChainResponse> indirectImpacts,
                                      List<ImpactedRuleChainResponse> cascadeImpacts,
                                      ChainSummary summary) {
        this.pullRequestId = pullRequestId;
        this.directImpacts = directImpacts;
        this.indirectImpacts = indirectImpacts;
        this.cascadeImpacts = cascadeImpacts;
        this.summary = summary;
    }
    
    public String getPullRequestId() {
        return pullRequestId;
    }
    
    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }
    
    public List<ImpactedRuleChainResponse> getDirectImpacts() {
        return directImpacts;
    }
    
    public void setDirectImpacts(List<ImpactedRuleChainResponse> directImpacts) {
        this.directImpacts = directImpacts;
    }
    
    public List<ImpactedRuleChainResponse> getIndirectImpacts() {
        return indirectImpacts;
    }
    
    public void setIndirectImpacts(List<ImpactedRuleChainResponse> indirectImpacts) {
        this.indirectImpacts = indirectImpacts;
    }
    
    public List<ImpactedRuleChainResponse> getCascadeImpacts() {
        return cascadeImpacts;
    }
    
    public void setCascadeImpacts(List<ImpactedRuleChainResponse> cascadeImpacts) {
        this.cascadeImpacts = cascadeImpacts;
    }
    
    public ChainSummary getSummary() {
        return summary;
    }
    
    public void setSummary(ChainSummary summary) {
        this.summary = summary;
    }
    
    public ProjectContext getProjectContext() {
        return projectContext;
    }
    
    public void setProjectContext(ProjectContext projectContext) {
        this.projectContext = projectContext;
    }
    
    /**
     * Sumário executivo da análise de impacto cruzado
     */
    public static class ChainSummary {
        private int totalRulesAffected;
        private Criticality highestCriticality;
        private boolean requiresExecutiveAttention;
        
        public ChainSummary() {
        }
        
        public ChainSummary(int totalRulesAffected,
                          Criticality highestCriticality,
                          boolean requiresExecutiveAttention) {
            this.totalRulesAffected = totalRulesAffected;
            this.highestCriticality = highestCriticality;
            this.requiresExecutiveAttention = requiresExecutiveAttention;
        }
        
        public int getTotalRulesAffected() {
            return totalRulesAffected;
        }
        
        public void setTotalRulesAffected(int totalRulesAffected) {
            this.totalRulesAffected = totalRulesAffected;
        }
        
        public Criticality getHighestCriticality() {
            return highestCriticality;
        }
        
        public void setHighestCriticality(Criticality highestCriticality) {
            this.highestCriticality = highestCriticality;
        }
        
        public boolean isRequiresExecutiveAttention() {
            return requiresExecutiveAttention;
        }
        
        public void setRequiresExecutiveAttention(boolean requiresExecutiveAttention) {
            this.requiresExecutiveAttention = requiresExecutiveAttention;
        }
    }
}
