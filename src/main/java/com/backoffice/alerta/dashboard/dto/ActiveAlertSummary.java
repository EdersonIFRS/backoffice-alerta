package com.backoffice.alerta.dashboard.dto;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;

/**
 * Resumo de alerta ativo
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
public class ActiveAlertSummary {
    
    private AlertType alertType;
    private AlertSeverity severity;
    private String message;
    
    public ActiveAlertSummary() {
    }
    
    public ActiveAlertSummary(AlertType alertType, AlertSeverity severity, String message) {
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
    }
    
    public AlertType getAlertType() {
        return alertType;
    }
    
    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }
    
    public AlertSeverity getSeverity() {
        return severity;
    }
    
    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
