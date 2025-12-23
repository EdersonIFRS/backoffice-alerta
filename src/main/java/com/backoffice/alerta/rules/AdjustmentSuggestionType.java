package com.backoffice.alerta.rules;

/**
 * Tipo de ajuste sugerido para melhoria de decisões de risco
 * Baseado em aprendizado organizacional através de feedbacks e incidentes
 */
public enum AdjustmentSuggestionType {
    
    /**
     * Sugestão de ajuste de peso de risco de uma regra de negócio
     */
    RISK_WEIGHT,
    
    /**
     * Sugestão de mudança de nível de criticidade de uma regra
     */
    CRITICALITY_LEVEL,
    
    /**
     * Sugestão de ajuste em política de aprovação
     */
    APPROVAL_POLICY,
    
    /**
     * Sugestão de ajuste em threshold de cobertura de testes
     */
    TEST_COVERAGE_THRESHOLD
}
