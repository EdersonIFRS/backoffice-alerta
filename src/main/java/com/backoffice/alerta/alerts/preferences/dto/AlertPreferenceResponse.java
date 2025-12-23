package com.backoffice.alerta.alerts.preferences.dto;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.alerts.notification.NotificationChannel;
import com.backoffice.alerta.alerts.preferences.AlertDeliveryWindow;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO para preferências de alerta configuradas
 * 
 * US#57 - Preferências de Alertas por Projeto e Regra de Negócio
 */
@Schema(description = "Preferências de alerta configuradas")
public class AlertPreferenceResponse {

    @Schema(description = "ID da preferência", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID do projeto (se preferência de projeto)")
    private UUID projectId;

    @Schema(description = "ID da regra de negócio (se preferência de regra)")
    private String businessRuleId;

    @Schema(description = "Severidade mínima", example = "WARNING")
    private AlertSeverity minimumSeverity;

    @Schema(description = "Tipos de alerta permitidos")
    private Set<AlertType> allowedAlertTypes;

    @Schema(description = "Canais habilitados")
    private Set<NotificationChannel> channels;

    @Schema(description = "Janela de entrega", example = "BUSINESS_HOURS")
    private AlertDeliveryWindow deliveryWindow;

    @Schema(description = "Data de criação")
    private Instant createdAt;

    @Schema(description = "Data de atualização")
    private Instant updatedAt;

    // Constructors
    public AlertPreferenceResponse() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
