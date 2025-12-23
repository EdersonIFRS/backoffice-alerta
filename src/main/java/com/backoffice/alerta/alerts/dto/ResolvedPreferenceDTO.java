package com.backoffice.alerta.alerts.dto;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.alerts.notification.NotificationChannel;
import java.util.Set;

/**
 * DTO para preferência resolvida da US#61 - Auditoria de Alertas
 * Mostra qual preferência foi efetivamente aplicada (REGRA / PROJETO / DEFAULT)
 * 
 * READ-ONLY - Usado apenas para auditoria
 */
public class ResolvedPreferenceDTO {
    
    private String source; // RULE, PROJECT, DEFAULT
    private AlertSeverity minimumSeverity;
    private Set<AlertType> allowedAlertTypes;
    private Set<NotificationChannel> allowedChannels;
    private String deliveryWindow;
    
    public ResolvedPreferenceDTO() {
    }
    
    public ResolvedPreferenceDTO(String source, AlertSeverity minimumSeverity,
                                 Set<AlertType> allowedAlertTypes, 
                                 Set<NotificationChannel> allowedChannels,
                                 String deliveryWindow) {
        this.source = source;
        this.minimumSeverity = minimumSeverity;
        this.allowedAlertTypes = allowedAlertTypes;
        this.allowedChannels = allowedChannels;
        this.deliveryWindow = deliveryWindow;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public AlertSeverity getMinimumSeverity() {
        return minimumSeverity;
    }
    
    public void setMinimumSeverity(AlertSeverity minimumSeverity) {
        this.minimumSeverity = minimumSeverity;
    }
    
    public Set<AlertType> getAllowedAlertTypes() {
        return allowedAlertTypes;
    }
    
    public void setAllowedAlertTypes(Set<AlertType> allowedAlertTypes) {
        this.allowedAlertTypes = allowedAlertTypes;
    }
    
    public Set<NotificationChannel> getAllowedChannels() {
        return allowedChannels;
    }
    
    public void setAllowedChannels(Set<NotificationChannel> allowedChannels) {
        this.allowedChannels = allowedChannels;
    }
    
    public String getDeliveryWindow() {
        return deliveryWindow;
    }
    
    public void setDeliveryWindow(String deliveryWindow) {
        this.deliveryWindow = deliveryWindow;
    }
}
