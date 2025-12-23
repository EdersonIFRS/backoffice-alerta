package com.backoffice.alerta.dto;

/**
 * Alerta gerado no dashboard executivo baseado em regras determinísticas
 * 
 * Read-only, sem mutação de estado
 */
public class DashboardAlert {
    
    public enum AlertType {
        HIGH_INCIDENT_RATE,           // Taxa de incidentes após aprovação > 3%
        FALSE_NEGATIVE_DOMINANCE,     // Falsos negativos > falsos positivos
        CRITICAL_CONFIDENCE,          // Confiança do sistema crítica (< 65%)
        PROBLEMATIC_RULE,             // Regra específica com múltiplos problemas
        ENVIRONMENT_DEGRADATION       // Ambiente específico com degradação
    }

    public enum AlertSeverity {
        INFO,
        WARNING,
        CRITICAL
    }

    private final AlertType type;
    private final AlertSeverity severity;
    private final String message;
    private final String affectedEntity;    // businessRuleId, environment, etc.
    private final double impactRate;        // 0-100

    public DashboardAlert(AlertType type, 
                         AlertSeverity severity, 
                         String message, 
                         String affectedEntity, 
                         double impactRate) {
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.affectedEntity = affectedEntity;
        this.impactRate = Math.min(100.0, Math.max(0.0, impactRate));
    }

    public AlertType getType() {
        return type;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String getAffectedEntity() {
        return affectedEntity;
    }

    public double getImpactRate() {
        return impactRate;
    }
}
