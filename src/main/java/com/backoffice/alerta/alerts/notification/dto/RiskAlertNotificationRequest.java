package com.backoffice.alerta.alerts.notification.dto;

import com.backoffice.alerta.alerts.notification.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO para envio de notificação de alerta
 * 
 * US#56 - Alertas Inteligentes via Slack / Microsoft Teams
 */
@Schema(description = "Request para envio de notificação de alerta de risco")
public class RiskAlertNotificationRequest {

    @NotNull(message = "Canal de notificação é obrigatório")
    @Schema(description = "Canal de notificação (SLACK ou TEAMS)", example = "SLACK", required = true)
    private NotificationChannel channel;

    @NotBlank(message = "Webhook URL é obrigatória")
    @Schema(description = "URL do Incoming Webhook (Slack ou Teams)", 
            example = "<REDACTED_SLACK_WEBHOOK>",
            required = true)
    private String webhookUrl;

    // Constructors
    public RiskAlertNotificationRequest() {}

    public RiskAlertNotificationRequest(NotificationChannel channel, String webhookUrl) {
        this.channel = channel;
        this.webhookUrl = webhookUrl;
    }

    // Getters and Setters
    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}
