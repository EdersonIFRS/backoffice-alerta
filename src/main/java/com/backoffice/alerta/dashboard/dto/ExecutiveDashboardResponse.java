package com.backoffice.alerta.dashboard.dto;

import java.util.List;

/**
 * Response consolidada do dashboard executivo
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
public class ExecutiveDashboardResponse {
    
    private ExecutiveDashboardSummary summary;
    private List<ProjectRiskSummary> topProjects;
    private List<RuleRiskSummary> topRules;
    private List<AlertTrendPoint> alertTrends;
    private List<ActiveAlertSummary> activeAlerts;
    
    public ExecutiveDashboardResponse() {
    }
    
    public ExecutiveDashboardResponse(ExecutiveDashboardSummary summary, List<ProjectRiskSummary> topProjects,
                                     List<RuleRiskSummary> topRules, List<AlertTrendPoint> alertTrends,
                                     List<ActiveAlertSummary> activeAlerts) {
        this.summary = summary;
        this.topProjects = topProjects;
        this.topRules = topRules;
        this.alertTrends = alertTrends;
        this.activeAlerts = activeAlerts;
    }
    
    public ExecutiveDashboardSummary getSummary() {
        return summary;
    }
    
    public void setSummary(ExecutiveDashboardSummary summary) {
        this.summary = summary;
    }
    
    public List<ProjectRiskSummary> getTopProjects() {
        return topProjects;
    }
    
    public void setTopProjects(List<ProjectRiskSummary> topProjects) {
        this.topProjects = topProjects;
    }
    
    public List<RuleRiskSummary> getTopRules() {
        return topRules;
    }
    
    public void setTopRules(List<RuleRiskSummary> topRules) {
        this.topRules = topRules;
    }
    
    public List<AlertTrendPoint> getAlertTrends() {
        return alertTrends;
    }
    
    public void setAlertTrends(List<AlertTrendPoint> alertTrends) {
        this.alertTrends = alertTrends;
    }
    
    public List<ActiveAlertSummary> getActiveAlerts() {
        return activeAlerts;
    }
    
    public void setActiveAlerts(List<ActiveAlertSummary> activeAlerts) {
        this.activeAlerts = activeAlerts;
    }
}
