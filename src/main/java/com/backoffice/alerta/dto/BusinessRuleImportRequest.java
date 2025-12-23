package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * US#68 - Request para importação de regras de negócio do Git
 */
@Schema(description = "Request para importação automática de regras de negócio a partir de repositório Git")
public class BusinessRuleImportRequest {

    @Schema(description = "ID do projeto ao qual as regras serão associadas", example = "550e8400-e29b-41d4-a716-446655440001", required = true)
    private UUID projectId;

    @Schema(description = "Provider Git (GITHUB ou GITLAB)", example = "GITHUB", required = true)
    private GitProvider provider;

    @Schema(description = "URL do repositório Git", example = "https://github.com/empresa/backoffice-pagamentos", required = true)
    private String repositoryUrl;

    @Schema(description = "Branch a ser analisada", example = "main", required = false)
    private String branch = "main";

    @Schema(description = "Modo dry-run: true = simulação sem persistência, false = importação real", example = "false", required = false)
    private boolean dryRun = false;

    // Constructors
    public BusinessRuleImportRequest() {
    }

    public BusinessRuleImportRequest(UUID projectId, GitProvider provider, String repositoryUrl, String branch, boolean dryRun) {
        this.projectId = projectId;
        this.provider = provider;
        this.repositoryUrl = repositoryUrl;
        this.branch = branch != null ? branch : "main";
        this.dryRun = dryRun;
    }

    // Getters and Setters
    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public GitProvider getProvider() {
        return provider;
    }

    public void setProvider(GitProvider provider) {
        this.provider = provider;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch != null ? branch : "main";
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    /**
     * Provider Git suportado
     */
    public enum GitProvider {
        GITHUB,
        GITLAB
    }
}
