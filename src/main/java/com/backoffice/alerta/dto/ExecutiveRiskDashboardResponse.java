package com.backoffice.alerta.dto;

import java.util.List;

/**
 * Response consolidado do dashboard executivo de risco
 * 
 * Agrega indicadores de decisões, métricas, alertas e recomendações
 * para análise executiva de acurácia e confiabilidade do sistema.
 * 
 * IMPORTANTE: 100% READ-ONLY
 * - Não modifica decisões de risco
 * - Não recalcula scores passados
 * - Não altera auditorias, feedbacks ou métricas
 * - Apenas consolida dados existentes
 */
public class ExecutiveRiskDashboardResponse {
    
    public enum ConfidenceStatus {
        EXCELLENT,      // >= 90%
        HEALTHY,        // 80-89%
        ATTENTION,      // 65-79%
        CRITICAL        // < 65%
    }

    private final PeriodSummary period;
    private final double systemConfidenceScore;     // 0-100
    private final ConfidenceStatus confidenceStatus;
    private final DashboardSummary summary;
    private final KeyRiskIndicators keyIndicators;
    private final List<TopRiskDriverSummary> topRiskDrivers;
    private final List<DashboardAlert> alerts;
    private final String recommendation;            // Texto determinístico baseado em status

    public ExecutiveRiskDashboardResponse(PeriodSummary period,
                                         double systemConfidenceScore,
                                         ConfidenceStatus confidenceStatus,
                                         DashboardSummary summary,
                                         KeyRiskIndicators keyIndicators,
                                         List<TopRiskDriverSummary> topRiskDrivers,
                                         List<DashboardAlert> alerts,
                                         String recommendation) {
        this.period = period;
        this.systemConfidenceScore = Math.min(100.0, Math.max(0.0, systemConfidenceScore));
        this.confidenceStatus = confidenceStatus;
        this.summary = summary;
        this.keyIndicators = keyIndicators;
        this.topRiskDrivers = topRiskDrivers;
        this.alerts = alerts;
        this.recommendation = recommendation;
    }

    public PeriodSummary getPeriod() {
        return period;
    }

    public double getSystemConfidenceScore() {
        return systemConfidenceScore;
    }

    public ConfidenceStatus getConfidenceStatus() {
        return confidenceStatus;
    }

    public DashboardSummary getSummary() {
        return summary;
    }

    public KeyRiskIndicators getKeyIndicators() {
        return keyIndicators;
    }

    public List<TopRiskDriverSummary> getTopRiskDrivers() {
        return topRiskDrivers;
    }

    public List<DashboardAlert> getAlerts() {
        return alerts;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
