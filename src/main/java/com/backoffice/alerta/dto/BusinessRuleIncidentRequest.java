package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.IncidentSeverity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Request para registrar incidente de produção relacionado a regra de negócio
 */
@Schema(description = "Requisição para registrar um incidente histórico")
public class BusinessRuleIncidentRequest {

    @Schema(description = "ID da regra de negócio afetada", example = "BR-001", required = true)
    private String businessRuleId;

    @Schema(description = "Título do incidente", example = "Falha no processamento de pagamento", required = true)
    private String title;

    @Schema(description = "Descrição detalhada do incidente", 
            example = "Sistema falhou ao processar pagamentos recorrentes causando perda de receita",
            required = true)
    private String description;

    @Schema(description = "Severidade do incidente", example = "CRITICAL", required = true)
    private IncidentSeverity severity;

    @Schema(description = "Data e hora em que o incidente ocorreu", required = true)
    private Instant occurredAt;

    public BusinessRuleIncidentRequest() {
    }

    public BusinessRuleIncidentRequest(String businessRuleId, String title, String description,
                                      IncidentSeverity severity, Instant occurredAt) {
        this.businessRuleId = businessRuleId;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.occurredAt = occurredAt;
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
}
