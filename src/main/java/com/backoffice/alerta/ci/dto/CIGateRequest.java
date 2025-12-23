package com.backoffice.alerta.ci.dto;

import com.backoffice.alerta.git.GitProvider;
import com.backoffice.alerta.rules.ChangeType;
import com.backoffice.alerta.rules.Environment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * US#53 - Request para CI/CD Gate de Risco
 * 
 * DTO usado por pipelines de CI/CD para validar Pull Requests/Merge Requests
 * antes do merge.
 * 
 * ⚠️ READ-ONLY: Apenas consulta, sem persistência
 */
@Schema(description = "Request para análise de risco em pipeline CI/CD")
public class CIGateRequest {

    @NotNull(message = "Provider Git é obrigatório")
    @Schema(description = "Provedor Git", example = "GITHUB", requiredMode = Schema.RequiredMode.REQUIRED)
    private GitProvider provider;

    @NotBlank(message = "URL do repositório é obrigatória")
    @Schema(description = "URL do repositório Git", 
            example = "https://github.com/acme/backoffice", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String repositoryUrl;

    @NotBlank(message = "Número do Pull Request é obrigatório")
    @Schema(description = "Número/ID do Pull Request ou Merge Request", 
            example = "123", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String pullRequestNumber;

    @NotNull(message = "Ambiente é obrigatório")
    @Schema(description = "Ambiente de deploy alvo", 
            example = "PRODUCTION", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Environment environment;

    @NotNull(message = "Tipo de mudança é obrigatório")
    @Schema(description = "Tipo de mudança sendo introduzida", 
            example = "FEATURE", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private ChangeType changeType;

    @Schema(description = "ID do projeto para análise escopada (US#50 - opcional)", 
            example = "550e8400-e29b-41d4-a716-446655440010")
    private UUID projectId;

    @Schema(description = "Branch base (target)", example = "main")
    private String baseBranch;

    @Schema(description = "Branch de feature (source)", example = "feature/payment-validation")
    private String headBranch;

    @Schema(description = "Lista de arquivos alterados (fallback se CI já tiver)")
    private List<String> changedFiles;

    // Construtores
    public CIGateRequest() {}

    public CIGateRequest(GitProvider provider, String repositoryUrl, String pullRequestNumber,
                        Environment environment, ChangeType changeType) {
        this.provider = provider;
        this.repositoryUrl = repositoryUrl;
        this.pullRequestNumber = pullRequestNumber;
        this.environment = environment;
        this.changeType = changeType;
    }

    // Getters e Setters
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

    public String getPullRequestNumber() {
        return pullRequestNumber;
    }

    public void setPullRequestNumber(String pullRequestNumber) {
        this.pullRequestNumber = pullRequestNumber;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getBaseBranch() {
        return baseBranch;
    }

    public void setBaseBranch(String baseBranch) {
        this.baseBranch = baseBranch;
    }

    public String getHeadBranch() {
        return headBranch;
    }

    public void setHeadBranch(String headBranch) {
        this.headBranch = headBranch;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(List<String> changedFiles) {
        this.changedFiles = changedFiles;
    }
}
