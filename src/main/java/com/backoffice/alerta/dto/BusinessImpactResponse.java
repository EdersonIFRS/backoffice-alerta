package com.backoffice.alerta.dto;

import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.rules.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Resposta da análise de impacto de negócio em Pull Request
 */
@Schema(description = "Resposta com análise de impacto de negócio")
public class BusinessImpactResponse {

    @Schema(description = "ID do Pull Request", example = "PR-123")
    private String pullRequestId;

    @Schema(description = "Lista de regras de negócio impactadas")
    private List<ImpactedBusinessRuleResponse> impactedBusinessRules;

    @Schema(description = "Risco geral do Pull Request para negócio", example = "CRITICO")
    private RiskLevel overallBusinessRisk;
    
    @Schema(description = "Contexto de escopo de projeto (se aplicável)")
    private ProjectContext projectContext;

    public BusinessImpactResponse() {
    }

    public BusinessImpactResponse(String pullRequestId, 
                                 List<ImpactedBusinessRuleResponse> impactedBusinessRules,
                                 RiskLevel overallBusinessRisk) {
        this.pullRequestId = pullRequestId;
        this.impactedBusinessRules = impactedBusinessRules;
        this.overallBusinessRisk = overallBusinessRisk;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public List<ImpactedBusinessRuleResponse> getImpactedBusinessRules() {
        return impactedBusinessRules;
    }

    public void setImpactedBusinessRules(List<ImpactedBusinessRuleResponse> impactedBusinessRules) {
        this.impactedBusinessRules = impactedBusinessRules;
    }

    public RiskLevel getOverallBusinessRisk() {
        return overallBusinessRisk;
    }

    public void setOverallBusinessRisk(RiskLevel overallBusinessRisk) {
        this.overallBusinessRisk = overallBusinessRisk;
    }
    
    public ProjectContext getProjectContext() {
        return projectContext;
    }
    
    public void setProjectContext(ProjectContext projectContext) {
        this.projectContext = projectContext;
    }
}
