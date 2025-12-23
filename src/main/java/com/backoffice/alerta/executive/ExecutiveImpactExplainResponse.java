package com.backoffice.alerta.executive;

import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.rules.RiskLevel;
import java.time.Instant;

/**
 * Response da explicação executiva de impacto sistêmico
 * 
 * Contém interpretação estruturada em linguagem de negócio.
 * 
 * US#38 - Explicação Executiva Inteligente
 * 
 * IMPORTANTE: Response consultivo - não representa decisão do sistema
 */
public class ExecutiveImpactExplainResponse {
    
    private final String pullRequestId;
    private final RiskLevel overallRiskLevel;
    private final ExecutiveSummary executiveSummary;
    private final ConfidenceLevel confidenceLevel;
    private final Instant generatedAt;
    private final ProjectContext projectContext;
    
    public ExecutiveImpactExplainResponse(String pullRequestId,
                                         RiskLevel overallRiskLevel,
                                         ExecutiveSummary executiveSummary,
                                         ConfidenceLevel confidenceLevel,
                                         Instant generatedAt,
                                         ProjectContext projectContext) {
        this.pullRequestId = pullRequestId;
        this.overallRiskLevel = overallRiskLevel;
        this.executiveSummary = executiveSummary;
        this.confidenceLevel = confidenceLevel;
        this.generatedAt = generatedAt;
        this.projectContext = projectContext;
    }
    
    public String getPullRequestId() {
        return pullRequestId;
    }
    
    public RiskLevel getOverallRiskLevel() {
        return overallRiskLevel;
    }
    
    public ExecutiveSummary getExecutiveSummary() {
        return executiveSummary;
    }
    
    public ConfidenceLevel getConfidenceLevel() {
        return confidenceLevel;
    }
    
    public Instant getGeneratedAt() {
        return generatedAt;
    }
    
    public ProjectContext getProjectContext() {
        return projectContext;
    }
}
