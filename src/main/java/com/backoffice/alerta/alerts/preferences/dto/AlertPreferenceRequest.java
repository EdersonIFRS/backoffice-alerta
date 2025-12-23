package com.backoffice.alerta.alerts.preferences.dto;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.alerts.notification.NotificationChannel;
import com.backoffice.alerta.alerts.preferences.AlertDeliveryWindow;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * Request DTO para criar/atualizar preferências de alerta
 * 
 * US#57 - Preferências de Alertas por Projeto e Regra de Negócio
 */
@Schema(description = "Configuração de preferências de alerta")
public class AlertPreferenceRequest {

    @Schema(description = "Severidade mínima para envio de alertas", example = "WARNING")
    private AlertSeverity minimumSeverity;

    @Schema(description = "Tipos de alerta permitidos (se vazio, todos são permitidos)")
    private Set<AlertType> allowedAlertTypes;

    @Schema(description = "Canais de notificação habilitados", example = "[\"SLACK\", \"TEAMS\"]")
    @NotNull(message = "Channels cannot be null")
    private Set<NotificationChannel> channels;

    @Schema(description = "Janela de entrega dos alertas", example = "BUSINESS_HOURS")
    @NotNull(message = "Delivery window cannot be null")
    private AlertDeliveryWindow deliveryWindow;

    // Constructors
    public AlertPreferenceRequest() {}

    public AlertPreferenceRequest(AlertSeverity minimumSeverity, Set<AlertType> allowedAlertTypes,
                                   Set<NotificationChannel> channels, AlertDeliveryWindow deliveryWindow) {
        this.minimumSeverity = minimumSeverity;
        this.allowedAlertTypes = allowedAlertTypes;
        this.channels = channels;
        this.deliveryWindow = deliveryWindow;
    }

    // Getters and Setters
    public AlertSeverity getMinimumSeverity() {
        return minimumSeverity;
    }

    public void setMinimumSeverity(AlertSeverity minimumSeverity) {
        this.minimumSeverity = minimumSeverity;
    }

    public Set<AlertType> getAllowedAlertTypes() {
        return allowedAlertTypes;
    }

    public void setAllowedAlertTypes(Set<AlertType> allowedAlertTypes) {
        this.allowedAlertTypes = allowedAlertTypes;
    }

    public Set<NotificationChannel> getChannels() {
        return channels;
    }

    public void setChannels(Set<NotificationChannel> channels) {
        this.channels = channels;
    }

    public AlertDeliveryWindow getDeliveryWindow() {
        return deliveryWindow;
    }

    public void setDeliveryWindow(AlertDeliveryWindow deliveryWindow) {
        this.deliveryWindow = deliveryWindow;
    }
}
