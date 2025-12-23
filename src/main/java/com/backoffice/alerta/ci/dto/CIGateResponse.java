package com.backoffice.alerta.ci.dto;

import com.backoffice.alerta.git.GitProvider;
import com.backoffice.alerta.project.dto.ProjectContext;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * US#53 - Response do CI/CD Gate de Risco
 * 
 * Resposta padronizada para pipelines de CI/CD processarem.
 * 
 * Exit Codes:
 * - 0: APROVADO (pipeline pode continuar)
 * - 1: APROVADO_COM_RESTRICOES (warning, mas pipeline continua)
 * - 2: BLOQUEADO (pipeline deve falhar)
 */
@Schema(description = "Response da análise de risco para CI/CD")
public class CIGateResponse {

    @Schema(description = "Decisão final do gate de risco", 
            example = "APROVADO_COM_RESTRICOES")
    private String finalDecision;

    @Schema(description = "Nível de risco geral identificado", 
            example = "MEDIO")
    private String overallRiskLevel;

    @Schema(description = "Exit code para pipeline CI/CD (0=aprovado, 1=warning, 2=bloqueado)", 
            example = "1")
    private int exitCode;

    @Schema(description = "Resumo curto da análise", 
            example = "PR aprovado com 3 regras de negócio impactadas. Revisão recomendada.")
    private String summary;

    @Schema(description = "Códigos padronizados de razão")
    private List<String> reasonCodes = new ArrayList<>();

    @Schema(description = "Ações requeridas (se houver)")
    private List<String> actionsRequired = new ArrayList<>();

    @Schema(description = "Contexto de projeto (GLOBAL ou SCOPED)")
    private ProjectContext projectContext;

    @Schema(description = "Provedor Git", example = "GITHUB")
    private GitProvider provider;

    @Schema(description = "Número do Pull Request", example = "123")
    private String pullRequestNumber;

    @Schema(description = "URL do repositório", example = "https://github.com/acme/backoffice")
    private String repositoryUrl;

    // Construtores
    public CIGateResponse() {}

    public CIGateResponse(String finalDecision, String overallRiskLevel, int exitCode, 
                         String summary, ProjectContext projectContext) {
        this.finalDecision = finalDecision;
        this.overallRiskLevel = overallRiskLevel;
        this.exitCode = exitCode;
        this.summary = summary;
        this.projectContext = projectContext;
    }

    // Getters e Setters
    public String getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(String finalDecision) {
        this.finalDecision = finalDecision;
    }

    public String getOverallRiskLevel() {
        return overallRiskLevel;
    }

    public void setOverallRiskLevel(String overallRiskLevel) {
        this.overallRiskLevel = overallRiskLevel;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getReasonCodes() {
        return reasonCodes;
    }

    public void setReasonCodes(List<String> reasonCodes) {
        this.reasonCodes = reasonCodes;
    }

    public List<String> getActionsRequired() {
        return actionsRequired;
    }

    public void setActionsRequired(List<String> actionsRequired) {
        this.actionsRequired = actionsRequired;
    }

    public ProjectContext getProjectContext() {
        return projectContext;
    }

    public void setProjectContext(ProjectContext projectContext) {
        this.projectContext = projectContext;
    }

    public GitProvider getProvider() {
        return provider;
    }

    public void setProvider(GitProvider provider) {
        this.provider = provider;
    }

    public String getPullRequestNumber() {
        return pullRequestNumber;
    }

    public void setPullRequestNumber(String pullRequestNumber) {
        this.pullRequestNumber = pullRequestNumber;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }
}
