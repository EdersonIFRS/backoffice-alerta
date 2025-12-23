// US#48 - DTO de Response para Projeto
package com.backoffice.alerta.project.api.dto;

import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.domain.ProjectType;
import com.backoffice.alerta.project.domain.RepositoryType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * US#48 - Response DTO para retorno de projeto.
 * 
 * Contém apenas campos relevantes para exibição.
 */
@Schema(description = "Response com dados de um projeto organizacional")
public class ProjectResponse {

    @Schema(description = "ID único do projeto", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Nome do projeto", example = "Backoffice Pagamentos")
    private String name;

    @Schema(description = "Descrição do projeto", example = "Sistema de processamento de pagamentos")
    private String description;

    @Schema(description = "Tipo do projeto", example = "BACKEND")
    private ProjectType type;

    @Schema(description = "Tipo de repositório", example = "GITHUB")
    private RepositoryType repositoryType;

    @Schema(description = "URL do repositório", example = "https://github.com/company/payment-backoffice")
    private String repositoryUrl;

    @Schema(description = "Branch padrão", example = "main")
    private String defaultBranch;

    @Schema(description = "Se o projeto está ativo", example = "true")
    private boolean active;

    @Schema(description = "Data de criação do projeto")
    private Instant createdAt;

    @Schema(description = "Data da última atualização")
    private Instant updatedAt;

    // Construtores

    public ProjectResponse() {
    }

    public ProjectResponse(UUID id, String name, String description, ProjectType type, 
                           RepositoryType repositoryType, String repositoryUrl, 
                           String defaultBranch, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.repositoryType = repositoryType;
        this.repositoryUrl = repositoryUrl;
        this.defaultBranch = defaultBranch;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory method para criar Response a partir de entidade.
     */
    public static ProjectResponse fromEntity(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getType(),
                project.getRepositoryType(),
                project.getRepositoryUrl(),
                project.getDefaultBranch(),
                project.isActive(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectType getType() {
        return type;
    }

    public void setType(ProjectType type) {
        this.type = type;
    }

    public RepositoryType getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(RepositoryType repositoryType) {
        this.repositoryType = repositoryType;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
