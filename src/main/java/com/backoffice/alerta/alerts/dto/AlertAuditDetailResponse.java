package com.backoffice.alerta.alerts.dto;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.alerts.notification.NotificationChannel;
import com.backoffice.alerta.alerts.notification.NotificationStatus;
import java.time.LocalDateTime;

/**
 * DTO para detalhamento completo de auditoria da US#61
 * Responde: "Por que este alerta foi enviado/bloqueado?"
 * 
 * READ-ONLY - Usado apenas para consulta
 */
public class AlertAuditDetailResponse {
    
    private Long alertHistoryId;
    private AlertType alertType;
    private AlertSeverity severity;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String messageSummary;
    private String deliveryReason;
    private LocalDateTime createdAt;
    private String createdBy;
    
    // Entidades relacionadas
    private ProjectSummaryDTO project;
    private BusinessRuleSummaryDTO businessRule;
    
    // Preferência resolvida
    private ResolvedPreferenceDTO resolvedPreference;
    
    // Explicação em linguagem humana
    private String explanation;
    
    // Flags de compliance
    private ComplianceFlagsDTO complianceFlags;
    
    public AlertAuditDetailResponse() {
    }
    
    public Long getAlertHistoryId() {
        return alertHistoryId;
    }
    
    public void setAlertHistoryId(Long alertHistoryId) {
        this.alertHistoryId = alertHistoryId;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public ProjectSummaryDTO getProject() {
        return project;
    }
    
    public void setProject(ProjectSummaryDTO project) {
        this.project = project;
    }
    
    public BusinessRuleSummaryDTO getBusinessRule() {
        return businessRule;
    }
    
    public void setBusinessRule(BusinessRuleSummaryDTO businessRule) {
        this.businessRule = businessRule;
    }
    
    public ResolvedPreferenceDTO getResolvedPreference() {
        return resolvedPreference;
    }
    
    public void setResolvedPreference(ResolvedPreferenceDTO resolvedPreference) {
        this.resolvedPreference = resolvedPreference;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public ComplianceFlagsDTO getComplianceFlags() {
        return complianceFlags;
    }
    
    public void setComplianceFlags(ComplianceFlagsDTO complianceFlags) {
        this.complianceFlags = complianceFlags;
    }
    
    // Nested DTOs
    public static class ProjectSummaryDTO {
        private Long id;
        private String name;
        
        public ProjectSummaryDTO() {}
        
        public ProjectSummaryDTO(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    public static class BusinessRuleSummaryDTO {
        private String id;
        private String name;
        
        public BusinessRuleSummaryDTO() {}
        
        public BusinessRuleSummaryDTO(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
