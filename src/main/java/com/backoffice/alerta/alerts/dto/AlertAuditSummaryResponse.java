package com.backoffice.alerta.alerts.dto;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.notification.NotificationChannel;
import java.util.List;

/**
 * DTO para resumo de auditoria da US#61
 * Responde: "Qual o panorama geral dos alertas bloqueados?"
 * 
 * READ-ONLY - Usado apenas para análise agregada
 */
public class AlertAuditSummaryResponse {
    
    private long totalAlerts;
    private long sent;
    private long skipped;
    private long failed;
    private AlertSeverity mostBlockedSeverity;
    private NotificationChannel mostBlockedChannel;
    private List<ProjectBlockedDTO> topProjectsByBlocked;
    private List<RuleBlockedDTO> topRulesByBlocked;
    
    public AlertAuditSummaryResponse() {
    }
    
    public long getTotalAlerts() {
        return totalAlerts;
    }
    
    public void setTotalAlerts(long totalAlerts) {
        this.totalAlerts = totalAlerts;
    }
    
    public long getSent() {
        return sent;
    }
    
    public void setSent(long sent) {
        this.sent = sent;
    }
    
    public long getSkipped() {
        return skipped;
    }
    
    public void setSkipped(long skipped) {
        this.skipped = skipped;
    }
    
    public long getFailed() {
        return failed;
    }
    
    public void setFailed(long failed) {
        this.failed = failed;
    }
    
    public AlertSeverity getMostBlockedSeverity() {
        return mostBlockedSeverity;
    }
    
    public void setMostBlockedSeverity(AlertSeverity mostBlockedSeverity) {
        this.mostBlockedSeverity = mostBlockedSeverity;
    }
    
    public NotificationChannel getMostBlockedChannel() {
        return mostBlockedChannel;
    }
    
    public void setMostBlockedChannel(NotificationChannel mostBlockedChannel) {
        this.mostBlockedChannel = mostBlockedChannel;
    }
    
    public List<ProjectBlockedDTO> getTopProjectsByBlocked() {
        return topProjectsByBlocked;
    }
    
    public void setTopProjectsByBlocked(List<ProjectBlockedDTO> topProjectsByBlocked) {
        this.topProjectsByBlocked = topProjectsByBlocked;
    }
    
    public List<RuleBlockedDTO> getTopRulesByBlocked() {
        return topRulesByBlocked;
    }
    
    public void setTopRulesByBlocked(List<RuleBlockedDTO> topRulesByBlocked) {
        this.topRulesByBlocked = topRulesByBlocked;
    }
    
    // Nested DTOs
    public static class ProjectBlockedDTO {
        private Long projectId;
        private String projectName;
        private long blockedCount;
        
        public ProjectBlockedDTO() {}
        
        public ProjectBlockedDTO(Long projectId, String projectName, long blockedCount) {
            this.projectId = projectId;
            this.projectName = projectName;
            this.blockedCount = blockedCount;
        }
        
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public long getBlockedCount() { return blockedCount; }
        public void setBlockedCount(long blockedCount) { this.blockedCount = blockedCount; }
    }
    
    public static class RuleBlockedDTO {
        private String ruleId;
        private String ruleName;
        private long blockedCount;
        
        public RuleBlockedDTO() {}
        
        public RuleBlockedDTO(String ruleId, String ruleName, long blockedCount) {
            this.ruleId = ruleId;
            this.ruleName = ruleName;
            this.blockedCount = blockedCount;
        }
        
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public long getBlockedCount() { return blockedCount; }
        public void setBlockedCount(long blockedCount) { this.blockedCount = blockedCount; }
    }
}
