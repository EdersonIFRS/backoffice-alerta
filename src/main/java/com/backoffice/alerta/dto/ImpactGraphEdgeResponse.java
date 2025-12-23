package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.BusinessRuleDependencyType;

/**
 * Representa uma aresta (dependência) no grafo de impacto
 * 
 * Define relacionamento direcional entre duas regras:
 * - sourceRuleId → targetRuleId
 * - Tipo de dependência (FEEDS, DEPENDS_ON, etc)
 * 
 * Usado para desenhar setas/conexões no grafo visual.
 * 
 * US#37 - Visualização de Impacto Sistêmico (Mapa de Dependências)
 */
public class ImpactGraphEdgeResponse {
    
    private String sourceRuleId;
    private String targetRuleId;
    private BusinessRuleDependencyType dependencyType;
    
    public ImpactGraphEdgeResponse() {
    }
    
    public ImpactGraphEdgeResponse(String sourceRuleId,
                                  String targetRuleId,
                                  BusinessRuleDependencyType dependencyType) {
        this.sourceRuleId = sourceRuleId;
        this.targetRuleId = targetRuleId;
        this.dependencyType = dependencyType;
    }
    
    public String getSourceRuleId() {
        return sourceRuleId;
    }
    
    public void setSourceRuleId(String sourceRuleId) {
        this.sourceRuleId = sourceRuleId;
    }
    
    public String getTargetRuleId() {
        return targetRuleId;
    }
    
    public void setTargetRuleId(String targetRuleId) {
        this.targetRuleId = targetRuleId;
    }
    
    public BusinessRuleDependencyType getDependencyType() {
        return dependencyType;
    }
    
    public void setDependencyType(BusinessRuleDependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }
}
