package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.RiskLevel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response contendo métricas de acurácia e confiabilidade do sistema de risco
 * 
 * Agrega dados de auditorias, feedbacks e incidentes para avaliar performance
 * das decisões de risco ao longo do tempo.
 * 
 * READ-ONLY: Não modifica decisões, riscos ou regras existentes
 */
public class RiskMetricsResponse {
    
    // Contadores gerais
    private final int totalDecisions;
    private final int totalDeploys;
    private final int totalIncidents;
    private final int approvedCount;
    private final int approvedWithRestrictionsCount;
    private final int blockedCount;

    // Métricas de acurácia (0-100)
    private final double accuracyRate;
    private final double falsePositiveRate;
    private final double falseNegativeRate;
    private final double incidentAfterApprovalRate;
    private final double safeChangeBlockedRate;

    // Métricas por nível de risco
    private final Map<RiskLevel, RiskLevelMetrics> metricsByRiskLevel;

    // Tendências detectadas
    private final List<TrendIndicator> trendIndicators;

    // Score de confiança do sistema (0-100)
    private final double systemConfidenceScore;

    // Período analisado
    private final LocalDate periodStart;
    private final LocalDate periodEnd;

    // Filtros aplicados
    private final String appliedFilters;

    public RiskMetricsResponse(int totalDecisions,
                              int totalDeploys,
                              int totalIncidents,
                              int approvedCount,
                              int approvedWithRestrictionsCount,
                              int blockedCount,
                              double accuracyRate,
                              double falsePositiveRate,
                              double falseNegativeRate,
                              double incidentAfterApprovalRate,
                              double safeChangeBlockedRate,
                              Map<RiskLevel, RiskLevelMetrics> metricsByRiskLevel,
                              List<TrendIndicator> trendIndicators,
                              double systemConfidenceScore,
                              LocalDate periodStart,
                              LocalDate periodEnd,
                              String appliedFilters) {
        this.totalDecisions = totalDecisions;
        this.totalDeploys = totalDeploys;
        this.totalIncidents = totalIncidents;
        this.approvedCount = approvedCount;
        this.approvedWithRestrictionsCount = approvedWithRestrictionsCount;
        this.blockedCount = blockedCount;
        this.accuracyRate = Math.min(100.0, Math.max(0.0, accuracyRate));
        this.falsePositiveRate = Math.min(100.0, Math.max(0.0, falsePositiveRate));
        this.falseNegativeRate = Math.min(100.0, Math.max(0.0, falseNegativeRate));
        this.incidentAfterApprovalRate = Math.min(100.0, Math.max(0.0, incidentAfterApprovalRate));
        this.safeChangeBlockedRate = Math.min(100.0, Math.max(0.0, safeChangeBlockedRate));
        this.metricsByRiskLevel = metricsByRiskLevel;
        this.trendIndicators = trendIndicators;
        this.systemConfidenceScore = Math.min(100.0, Math.max(0.0, systemConfidenceScore));
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.appliedFilters = appliedFilters;
    }

    public int getTotalDecisions() {
        return totalDecisions;
    }

    public int getTotalDeploys() {
        return totalDeploys;
    }

    public int getTotalIncidents() {
        return totalIncidents;
    }

    public int getApprovedCount() {
        return approvedCount;
    }

    public int getApprovedWithRestrictionsCount() {
        return approvedWithRestrictionsCount;
    }

    public int getBlockedCount() {
        return blockedCount;
    }

    public double getAccuracyRate() {
        return accuracyRate;
    }

    public double getFalsePositiveRate() {
        return falsePositiveRate;
    }

    public double getFalseNegativeRate() {
        return falseNegativeRate;
    }

    public double getIncidentAfterApprovalRate() {
        return incidentAfterApprovalRate;
    }

    public double getSafeChangeBlockedRate() {
        return safeChangeBlockedRate;
    }

    public Map<RiskLevel, RiskLevelMetrics> getMetricsByRiskLevel() {
        return metricsByRiskLevel;
    }

    public List<TrendIndicator> getTrendIndicators() {
        return trendIndicators;
    }

    public double getSystemConfidenceScore() {
        return systemConfidenceScore;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public String getAppliedFilters() {
        return appliedFilters;
    }
}
