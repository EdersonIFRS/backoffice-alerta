package com.backoffice.alerta.alerts.notification.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO de resumo executivo do histórico de notificações
 * 
 * US#59 - Histórico e Rastreabilidade de Notificações de Alerta
 */
public class RiskAlertNotificationHistorySummaryResponse {
    
    private long totalSent;
    private long totalSkipped;
    private long totalFailed;
    private long totalCritical;
    private List<Map<String, Object>> topProjects;
    private List<Map<String, Object>> topRules;
    
    // Constructors
    
    public RiskAlertNotificationHistorySummaryResponse() {
    }
    
    public RiskAlertNotificationHistorySummaryResponse(
            long totalSent, 
            long totalSkipped, 
            long totalFailed, 
            long totalCritical,
            List<Map<String, Object>> topProjects,
            List<Map<String, Object>> topRules) {
        this.totalSent = totalSent;
        this.totalSkipped = totalSkipped;
        this.totalFailed = totalFailed;
        this.totalCritical = totalCritical;
        this.topProjects = topProjects;
        this.topRules = topRules;
    }
    
    // Getters and Setters
    
    public long getTotalSent() {
        return totalSent;
    }
    
    public void setTotalSent(long totalSent) {
        this.totalSent = totalSent;
    }
    
    public long getTotalSkipped() {
        return totalSkipped;
    }
    
    public void setTotalSkipped(long totalSkipped) {
        this.totalSkipped = totalSkipped;
    }
    
    public long getTotalFailed() {
        return totalFailed;
    }
    
    public void setTotalFailed(long totalFailed) {
        this.totalFailed = totalFailed;
    }
    
    public long getTotalCritical() {
        return totalCritical;
    }
    
    public void setTotalCritical(long totalCritical) {
        this.totalCritical = totalCritical;
    }
    
    public List<Map<String, Object>> getTopProjects() {
        return topProjects;
    }
    
    public void setTopProjects(List<Map<String, Object>> topProjects) {
        this.topProjects = topProjects;
    }
    
    public List<Map<String, Object>> getTopRules() {
        return topRules;
    }
    
    public void setTopRules(List<Map<String, Object>> topRules) {
        this.topRules = topRules;
    }
}
