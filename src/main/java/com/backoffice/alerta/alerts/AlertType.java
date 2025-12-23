package com.backoffice.alerta.alerts;

/**
 * Tipos de alerta detectados pelas métricas do Gate de Risco
 * 
 * US#55 - Alertas Inteligentes Baseados em Métricas
 */
public enum AlertType {
    /**
     * Projeto com taxa de bloqueio acima de 30%
     */
    HIGH_BLOCK_RATE_PROJECT,
    
    /**
     * Regra de negócio bloqueando excessivamente (≥5 PRs)
     */
    RULE_OVERBLOCKING,
    
    /**
     * Aumento súbito de warnings (>15% acima da média)
     */
    WARNING_SPIKE,
    
    /**
     * Tendência negativa contínua (≥3 dias de piora)
     */
    NEGATIVE_TREND,
    
    /**
     * Sistema degradado (blockRate global >25%)
     */
    SYSTEM_DEGRADATION,
    
    /**
     * Possível falso positivo (warnings altos + poucos incidentes)
     */
    POTENTIAL_FALSE_POSITIVE
}
