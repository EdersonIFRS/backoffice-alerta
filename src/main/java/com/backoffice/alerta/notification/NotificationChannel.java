package com.backoffice.alerta.notification;

/**
 * Canais disponíveis para notificações organizacionais
 * 
 * Especifica por onde as notificações podem ser enviadas.
 * Na US#27 apenas geramos eventos, não enviamos mensagens reais.
 */
public enum NotificationChannel {
    
    /**
     * Notificação por email
     */
    EMAIL("Email"),
    
    /**
     * Notificação via Slack
     */
    SLACK("Slack"),
    
    /**
     * Notificação via webhook HTTP
     */
    WEBHOOK("Webhook"),
    
    /**
     * Notificação interna (dashboard)
     */
    INTERNAL("Dashboard Interno");
    
    private final String description;
    
    NotificationChannel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
