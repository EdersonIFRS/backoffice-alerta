package com.backoffice.alerta.llm;

import com.backoffice.alerta.git.GitProvider;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * US#70 - Request para análise de mudanças potencialmente geradas por LLM
 */
@Schema(description = "Dados para análise de mudanças suspeitas de LLM")
public class LLMChangeDetectionRequest {

    @Schema(description = "ID do projeto (opcional, para escopo)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID projectId;

    @NotNull
    @Schema(description = "ID do Pull Request", example = "123", required = true)
    private String pullRequestId;

    @NotNull
    @Schema(description = "Provider Git (GITHUB, GITLAB, DUMMY)", example = "GITHUB", required = true)
    private GitProvider provider;

    @Schema(description = "URL do repositório", example = "https://github.com/org/repo")
    private String repositoryUrl;

    public LLMChangeDetectionRequest() {
    }

    public LLMChangeDetectionRequest(UUID projectId, String pullRequestId, GitProvider provider, String repositoryUrl) {
        this.projectId = projectId;
        this.pullRequestId = pullRequestId;
        this.provider = provider;
        this.repositoryUrl = repositoryUrl;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
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
}
