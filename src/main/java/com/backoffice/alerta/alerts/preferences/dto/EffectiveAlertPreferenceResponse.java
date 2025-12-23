package com.backoffice.alerta.alerts.preferences.dto;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.alerts.notification.NotificationChannel;
import com.backoffice.alerta.alerts.preferences.AlertDeliveryWindow;
import com.backoffice.alerta.project.dto.ProjectContext;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

/**
 * Response DTO contendo a preferência efetiva após aplicar hierarquia
 * 
 * US#57 - Preferências de Alertas por Projeto e Regra de Negócio
 * 
 * HIERARQUIA: Regra > Projeto > Default
 */
@Schema(description = "Preferência efetiva após resolução de hierarquia")
public class EffectiveAlertPreferenceResponse {

    public enum PreferenceSource {
        RULE,
        PROJECT,
        DEFAULT
    }

    @Schema(description = "Fonte da preferência aplicada", example = "RULE")
    private PreferenceSource source;

    @Schema(description = "ID do projeto (contexto)")
    private UUID projectId;

    @Schema(description = "Nome do projeto (contexto)")
    private String projectName;

    @Schema(description = "ID da regra de negócio (contexto)")
    private String businessRuleId;

    @Schema(description = "Nome da regra de negócio (contexto)")
    private String businessRuleName;

    @Schema(description = "Severidade mínima efetiva", example = "CRITICAL")
    private AlertSeverity minimumSeverity;

    @Schema(description = "Tipos de alerta permitidos (vazio = todos)")
    private Set<AlertType> allowedAlertTypes;

    @Schema(description = "Canais habilitados")
    private Set<NotificationChannel> channels;

    @Schema(description = "Janela de entrega", example = "ANY_TIME")
    private AlertDeliveryWindow deliveryWindow;

    @Schema(description = "Contexto do projeto")
    private ProjectContext projectContext;

    // Constructors
    public EffectiveAlertPreferenceResponse() {}

    public EffectiveAlertPreferenceResponse(PreferenceSource source) {
        this.source = source;
    }

    // Getters and Setters
    public PreferenceSource getSource() {
        return source;
    }

    public void setSource(PreferenceSource source) {
        this.source = source;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public ProjectContext getProjectContext() {
        return projectContext;
    }

    public void setProjectContext(ProjectContext projectContext) {
        this.projectContext = projectContext;
    }
}
