package com.backoffice.alerta.alerts;

/**
 * Severidade de alertas de métricas
 * 
 * US#55 - Alertas Inteligentes Baseados em Métricas
 */
public enum AlertSeverity {
    /**
     * Informativo - monitoramento de tendências
     */
    INFO,
    
    /**
     * Aviso - requer atenção
     */
    WARNING,
    
    /**
     * Crítico - ação imediata recomendada
     */
    CRITICAL
}
