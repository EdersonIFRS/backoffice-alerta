package com.backoffice.alerta.dto;

/**
 * Indicador de tendência detectado na análise de métricas
 * 
 * Representa padrões identificados como crescimento de falsos positivos/negativos,
 * regras problemáticas, ambientes com alta taxa de erro, etc.
 */
public class TrendIndicator {
    
    public enum TrendType {
        FALSE_POSITIVE_INCREASE,      // Crescimento de falsos positivos
        FALSE_NEGATIVE_INCREASE,      // Crescimento de falsos negativos
        HIGH_INCIDENT_RATE,           // Alta taxa de incidentes
        PROBLEMATIC_BUSINESS_RULE,    // Regra de negócio problemática
        ENVIRONMENT_DEGRADATION,      // Degradação de qualidade por ambiente
        ACCURACY_IMPROVEMENT,         // Melhoria na acurácia
        LOW_CONFIDENCE_PERIOD         // Período de baixa confiança
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    private final TrendType type;
    private final Severity severity;
    private final String description;
    private final String affectedEntity;  // businessRuleId, environment, etc.
    private final int evidenceCount;      // Quantidade de evidências
    private final double impactRate;      // Taxa de impacto (0-100)

    public TrendIndicator(TrendType type,
                         Severity severity,
                         String description,
                         String affectedEntity,
                         int evidenceCount,
                         double impactRate) {
        this.type = type;
        this.severity = severity;
        this.description = description;
        this.affectedEntity = affectedEntity;
        this.evidenceCount = evidenceCount;
        this.impactRate = Math.min(100.0, Math.max(0.0, impactRate));
    }

    public TrendType getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public String getAffectedEntity() {
        return affectedEntity;
    }

    public int getEvidenceCount() {
        return evidenceCount;
    }

    public double getImpactRate() {
        return impactRate;
    }
}
