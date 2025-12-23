package com.backoffice.alerta.dashboard.dto;

/**
 * Resumo executivo do dashboard de risco
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
public class ExecutiveDashboardSummary {
    
    private long totalGates;
    private double blockRate;
    private double warningRate;
    private long criticalAlertsLast7Days;
    private boolean alertFatigueDetected;
    
    public ExecutiveDashboardSummary() {
    }
    
    public ExecutiveDashboardSummary(long totalGates, double blockRate, double warningRate, 
                                    long criticalAlertsLast7Days, boolean alertFatigueDetected) {
        this.totalGates = totalGates;
        this.blockRate = blockRate;
        this.warningRate = warningRate;
        this.criticalAlertsLast7Days = criticalAlertsLast7Days;
        this.alertFatigueDetected = alertFatigueDetected;
    }
    
    public long getTotalGates() {
        return totalGates;
    }
    
    public void setTotalGates(long totalGates) {
        this.totalGates = totalGates;
    }
    
    public double getBlockRate() {
        return blockRate;
    }
    
    public void setBlockRate(double blockRate) {
        this.blockRate = blockRate;
    }
    
    public double getWarningRate() {
        return warningRate;
    }
    
    public void setWarningRate(double warningRate) {
        this.warningRate = warningRate;
    }
    
    public long getCriticalAlertsLast7Days() {
        return criticalAlertsLast7Days;
    }
    
    public void setCriticalAlertsLast7Days(long criticalAlertsLast7Days) {
        this.criticalAlertsLast7Days = criticalAlertsLast7Days;
    }
    
    public boolean isAlertFatigueDetected() {
        return alertFatigueDetected;
    }
    
    public void setAlertFatigueDetected(boolean alertFatigueDetected) {
        this.alertFatigueDetected = alertFatigueDetected;
    }
}
