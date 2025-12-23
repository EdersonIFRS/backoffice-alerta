package com.backoffice.alerta.onboarding;

import com.backoffice.alerta.git.GitProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * US#72 - Request de onboarding guiado de projeto real
 */
@Schema(description = "Requisição de onboarding guiado de projeto real")
public class ProjectOnboardingRequest {

    @NotNull
    @Schema(description = "ID do projeto", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @NotNull
    @Schema(description = "Provedor Git", example = "GITHUB")
    private GitProvider provider;

    @NotNull
    @Schema(description = "URL do repositório", example = "https://github.com/company/backoffice-payment")
    private String repositoryUrl;

    @Schema(description = "Branch principal", example = "main")
    private String branch = "main";

    public ProjectOnboardingRequest() {}

    public ProjectOnboardingRequest(UUID projectId, GitProvider provider, String repositoryUrl, String branch) {
        this.projectId = projectId;
        this.provider = provider;
        this.repositoryUrl = repositoryUrl;
        this.branch = branch;
    }

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
        this.branch = branch;
    }
}
