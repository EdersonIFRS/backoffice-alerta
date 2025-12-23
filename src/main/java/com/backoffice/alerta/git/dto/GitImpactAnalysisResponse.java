package com.backoffice.alerta.git.dto;

import com.backoffice.alerta.ast.ASTImpactDetail;
import com.backoffice.alerta.project.dto.ProjectContext;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * US#51 - Response da análise de impacto de Pull Request
 * 
 * Consolida metadados do PR + análise de impacto
 */
@Schema(description = "Resultado da análise de impacto de Pull Request")
public class GitImpactAnalysisResponse {

    @Schema(description = "Dados do Pull Request analisado")
    private GitPullRequestData pullRequest;

    @Schema(description = "Contexto de projeto (US#50)")
    private ProjectContext projectContext;

    @Schema(description = "Nível de risco identificado", example = "ALTO")
    private String riskLevel;

    @Schema(description = "Decisão final sugerida", example = "APROVADO_COM_RESTRICOES")
    private String finalDecision;

    @Schema(description = "Resumo consolidado do impacto")
    private Object impactSummary;
    
    /**
     * US#69 - Detalhes de impacto a nível de AST (métodos, classes)
     * Lista vazia quando análise AST não aplicável (nunca null)
     */
    @Schema(description = "Detalhes de impacto a nível de AST (métodos, classes)")
    private List<ASTImpactDetail> astDetails = new ArrayList<>();

    public GitImpactAnalysisResponse() {}

    public GitImpactAnalysisResponse(GitPullRequestData pullRequest, ProjectContext projectContext,
                                    String riskLevel, String finalDecision, Object impactSummary) {
        this.pullRequest = pullRequest;
        this.projectContext = projectContext;
        this.riskLevel = riskLevel;
        this.finalDecision = finalDecision;
        this.impactSummary = impactSummary;
    }

    // Getters e Setters
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
    
    public List<ASTImpactDetail> getAstDetails() {
        return astDetails;
    }
    
    public void setAstDetails(List<ASTImpactDetail> astDetails) {
        this.astDetails = astDetails;
    }
}
