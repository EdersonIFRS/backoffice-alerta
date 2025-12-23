package com.backoffice.alerta.alerts.notification.dto;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.alerts.notification.NotificationChannel;
import com.backoffice.alerta.alerts.notification.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta para histórico de notificação de alerta
 * 
 * US#59 - Histórico e Rastreabilidade de Notificações de Alerta
 */
public class RiskAlertNotificationHistoryResponse {
    
    private UUID id;
    private AlertType alertType;
    private AlertSeverity severity;
    private NotificationChannel channel;
    private NotificationStatus status;
    private UUID projectId;
    private String projectName;
    private String businessRuleId;
    private String businessRuleName;
    private String messageSummary;
    private String deliveryReason;
    private String recipient;
    private Instant createdAt;
    private String createdBy;
    
    // Constructors
    
    public RiskAlertNotificationHistoryResponse() {
    }
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
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
    
    public NotificationChannel getChannel() {
        return channel;
    }
    
    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }
    
    public NotificationStatus getStatus() {
        return status;
    }
    
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getBusinessRuleId() {
        return businessRuleId;
    }
    
    public void setBusinessRuleId(String businessRuleId) {
        this.businessRuleId = businessRuleId;
    }
    
    public String getBusinessRuleName() {
        return businessRuleName;
    }
    
    public void setBusinessRuleName(String businessRuleName) {
        this.businessRuleName = businessRuleName;
    }
    
    public String getMessageSummary() {
        return messageSummary;
    }
    
    public void setMessageSummary(String messageSummary) {
        this.messageSummary = messageSummary;
    }
    
    public String getDeliveryReason() {
        return deliveryReason;
    }
    
    public void setDeliveryReason(String deliveryReason) {
        this.deliveryReason = deliveryReason;
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
