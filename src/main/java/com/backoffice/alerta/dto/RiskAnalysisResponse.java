package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Resultado da análise de risco do Pull Request")
public class RiskAnalysisResponse {

    @Schema(description = "Identificador do Pull Request analisado", example = "PR-12345")
    private String pullRequestId;
    
    @Schema(description = "Score de risco calculado (0-100)", example = "85")
    private Integer riskScore;
    
    @Schema(description = "Nível de risco", example = "CRÍTICO", allowableValues = {"BAIXO", "MÉDIO", "ALTO", "CRÍTICO"})
    private String riskLevel;
    
    @Schema(description = "Explicação detalhada dos pontos calculados")
    private List<String> explanation;

    @Schema(description = "Versão das regras aplicada na análise", example = "v1")
    private String ruleVersion;

    public RiskAnalysisResponse() {
    }

    public RiskAnalysisResponse(String pullRequestId, Integer riskScore, String riskLevel, List<String> explanation) {
        this.pullRequestId = pullRequestId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.explanation = explanation;
    }

    public RiskAnalysisResponse(String pullRequestId, Integer riskScore, String riskLevel, List<String> explanation, String ruleVersion) {
        this.pullRequestId = pullRequestId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.explanation = explanation;
        this.ruleVersion = ruleVersion;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getExplanation() {
        return explanation;
    }

    public void setExplanation(List<String> explanation) {
        this.explanation = explanation;
    }

    public String getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(String ruleVersion) {
        this.ruleVersion = ruleVersion;
    }
}
