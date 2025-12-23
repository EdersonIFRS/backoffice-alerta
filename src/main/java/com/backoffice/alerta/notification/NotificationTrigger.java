package com.backoffice.alerta.notification;

/**
 * Gatilhos que disparam notificações organizacionais
 * 
 * Define em que situações notificações são geradas automaticamente.
 */
public enum NotificationTrigger {
    
    /**
     * Mudança bloqueada por alto risco
     */
    RISK_BLOCKED("Mudança Bloqueada por Risco"),
    
    /**
     * Mudança aprovada com restrições
     */
    RISK_RESTRICTED("Mudança Aprovada com Restrições"),
    
    /**
     * Alto risco detectado em produção
     */
    HIGH_RISK_PRODUCTION("Alto Risco em Produção"),
    
    /**
     * Alerta por histórico de incidentes
     */
    INCIDENT_HISTORY_ALERT("Histórico de Incidentes Críticos");
    
    private final String description;
    
    NotificationTrigger(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
