package com.backoffice.alerta.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * US#50 - Context de Projeto para Responses
 * 
 * Indica se a análise está escopada a um projeto específico
 */
@Schema(description = "Contexto de projeto da análise")
public class ProjectContext {

    @Schema(description = "Se a análise está escopada a um projeto", example = "true")
    private boolean scoped;

    @Schema(description = "ID do projeto (quando escopado)", example = "550e8400-e29b-41d4-a716-446655440010")
    private UUID projectId;

    @Schema(description = "Nome do projeto (quando escopado)", example = "Backoffice Pagamentos")
    private String projectName;

    public ProjectContext() {
        this.scoped = false;
    }

    public ProjectContext(boolean scoped) {
        this.scoped = scoped;
    }

    public ProjectContext(UUID projectId, String projectName) {
        this.scoped = true;
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public boolean isScoped() {
        return scoped;
    }

    public void setScoped(boolean scoped) {
        this.scoped = scoped;
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

    /**
     * Factory method para contexto global (sem projeto)
     */
    public static ProjectContext global() {
        return new ProjectContext(false);
    }

    /**
     * Factory method para contexto escopado
     */
    public static ProjectContext scoped(UUID projectId, String projectName) {
        return new ProjectContext(projectId, projectName);
    }
}
