package com.backoffice.alerta.dashboard.dto;

import java.util.UUID;

/**
 * Resumo de risco por projeto
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
public class ProjectRiskSummary {
    
    private UUID projectId;
    private String projectName;
    private double blockRate;
    private long alertsLast30Days;
    
    public ProjectRiskSummary() {
    }
    
    public ProjectRiskSummary(UUID projectId, String projectName, double blockRate, long alertsLast30Days) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.blockRate = blockRate;
        this.alertsLast30Days = alertsLast30Days;
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
    
    public double getBlockRate() {
        return blockRate;
    }
    
    public void setBlockRate(double blockRate) {
        this.blockRate = blockRate;
    }
    
    public long getAlertsLast30Days() {
        return alertsLast30Days;
    }
    
    public void setAlertsLast30Days(long alertsLast30Days) {
        this.alertsLast30Days = alertsLast30Days;
    }
}
