// US#48 - DTO de Request para criação/atualização de Projeto
package com.backoffice.alerta.project.api.dto;

import com.backoffice.alerta.project.domain.ProjectType;
import com.backoffice.alerta.project.domain.RepositoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * US#48 - Request DTO para criação e atualização de projetos.
 * 
 * Validações via Jakarta Bean Validation.
 */
@Schema(description = "Request para criar ou atualizar um projeto organizacional")
public class ProjectRequest {

    @NotBlank(message = "Nome do projeto é obrigatório")
    @Schema(description = "Nome único do projeto", example = "Backoffice Pagamentos", required = true)
    private String name;

    @Schema(description = "Descrição detalhada do projeto", example = "Sistema de processamento de pagamentos para clientes PJ e PF")
    private String description;

    @NotNull(message = "Tipo de projeto é obrigatório")
    @Schema(description = "Tipo arquitetural do projeto", example = "BACKEND", required = true)
    private ProjectType type;

    @NotNull(message = "Tipo de repositório é obrigatório")
    @Schema(description = "Provedor de controle de versão", example = "GITHUB", required = true)
    private RepositoryType repositoryType;

    @NotBlank(message = "URL do repositório é obrigatória")
    @Schema(description = "URL completa do repositório", example = "https://github.com/company/payment-backoffice", required = true)
    private String repositoryUrl;

    @NotBlank(message = "Branch padrão é obrigatória")
    @Schema(description = "Branch principal do projeto", example = "main", required = true)
    private String defaultBranch;

    // Construtores

    public ProjectRequest() {
    }

    public ProjectRequest(String name, String description, ProjectType type, 
                          RepositoryType repositoryType, String repositoryUrl, String defaultBranch) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.repositoryType = repositoryType;
        this.repositoryUrl = repositoryUrl;
        this.defaultBranch = defaultBranch;
    }

    // Getters and Setters

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
}
