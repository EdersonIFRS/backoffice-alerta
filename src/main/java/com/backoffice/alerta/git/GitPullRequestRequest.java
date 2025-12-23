package com.backoffice.alerta.git;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * US#51 - Request para análise de Pull Request
 * 
 * DTO para solicitar análise de impacto de um PR.
 * Suporta contextualização por projeto (US#50).
 */
@Schema(description = "Solicitação de análise de Pull Request")
public class GitPullRequestRequest {
    
    @NotNull(message = "Provider Git é obrigatório")
    @Schema(description = "Provedor Git", example = "GITHUB", required = true)
    private GitProvider provider;
    
    @Schema(description = "ID do projeto para escopo (opcional)", example = "550e8400-e29b-41d4-a716-446655440010")
    private UUID projectId;
    
    @NotBlank(message = "URL do repositório é obrigatória")
    @Schema(description = "URL do repositório Git", example = "https://github.com/company/payment-backoffice", required = true)
    private String repositoryUrl;
    
    @NotBlank(message = "Número do Pull Request é obrigatório")
    @Schema(description = "Número ou ID do Pull Request", example = "123", required = true)
    private String pullRequestNumber;
    
    public GitPullRequestRequest() {}
    
    public GitProvider getProvider() {
        return provider;
    }
    
    public void setProvider(GitProvider provider) {
        this.provider = provider;
    }
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
    
    public String getRepositoryUrl() {
        return repositoryUrl;
    }
    
    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }
    
    public String getPullRequestNumber() {
        return pullRequestNumber;
    }
    
    public void setPullRequestNumber(String pullRequestNumber) {
        this.pullRequestNumber = pullRequestNumber;
    }
}
