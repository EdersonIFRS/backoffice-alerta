package com.backoffice.alerta.rules;

/**
 * Resultado do feedback humano pós-deploy
 * Captura o que realmente aconteceu após a decisão de risco
 */
public enum FeedbackOutcome {
    
    /**
     * Deploy ocorreu sem problemas
     */
    SUCCESS,
    
    /**
     * Pequenos problemas identificados, mas sem necessidade de rollback
     */
    MINOR_ISSUES,
    
    /**
     * Incidente em produção relacionado à mudança
     */
    INCIDENT,
    
    /**
     * Necessidade de rollback da mudança
     */
    ROLLBACK,
    
    /**
     * Risco foi superestimado - decisão foi muito conservadora
     */
    FALSE_POSITIVE_RISK,
    
    /**
     * Risco foi subestimado - decisão foi muito permissiva
     */
    FALSE_NEGATIVE_RISK
}
