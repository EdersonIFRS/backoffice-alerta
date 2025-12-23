package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Criticality;

import java.util.List;

/**
 * Response representando uma regra impactada na cadeia de dependências
 * 
 * Contém informações sobre o nível de impacto (direto/indireto/cascata)
 * e o caminho de dependência que levou até essa regra.
 * 
 * US#36 - Análise de Impacto Cruzado (Cadeia de Regras Afetadas)
 */
public class ImpactedRuleChainResponse {
    
    private String businessRuleId;
    private String ruleName;
    private ImpactLevel impactLevel;
    private List<String> dependencyPath;
    private Criticality criticality;
    private List<BusinessRuleOwnershipResponse> ownerships;
    
    public ImpactedRuleChainResponse() {
    }
    
    public ImpactedRuleChainResponse(String businessRuleId,
                                    String ruleName,
                                    ImpactLevel impactLevel,
                                    List<String> dependencyPath,
                                    Criticality criticality,
                                    List<BusinessRuleOwnershipResponse> ownerships) {
        this.businessRuleId = businessRuleId;
        this.ruleName = ruleName;
        this.impactLevel = impactLevel;
        this.dependencyPath = dependencyPath;
        this.criticality = criticality;
        this.ownerships = ownerships;
    }
    
    public String getBusinessRuleId() {
        return businessRuleId;
    }
    
    public void setBusinessRuleId(String businessRuleId) {
        this.businessRuleId = businessRuleId;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public ImpactLevel getImpactLevel() {
        return impactLevel;
    }
    
    public void setImpactLevel(ImpactLevel impactLevel) {
        this.impactLevel = impactLevel;
    }
    
    public List<String> getDependencyPath() {
        return dependencyPath;
    }
    
    public void setDependencyPath(List<String> dependencyPath) {
        this.dependencyPath = dependencyPath;
    }
    
    public Criticality getCriticality() {
        return criticality;
    }
    
    public void setCriticality(Criticality criticality) {
        this.criticality = criticality;
    }
    
    public List<BusinessRuleOwnershipResponse> getOwnerships() {
        return ownerships;
    }
    
    public void setOwnerships(List<BusinessRuleOwnershipResponse> ownerships) {
        this.ownerships = ownerships;
    }
    
    /**
     * Níveis de impacto na cadeia de dependências
     */
    public enum ImpactLevel {
        /**
         * Impacto direto: arquivo alterado implementa esta regra diretamente
         */
        DIRECT,
        
        /**
         * Impacto indireto: esta regra depende de uma regra diretamente impactada
         * Exemplo: Arquivo alterou BR-001, que alimenta BR-002 (BR-002 é INDIRECT)
         */
        INDIRECT,
        
        /**
         * Impacto em cascata: esta regra está a 2+ níveis de distância
         * Exemplo: Arquivo alterou BR-001 -> BR-002 -> BR-003 (BR-003 é CASCADE)
         */
        CASCADE
    }
}
