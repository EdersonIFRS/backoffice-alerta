package com.backoffice.alerta.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * US#49 - Response com dados da associação Project-BusinessRule
 */
@Schema(description = "Dados da associação entre projeto e regra de negócio")
public class ProjectBusinessRuleResponse {

    @Schema(description = "ID da associação", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    private UUID id;

    @Schema(description = "ID do projeto", example = "550e8400-e29b-41d4-a716-446655440010")
    private UUID projectId;

    @Schema(description = "Nome do projeto", example = "Backoffice Pagamentos")
    private String projectName;

    @Schema(description = "ID da regra de negócio", example = "550e8400-e29b-41d4-a716-446655440001")
    private String businessRuleId;

    @Schema(description = "Nome da regra", example = "REGRA_CALCULO_HORAS_PJ")
    private String businessRuleName;

    @Schema(description = "Descrição da regra", example = "Cálculo de Horas PJ")
    private String businessRuleDescription;

    @Schema(description = "Data de associação", example = "2024-12-19T22:30:00Z")
    private Instant createdAt;

    @Schema(description = "Usuário que criou a associação", example = "admin")
    private String createdBy;

    public ProjectBusinessRuleResponse() {
    }

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

    public String getBusinessRuleDescription() {
        return businessRuleDescription;
    }

    public void setBusinessRuleDescription(String businessRuleDescription) {
        this.businessRuleDescription = businessRuleDescription;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
