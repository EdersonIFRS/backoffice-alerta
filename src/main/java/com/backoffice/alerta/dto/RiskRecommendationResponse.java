package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resposta com recomendações de mitigação de risco")
public class RiskRecommendationResponse {

    @Schema(description = "Identificador do Pull Request analisado", example = "PR-12345")
    private String pullRequestId;

    @Schema(description = "Versão das regras aplicada na análise", example = "v2")
    private String ruleVersion;

    @Schema(description = "Score de risco atual", example = "95")
    private int currentRiskScore;

    @Schema(description = "Nível de risco atual", example = "CRÍTICO", allowableValues = {"BAIXO", "MÉDIO", "ALTO", "CRÍTICO"})
    private String currentRiskLevel;

    @Schema(description = "Nível de risco desejado", example = "MÉDIO", allowableValues = {"BAIXO", "MÉDIO", "ALTO"})
    private String targetRiskLevel;

    @Schema(description = "Lista de recomendações de mitigação")
    private List<RiskRecommendation> recommendations;

    @Schema(description = "Score esperado após aplicar as recomendações", example = "50")
    private int expectedRiskScore;

    @Schema(description = "Nível de risco esperado após aplicar as recomendações", example = "MÉDIO")
    private String expectedRiskLevel;

    public RiskRecommendationResponse() {
    }

    public RiskRecommendationResponse(String pullRequestId, String ruleVersion, 
                                      int currentRiskScore, String currentRiskLevel,
                                      String targetRiskLevel, List<RiskRecommendation> recommendations,
                                      int expectedRiskScore, String expectedRiskLevel) {
        this.pullRequestId = pullRequestId;
        this.ruleVersion = ruleVersion;
        this.currentRiskScore = currentRiskScore;
        this.currentRiskLevel = currentRiskLevel;
        this.targetRiskLevel = targetRiskLevel;
        this.recommendations = recommendations;
        this.expectedRiskScore = expectedRiskScore;
        this.expectedRiskLevel = expectedRiskLevel;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public String getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(String ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    public int getCurrentRiskScore() {
        return currentRiskScore;
    }

    public void setCurrentRiskScore(int currentRiskScore) {
        this.currentRiskScore = currentRiskScore;
    }

    public String getCurrentRiskLevel() {
        return currentRiskLevel;
    }

    public void setCurrentRiskLevel(String currentRiskLevel) {
        this.currentRiskLevel = currentRiskLevel;
    }

    public String getTargetRiskLevel() {
        return targetRiskLevel;
    }

    public void setTargetRiskLevel(String targetRiskLevel) {
        this.targetRiskLevel = targetRiskLevel;
    }

    public List<RiskRecommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RiskRecommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public int getExpectedRiskScore() {
        return expectedRiskScore;
    }

    public void setExpectedRiskScore(int expectedRiskScore) {
        this.expectedRiskScore = expectedRiskScore;
    }

    public String getExpectedRiskLevel() {
        return expectedRiskLevel;
    }

    public void setExpectedRiskLevel(String expectedRiskLevel) {
        this.expectedRiskLevel = expectedRiskLevel;
    }
}
