package com.backoffice.alerta.alerts.dto;

import com.backoffice.alerta.alerts.AlertType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.Map;

/**
 * Response DTO para resumo executivo de alertas
 * 
 * US#55 - Alertas Inteligentes Baseados em Métricas
 * 
 * READ-ONLY: Apenas retorna estatísticas agregadas, sem side-effects.
 */
@Schema(description = "Resumo executivo de alertas de métricas do Gate de Risco")
public class RiskMetricAlertSummaryResponse {

    @Schema(description = "Total de alertas detectados", example = "12")
    private int totalAlerts;

    @Schema(description = "Quantidade de alertas críticos", example = "3")
    private int criticalCount;

    @Schema(description = "Quantidade de alertas de warning", example = "7")
    private int warningCount;

    @Schema(description = "Quantidade de alertas informativos", example = "2")
    private int infoCount;

    @Schema(description = "Distribuição de alertas por tipo",
            example = "{\"HIGH_BLOCK_RATE_PROJECT\": 2, \"RULE_OVERBLOCKING\": 3, \"SYSTEM_DEGRADATION\": 1}")
    private Map<AlertType, Integer> alertsByType = new HashMap<>();

    @Schema(description = "Distribuição de alertas por projeto",
            example = "{\"Backoffice Pagamentos\": 5, \"Portal do Cliente\": 3}")
    private Map<String, Integer> alertsByProject = new HashMap<>();

    @Schema(description = "Status geral do sistema", example = "DEGRADED")
    private String healthStatus;

    // Constructors
    public RiskMetricAlertSummaryResponse() {}

    public RiskMetricAlertSummaryResponse(int totalAlerts, int criticalCount, int warningCount, int infoCount) {
        this.totalAlerts = totalAlerts;
        this.criticalCount = criticalCount;
        this.warningCount = warningCount;
        this.infoCount = infoCount;
    }

    // Getters and Setters
    public int getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(int totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public int getCriticalCount() {
        return criticalCount;
    }

    public void setCriticalCount(int criticalCount) {
        this.criticalCount = criticalCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public int getInfoCount() {
        return infoCount;
    }

    public void setInfoCount(int infoCount) {
        this.infoCount = infoCount;
    }

    public Map<AlertType, Integer> getAlertsByType() {
        return alertsByType;
    }

    public void setAlertsByType(Map<AlertType, Integer> alertsByType) {
        this.alertsByType = alertsByType;
    }

    public Map<String, Integer> getAlertsByProject() {
        return alertsByProject;
    }

    public void setAlertsByProject(Map<String, Integer> alertsByProject) {
        this.alertsByProject = alertsByProject;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }
}
