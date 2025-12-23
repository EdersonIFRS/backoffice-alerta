package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Domain;

import java.util.List;

/**
 * Representa um nó (regra de negócio) no grafo de impacto
 * 
 * Contém informações visuais necessárias para renderização:
 * - Dados da regra (id, nome, domínio, criticidade)
 * - Nível de impacto (direto/indireto/cascata) para coloração
 * - Ownership para tooltips e contexto
 * - Indicador de incidentes históricos para alertas visuais
 * 
 * US#37 - Visualização de Impacto Sistêmico (Mapa de Dependências)
 */
public class ImpactGraphNodeResponse {
    
    private String ruleId;
    private String ruleName;
    private Domain domain;
    private Criticality criticality;
    private ImpactLevel impactLevel;
    private List<OwnershipInfo> ownerships;
    private boolean hasIncidents;
    
    public ImpactGraphNodeResponse() {
    }
    
    public ImpactGraphNodeResponse(String ruleId,
                                  String ruleName,
                                  Domain domain,
                                  Criticality criticality,
                                  ImpactLevel impactLevel,
                                  List<OwnershipInfo> ownerships,
                                  boolean hasIncidents) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.domain = domain;
        this.criticality = criticality;
        this.impactLevel = impactLevel;
        this.ownerships = ownerships;
        this.hasIncidents = hasIncidents;
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
    
    public Domain getDomain() {
        return domain;
    }
    
    public void setDomain(Domain domain) {
        this.domain = domain;
    }
    
    public Criticality getCriticality() {
        return criticality;
    }
    
    public void setCriticality(Criticality criticality) {
        this.criticality = criticality;
    }
    
    public ImpactLevel getImpactLevel() {
        return impactLevel;
    }
    
    public void setImpactLevel(ImpactLevel impactLevel) {
        this.impactLevel = impactLevel;
    }
    
    public List<OwnershipInfo> getOwnerships() {
        return ownerships;
    }
    
    public void setOwnerships(List<OwnershipInfo> ownerships) {
        this.ownerships = ownerships;
    }
    
    public boolean isHasIncidents() {
        return hasIncidents;
    }
    
    public void setHasIncidents(boolean hasIncidents) {
        this.hasIncidents = hasIncidents;
    }
    
    /**
     * Níveis de impacto para coloração no grafo
     */
    public enum ImpactLevel {
        DIRECT,    // Azul - Impacto direto do arquivo alterado
        INDIRECT,  // Amarelo - 1 nível de distância
        CASCADE    // Vermelho - 2+ níveis de distância
    }
    
    /**
     * Informações simplificadas de ownership para tooltips
     */
    public static class OwnershipInfo {
        private String teamName;
        private String role;
        
        public OwnershipInfo() {
        }
        
        public OwnershipInfo(String teamName, String role) {
            this.teamName = teamName;
            this.role = role;
        }
        
        public String getTeamName() {
            return teamName;
        }
        
        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
    }
}
