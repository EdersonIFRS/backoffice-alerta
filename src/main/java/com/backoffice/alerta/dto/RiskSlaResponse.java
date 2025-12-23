package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.RiskLevel;
import com.backoffice.alerta.sla.EscalationLevel;
import com.backoffice.alerta.sla.RiskSlaTracking;
import com.backoffice.alerta.sla.SlaStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response contendo informações de SLA de risco
 */
@Schema(description = "Informações de SLA de resposta organizacional para risco")
public class RiskSlaResponse {

    @Schema(description = "ID único do SLA", example = "123e4567-e89b-12d3-a456-426614174000")
    private final UUID id;

    @Schema(description = "ID da notificação relacionada", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID notificationId;

    @Schema(description = "ID da auditoria relacionada", example = "660e8400-e29b-41d4-a716-446655440001")
    private final UUID auditId;

    @Schema(description = "ID do Pull Request", example = "PR-123")
    private final String pullRequestId;

    @Schema(description = "Nível de risco", example = "CRITICO")
    private final RiskLevel riskLevel;

    @Schema(description = "Nível atual de escalonamento", example = "PRIMARY")
    private final EscalationLevel currentLevel;

    @Schema(description = "Prazo limite do SLA", example = "2024-03-15T15:00:00Z")
    private final Instant slaDeadline;

    @Schema(description = "Status do SLA", example = "PENDING")
    private final SlaStatus status;

    @Schema(description = "Data/hora de criação do SLA", example = "2024-03-15T14:30:00Z")
    private final Instant createdAt;

    @Schema(description = "Data/hora do último escalonamento", example = "2024-03-15T14:45:00Z")
    private final Instant lastEscalationAt;

    @Schema(description = "Indica se o SLA está vencido", example = "false")
    private final boolean overdue;

    /**
     * Construtor a partir da entidade
     */
    public RiskSlaResponse(RiskSlaTracking sla) {
        this.id = sla.getId();
        this.notificationId = sla.getNotificationId();
        this.auditId = sla.getAuditId();
        this.pullRequestId = sla.getPullRequestId();
        this.riskLevel = sla.getRiskLevel();
        this.currentLevel = sla.getCurrentLevel();
        this.slaDeadline = sla.getSlaDeadline();
        this.status = sla.getStatus();
        this.createdAt = sla.getCreatedAt();
        this.lastEscalationAt = sla.getLastEscalationAt();
        this.overdue = sla.isOverdue();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public UUID getAuditId() {
        return auditId;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public EscalationLevel getCurrentLevel() {
        return currentLevel;
    }

    public Instant getSlaDeadline() {
        return slaDeadline;
    }

    public SlaStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastEscalationAt() {
        return lastEscalationAt;
    }

    public boolean isOverdue() {
        return overdue;
    }
}
