package com.backoffice.alerta.alerts.notification;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Histórico de notificações de alertas enviadas
 * 
 * US#59 - Histórico e Rastreabilidade de Notificações de Alerta
 * 
 * Registra TODAS as tentativas de notificação (SENT, SKIPPED, FAILED)
 * para auditoria e análise executiva.
 */
@Entity
@Table(name = "risk_alert_notification_history")
public class RiskAlertNotificationHistory {
    
    @Id
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;
    
    @Column(name = "project_id")
    private UUID projectId;
    
    @Column(name = "project_name")
    private String projectName;
    
    @Column(name = "business_rule_id")
    private String businessRuleId;
    
    @Column(name = "business_rule_name")
    private String businessRuleName;
    
    @Column(name = "message_summary", nullable = false)
    private String messageSummary;
    
    @Column(name = "delivery_reason", nullable = false)
    private String deliveryReason;
    
    @Column(name = "recipient")
    private String recipient;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    // Constructors
    
    public RiskAlertNotificationHistory() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
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
