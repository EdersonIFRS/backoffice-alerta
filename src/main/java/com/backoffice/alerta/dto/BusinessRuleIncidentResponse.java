package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.IncidentSeverity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response com dados de incidente histórico
 */
@Schema(description = "Resposta com dados de um incidente histórico")
public class BusinessRuleIncidentResponse {

    @Schema(description = "ID único do incidente")
    private String id;

    @Schema(description = "ID da regra de negócio afetada", example = "BR-001")
    private String businessRuleId;

    @Schema(description = "Título do incidente", example = "Falha no processamento de pagamento")
    private String title;

    @Schema(description = "Descrição detalhada do incidente")
    private String description;

    @Schema(description = "Severidade do incidente", example = "CRITICAL")
    private IncidentSeverity severity;

    @Schema(description = "Data e hora em que o incidente ocorreu")
    private Instant occurredAt;

    @Schema(description = "Data e hora em que o incidente foi registrado")
    private Instant createdAt;

    public BusinessRuleIncidentResponse() {
    }

    public BusinessRuleIncidentResponse(String id, String businessRuleId, String title, 
                                       String description, IncidentSeverity severity, 
                                       Instant occurredAt, Instant createdAt) {
        this.id = id;
        this.businessRuleId = businessRuleId;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBusinessRuleId() {
        return businessRuleId;
    }

    public void setBusinessRuleId(String businessRuleId) {
        this.businessRuleId = businessRuleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
