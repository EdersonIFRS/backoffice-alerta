package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.RiskLevel;

/**
 * Métricas agregadas por nível de risco (BAIXA, MEDIA, ALTA, CRITICA)
 * 
 * Contém estatísticas de decisões, deploys, incidentes e acurácia por nível
 */
public class RiskLevelMetrics {
    
    private final RiskLevel riskLevel;
    private final int totalDecisions;
    private final int approvedCount;
    private final int blockedCount;
    private final int deploysWithIncidents;
    private final int deploysWithSuccess;
    private final double accuracyRate;
    private final double falsePositiveRate;
    private final double falseNegativeRate;

    public RiskLevelMetrics(RiskLevel riskLevel,
                           int totalDecisions,
                           int approvedCount,
                           int blockedCount,
                           int deploysWithIncidents,
                           int deploysWithSuccess,
                           double accuracyRate,
                           double falsePositiveRate,
                           double falseNegativeRate) {
        this.riskLevel = riskLevel;
        this.totalDecisions = totalDecisions;
        this.approvedCount = approvedCount;
        this.blockedCount = blockedCount;
        this.deploysWithIncidents = deploysWithIncidents;
        this.deploysWithSuccess = deploysWithSuccess;
        this.accuracyRate = Math.min(100.0, Math.max(0.0, accuracyRate));
        this.falsePositiveRate = Math.min(100.0, Math.max(0.0, falsePositiveRate));
        this.falseNegativeRate = Math.min(100.0, Math.max(0.0, falseNegativeRate));
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public int getTotalDecisions() {
        return totalDecisions;
    }

    public int getApprovedCount() {
        return approvedCount;
    }

    public int getBlockedCount() {
        return blockedCount;
    }

    public int getDeploysWithIncidents() {
        return deploysWithIncidents;
    }

    public int getDeploysWithSuccess() {
        return deploysWithSuccess;
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
}
