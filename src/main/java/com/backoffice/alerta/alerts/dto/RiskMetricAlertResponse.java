package com.backoffice.alerta.alerts.dto;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.rag.OwnershipSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO para alerta de métrica do Gate de Risco
 * 
 * US#55 - Alertas Inteligentes Baseados em Métricas
 * 
 * READ-ONLY: Apenas retorna alertas detectados, sem side-effects.
 */
@Schema(description = "Alerta gerado por análise de métricas do Gate de Risco")
public class RiskMetricAlertResponse {

    @Schema(description = "ID único do alerta (gerado dinamicamente)", example = "550e8400-e29b-41d4-a716-446655440099")
    private UUID id;

    @Schema(description = "Tipo do alerta detectado", example = "HIGH_BLOCK_RATE_PROJECT")
    private AlertType type;

    @Schema(description = "Severidade do alerta", example = "CRITICAL")
    private AlertSeverity severity;

    @Schema(description = "Mensagem explicativa do alerta", 
            example = "Projeto 'Backoffice Pagamentos' apresenta taxa de bloqueio crítica (35.7%)")
    private String message;

    @Schema(description = "Contexto do projeto (se aplicável)")
    private ProjectContext projectContext;

    @Schema(description = "ID da regra de negócio (se aplicável)", 
            example = "550e8400-e29b-41d4-a716-446655440001")
    private String businessRuleId;

    @Schema(description = "Nome da regra de negócio (se aplicável)", 
            example = "REGRA_CALCULO_HORAS_PJ")
    private String businessRuleName;

    @Schema(description = "Times/pessoas responsáveis (ownership)")
    private List<OwnershipSummary> ownerships = new ArrayList<>();

    @Schema(description = "Momento da detecção do alerta", example = "2025-12-20T19:30:00Z")
    private Instant detectedAt;

    @Schema(description = "Evidências numéricas que geraram o alerta",
            example = "{\"blockRate\": 35.7, \"threshold\": 30.0, \"blockedCount\": 15, \"totalExecutions\": 42}")
    private Map<String, Object> evidence;

    // Constructors
    public RiskMetricAlertResponse() {}

    public RiskMetricAlertResponse(UUID id, AlertType type, AlertSeverity severity, 
                                    String message, Instant detectedAt, Map<String, Object> evidence) {
        this.id = id;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.detectedAt = detectedAt;
        this.evidence = evidence;
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

    public ProjectContext getProjectContext() {
        return projectContext;
    }

    public void setProjectContext(ProjectContext projectContext) {
        this.projectContext = projectContext;
    }

    public String getBusinessRuleId() {
        return businessRuleId;
    }

    public void setBusinessRuleId(String businessRuleId) {
        this.businessRuleId = businessRuleId;
    }

    public String getBusinessRuleName() {
        return businessRuleName;
    }

    public void setBusinessRuleName(String businessRuleName) {
        this.businessRuleName = businessRuleName;
    }

    public List<OwnershipSummary> getOwnerships() {
        return ownerships;
    }

    public void setOwnerships(List<OwnershipSummary> ownerships) {
        this.ownerships = ownerships;
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
