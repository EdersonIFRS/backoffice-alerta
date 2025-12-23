package com.backoffice.alerta.notification;

/**
 * Severidade/urgência de uma notificação organizacional
 * 
 * Define a prioridade da notificação para os times responsáveis.
 */
public enum NotificationSeverity {
    
    /**
     * Informativa - baixa urgência
     */
    INFO("Informação"),
    
    /**
     * Alerta - requer atenção
     */
    WARNING("Alerta"),
    
    /**
     * Crítica - requer ação imediata
     */
    CRITICAL("Crítica");
    
    private final String description;
    
    NotificationSeverity(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
