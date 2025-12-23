package com.backoffice.alerta.alerts.notification;

/**
 * Status de envio de notificação de alerta
 * 
 * US#56 - Alertas Inteligentes via Slack / Microsoft Teams
 */
public enum NotificationStatus {
    /**
     * Notificação enviada com sucesso
     */
    SENT,
    
    /**
     * Falha no envio da notificação
     */
    FAILED,
    
    /**
     * Notificação ignorada (ex: severidade baixa, webhook desabilitado)
     */
    SKIPPED
}
