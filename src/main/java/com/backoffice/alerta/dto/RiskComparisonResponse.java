package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultado da comparação de risco entre versões de regras")
public class RiskComparisonResponse {

    @Schema(description = "Identificador do Pull Request analisado", example = "PR-12345")
    private String pullRequestId;

    @Schema(description = "Versão inicial das regras", example = "v1")
    private String fromVersion;

    @Schema(description = "Versão final das regras", example = "v2")
    private String toVersion;

    @Schema(description = "Score de risco na versão inicial", example = "35")
    private Integer previousScore;

    @Schema(description = "Score de risco na versão final", example = "50")
    private Integer newScore;

    @Schema(description = "Diferença entre os scores (newScore - previousScore)", example = "15")
    private Integer riskDelta;

    @Schema(description = "Nível de risco na versão inicial", example = "MÉDIO")
    private String previousLevel;

    @Schema(description = "Nível de risco na versão final", example = "MÉDIO")
    private String newLevel;

    @Schema(
        description = "Decisão sobre a mudança de risco",
        example = "RISCO AUMENTOU",
        allowableValues = {"RISCO AUMENTOU", "RISCO DIMINUIU", "RISCO MANTEVE"}
    )
    private String decision;

    @Schema(description = "Explicação das diferenças entre as versões")
    private List<String> explanation;

    public RiskComparisonResponse() {
    }

    public RiskComparisonResponse(String pullRequestId, String fromVersion, String toVersion,
                                  Integer previousScore, Integer newScore, Integer riskDelta,
                                  String previousLevel, String newLevel, String decision,
                                  List<String> explanation) {
        this.pullRequestId = pullRequestId;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.previousScore = previousScore;
        this.newScore = newScore;
        this.riskDelta = riskDelta;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.decision = decision;
        this.explanation = explanation;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public String getFromVersion() {
        return fromVersion;
    }

    public void setFromVersion(String fromVersion) {
        this.fromVersion = fromVersion;
    }

    public String getToVersion() {
        return toVersion;
    }

    public void setToVersion(String toVersion) {
        this.toVersion = toVersion;
    }

    public Integer getPreviousScore() {
        return previousScore;
    }

    public void setPreviousScore(Integer previousScore) {
        this.previousScore = previousScore;
    }

    public Integer getNewScore() {
        return newScore;
    }

    public void setNewScore(Integer newScore) {
        this.newScore = newScore;
    }

    public Integer getRiskDelta() {
        return riskDelta;
    }

    public void setRiskDelta(Integer riskDelta) {
        this.riskDelta = riskDelta;
    }

    public String getPreviousLevel() {
        return previousLevel;
    }

    public void setPreviousLevel(String previousLevel) {
        this.previousLevel = previousLevel;
    }

    public String getNewLevel() {
        return newLevel;
    }

    public void setNewLevel(String newLevel) {
        this.newLevel = newLevel;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public List<String> getExplanation() {
        return explanation;
    }

    public void setExplanation(List<String> explanation) {
        this.explanation = explanation;
    }
}
