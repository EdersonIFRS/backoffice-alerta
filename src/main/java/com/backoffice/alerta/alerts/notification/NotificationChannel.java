package com.backoffice.alerta.alerts.notification;

/**
 * Canais de notificação suportados para alertas de risco
 * 
 * US#56 - Alertas Inteligentes via Slack / Microsoft Teams
 */
public enum NotificationChannel {
    /**
     * Slack (Incoming Webhook)
     */
    SLACK,
    
    /**
     * Microsoft Teams (Incoming Webhook)
     */
    TEAMS
}
