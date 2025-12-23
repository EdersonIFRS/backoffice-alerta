package com.backoffice.alerta.llm;

import com.backoffice.alerta.git.GitProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * US#71 - Request para comparação de impacto PRE vs POST
 * 
 * READ-ONLY - não persiste dados, apenas compara estados
 */
@Schema(description = "Request para comparação de impacto antes e depois de mudança")
public class LLMImpactComparisonRequest {

    @Schema(description = "ID do projeto (opcional - permite análise global)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID projectId;

    @NotNull
    @Schema(description = "Provider Git", example = "GITHUB", required = true)
    private GitProvider provider;

    @NotNull
    @Schema(description = "URL do repositório", example = "https://github.com/org/repo", required = true)
    private String repositoryUrl;

    @NotNull
    @Schema(description = "Referência base (branch ou commit SHA)", example = "main", required = true)
    private String baseRef;

    @NotNull
    @Schema(description = "Referência de comparação (PR number ou commit SHA)", example = "123", required = true)
    private String compareRef;

    // Constructors
    public LLMImpactComparisonRequest() {
    }

    public LLMImpactComparisonRequest(UUID projectId, GitProvider provider, String repositoryUrl, 
                                     String baseRef, String compareRef) {
        this.projectId = projectId;
        this.provider = provider;
        this.repositoryUrl = repositoryUrl;
        this.baseRef = baseRef;
        this.compareRef = compareRef;
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

    public String getBaseRef() {
        return baseRef;
    }

    public void setBaseRef(String baseRef) {
        this.baseRef = baseRef;
    }

    public String getCompareRef() {
        return compareRef;
    }

    public void setCompareRef(String compareRef) {
        this.compareRef = compareRef;
    }
}
