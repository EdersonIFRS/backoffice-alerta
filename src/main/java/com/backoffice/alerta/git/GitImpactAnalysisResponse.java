package com.backoffice.alerta.git;

import com.backoffice.alerta.project.dto.ProjectContext;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * US#51 - Response da análise de impacto de PR
 * 
 * Consolidação de metadados do PR + análise de impacto.
 * Read-only, sem persistência.
 */
@Schema(description = "Resultado da análise de impacto do Pull Request")
public class GitImpactAnalysisResponse {
    
    @Schema(description = "Dados do Pull Request analisado")
    private GitPullRequestData pullRequest;
    
    @Schema(description = "Contexto de projeto (US#50)")
    private ProjectContext projectContext;
    
    @Schema(description = "Nível de risco calculado", example = "MEDIUM")
    private String riskLevel;
    
    @Schema(description = "Decisão final sugerida", example = "APPROVE_WITH_REVIEW")
    private String finalDecision;
    
    @Schema(description = "Resumo consolidado do impacto")
    private Object impactSummary;
    
    public GitImpactAnalysisResponse() {}
    
    public GitPullRequestData getPullRequest() {
        return pullRequest;
    }
    
    public void setPullRequest(GitPullRequestData pullRequest) {
        this.pullRequest = pullRequest;
    }
    
    public ProjectContext getProjectContext() {
        return projectContext;
    }
    
    public void setProjectContext(ProjectContext projectContext) {
        this.projectContext = projectContext;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public String getFinalDecision() {
        return finalDecision;
    }
    
    public void setFinalDecision(String finalDecision) {
        this.finalDecision = finalDecision;
    }
    
    public Object getImpactSummary() {
        return impactSummary;
    }
    
    public void setImpactSummary(Object impactSummary) {
        this.impactSummary = impactSummary;
    }
}
