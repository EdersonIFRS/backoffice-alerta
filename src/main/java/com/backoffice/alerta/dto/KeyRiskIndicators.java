package com.backoffice.alerta.dto;

/**
 * Indicadores-chave de risco (KRIs) para o dashboard executivo
 * 
 * Métricas consolidadas de acurácia e confiabilidade
 */
public class KeyRiskIndicators {
    
    private final double accuracyRate;              // 0-100
    private final double falsePositiveRate;         // 0-100
    private final double falseNegativeRate;         // 0-100
    private final double safeChangeBlockedRate;     // 0-100
    private final double incidentAfterApprovalRate; // 0-100

    public KeyRiskIndicators(double accuracyRate,
                            double falsePositiveRate,
                            double falseNegativeRate,
                            double safeChangeBlockedRate,
                            double incidentAfterApprovalRate) {
        this.accuracyRate = Math.min(100.0, Math.max(0.0, accuracyRate));
        this.falsePositiveRate = Math.min(100.0, Math.max(0.0, falsePositiveRate));
        this.falseNegativeRate = Math.min(100.0, Math.max(0.0, falseNegativeRate));
        this.safeChangeBlockedRate = Math.min(100.0, Math.max(0.0, safeChangeBlockedRate));
        this.incidentAfterApprovalRate = Math.min(100.0, Math.max(0.0, incidentAfterApprovalRate));
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

    public double getSafeChangeBlockedRate() {
        return safeChangeBlockedRate;
    }

    public double getIncidentAfterApprovalRate() {
        return incidentAfterApprovalRate;
    }
}
