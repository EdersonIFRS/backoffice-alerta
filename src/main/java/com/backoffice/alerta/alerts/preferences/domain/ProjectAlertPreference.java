package com.backoffice.alerta.alerts.preferences.domain;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.alerts.notification.NotificationChannel;
import com.backoffice.alerta.alerts.preferences.AlertDeliveryWindow;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Preferências de alertas configuradas para um projeto
 * 
 * US#57 - Preferências de Alertas por Projeto e Regra de Negócio
 * 
 * HIERARQUIA: Regra > Projeto > Default
 */
@Entity
@Table(name = "project_alert_preferences")
public class ProjectAlertPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false, unique = true)
    private UUID projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "minimum_severity")
    private AlertSeverity minimumSeverity;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_alert_allowed_types", joinColumns = @JoinColumn(name = "preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type")
    private Set<AlertType> allowedAlertTypes = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_alert_channels", joinColumns = @JoinColumn(name = "preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "channel")
    private Set<NotificationChannel> channels = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_window")
    private AlertDeliveryWindow deliveryWindow;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Constructors
    public ProjectAlertPreference() {}

    public ProjectAlertPreference(UUID projectId) {
        this.projectId = projectId;
    }

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
