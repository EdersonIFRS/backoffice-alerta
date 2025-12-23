package com.backoffice.alerta.alerts;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Alerta gerado por análise de métricas do Gate de Risco
 * 
 * US#55 - Alertas Inteligentes Baseados em Métricas
 * 
 * IMPORTANTE:
 * - NÃO é entidade JPA (sem @Entity)
 * - NÃO é persistido em banco
 * - Gerado dinamicamente em cada consulta
 * - READ-ONLY absoluto
 */
public class RiskMetricAlert {

    private UUID id;
    private AlertType type;
    private AlertSeverity severity;
    private String message;
    private UUID projectId;
    private String businessRuleId;
    private Instant detectedAt;
    private Map<String, Object> evidence;

    public RiskMetricAlert() {
        this.id = UUID.randomUUID();
        this.detectedAt = Instant.now();
        this.evidence = new HashMap<>();
    }

    public RiskMetricAlert(AlertType type, AlertSeverity severity, String message) {
        this();
        this.type = type;
        this.severity = severity;
        this.message = message;
    }

    // Builder pattern para facilitar construção
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RiskMetricAlert alert = new RiskMetricAlert();

        public Builder type(AlertType type) {
            alert.type = type;
            return this;
        }

        public Builder severity(AlertSeverity severity) {
            alert.severity = severity;
            return this;
        }

        public Builder message(String message) {
            alert.message = message;
            return this;
        }

        public Builder projectId(UUID projectId) {
            alert.projectId = projectId;
            return this;
        }

        public Builder businessRuleId(String businessRuleId) {
            alert.businessRuleId = businessRuleId;
            return this;
        }

        public Builder evidence(String key, Object value) {
            alert.evidence.put(key, value);
            return this;
        }

        public Builder evidenceMap(Map<String, Object> evidence) {
            alert.evidence.putAll(evidence);
            return this;
        }

        public RiskMetricAlert build() {
            if (alert.type == null || alert.severity == null || alert.message == null) {
                throw new IllegalStateException("type, severity e message são obrigatórios");
            }
            return alert;
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getBusinessRuleId() {
        return businessRuleId;
    }

    public void setBusinessRuleId(String businessRuleId) {
        this.businessRuleId = businessRuleId;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }

    public Map<String, Object> getEvidence() {
        return evidence;
    }

    public void setEvidence(Map<String, Object> evidence) {
        this.evidence = evidence;
    }
}
