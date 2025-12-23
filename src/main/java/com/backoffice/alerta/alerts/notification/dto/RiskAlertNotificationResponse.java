package com.backoffice.alerta.alerts.notification.dto;

import com.backoffice.alerta.alerts.notification.NotificationChannel;
import com.backoffice.alerta.alerts.notification.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO para resultado de envio de notificação
 * 
 * US#56 - Alertas Inteligentes via Slack / Microsoft Teams
 */
@Schema(description = "Response com resultado do envio de notificação de alerta")
public class RiskAlertNotificationResponse {

    @Schema(description = "ID do alerta notificado", example = "550e8400-e29b-41d4-a716-446655440099")
    private UUID alertId;

    @Schema(description = "Canal de notificação utilizado", example = "SLACK")
    private NotificationChannel channel;

    @Schema(description = "Status do envio", example = "SENT")
    private NotificationStatus status;

    @Schema(description = "Momento do envio", example = "2025-12-20T20:15:00Z")
    private Instant sentAt;

    @Schema(description = "Mensagem de erro (se status = FAILED)", example = "Connection timeout")
    private String errorMessage;

    // Constructors
    public RiskAlertNotificationResponse() {
        this.sentAt = Instant.now();
    }

    public RiskAlertNotificationResponse(UUID alertId, NotificationChannel channel, NotificationStatus status) {
        this();
        this.alertId = alertId;
        this.channel = channel;
        this.status = status;
    }

    public RiskAlertNotificationResponse(UUID alertId, NotificationChannel channel, 
                                          NotificationStatus status, String errorMessage) {
        this(alertId, channel, status);
        this.errorMessage = errorMessage;
    }

    // Getters and Setters
    public UUID getAlertId() {
        return alertId;
    }

    public void setAlertId(UUID alertId) {
        this.alertId = alertId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // Factory methods
    public static RiskAlertNotificationResponse success(UUID alertId, NotificationChannel channel) {
        return new RiskAlertNotificationResponse(alertId, channel, NotificationStatus.SENT);
    }

    public static RiskAlertNotificationResponse error(UUID alertId, NotificationChannel channel, String errorMessage) {
        return new RiskAlertNotificationResponse(alertId, channel, NotificationStatus.FAILED, errorMessage);
    }
}
