package com.backoffice.alerta.dto;

import com.backoffice.alerta.notification.*;
import com.backoffice.alerta.rules.OwnershipRole;
import com.backoffice.alerta.rules.TeamType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response contendo informações de uma notificação organizacional
 */
@Schema(description = "Informações de uma notificação organizacional gerada automaticamente")
public class RiskNotificationResponse {

    @Schema(description = "ID único da notificação", example = "123e4567-e89b-12d3-a456-426614174000")
    private final UUID id;

    @Schema(description = "ID da auditoria relacionada", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID auditId;

    @Schema(description = "ID do Pull Request", example = "PR-123")
    private final String pullRequestId;

    @Schema(description = "ID da regra de negócio impactada", example = "BR-001")
    private final UUID businessRuleId;

    @Schema(description = "Nome do time responsável", example = "Time de Pagamentos")
    private final String teamName;

    @Schema(description = "Tipo organizacional do time", example = "FINANCE")
    private final TeamType teamType;

    @Schema(description = "Papel do time no ownership", example = "PRIMARY_OWNER")
    private final OwnershipRole ownershipRole;

    @Schema(description = "Gatilho que disparou a notificação", example = "RISK_BLOCKED")
    private final NotificationTrigger notificationTrigger;

    @Schema(description = "Severidade da notificação", example = "CRITICAL")
    private final NotificationSeverity severity;

    @Schema(description = "Canal da notificação", example = "EMAIL")
    private final NotificationChannel channel;

    @Schema(description = "Mensagem detalhada da notificação")
    private final String message;

    @Schema(description = "Data/hora de criação da notificação", example = "2024-03-15T14:30:00Z")
    private final Instant createdAt;

    /**
     * Construtor a partir da entidade
     */
    public RiskNotificationResponse(RiskNotification notification) {
        this.id = notification.getId();
        this.auditId = notification.getAuditId();
        this.pullRequestId = notification.getPullRequestId();
        this.businessRuleId = notification.getBusinessRuleId();
        this.teamName = notification.getTeamName();
        this.teamType = notification.getTeamType();
        this.ownershipRole = notification.getOwnershipRole();
        this.notificationTrigger = notification.getNotificationTrigger();
        this.severity = notification.getSeverity();
        this.channel = notification.getChannel();
        this.message = notification.getMessage();
        this.createdAt = notification.getCreatedAt();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getAuditId() {
        return auditId;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public UUID getBusinessRuleId() {
        return businessRuleId;
    }

    public String getTeamName() {
        return teamName;
    }

    public TeamType getTeamType() {
        return teamType;
    }

    public OwnershipRole getOwnershipRole() {
        return ownershipRole;
    }

    public NotificationTrigger getNotificationTrigger() {
        return notificationTrigger;
    }

    public NotificationSeverity getSeverity() {
        return severity;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
