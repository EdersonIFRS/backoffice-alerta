package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultado da simulação comparando situação atual vs. situação simulada")
public class RiskSimulationResponse {

    @Schema(description = "Identificador do Pull Request analisado", example = "PR-12345")
    private String pullRequestId;

    @Schema(description = "Versão das regras aplicada na análise", example = "v2")
    private String ruleVersion;

    @Schema(description = "Score de risco na situação atual", example = "65")
    private int currentScore;

    @Schema(description = "Score de risco na situação simulada", example = "40")
    private int simulatedScore;

    @Schema(description = "Diferença entre score simulado e atual (negativo = melhora)", example = "-25")
    private int riskDelta;

    @Schema(description = "Nível de risco atual", example = "ALTO")
    private String currentLevel;

    @Schema(description = "Nível de risco simulado", example = "MÉDIO")
    private String simulatedLevel;

    @Schema(description = "Interpretação do resultado", example = "RISCO DIMINUIU", allowableValues = {"RISCO AUMENTOU", "RISCO DIMINUIU", "RISCO MANTEVE"})
    private String decision;

    @Schema(description = "Lista de explicações conceituais sobre as mudanças causadas pela simulação")
    private List<String> explanation;

    public RiskSimulationResponse() {
    }

    public RiskSimulationResponse(String pullRequestId, String ruleVersion,
                                  int currentScore, int simulatedScore, int riskDelta,
                                  String currentLevel, String simulatedLevel, 
                                  String decision, List<String> explanation) {
        this.pullRequestId = pullRequestId;
        this.ruleVersion = ruleVersion;
        this.currentScore = currentScore;
        this.simulatedScore = simulatedScore;
        this.riskDelta = riskDelta;
        this.currentLevel = currentLevel;
        this.simulatedLevel = simulatedLevel;
        this.decision = decision;
        this.explanation = explanation;
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

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public int getSimulatedScore() {
        return simulatedScore;
    }

    public void setSimulatedScore(int simulatedScore) {
        this.simulatedScore = simulatedScore;
    }

    public int getRiskDelta() {
        return riskDelta;
    }

    public void setRiskDelta(int riskDelta) {
        this.riskDelta = riskDelta;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(String currentLevel) {
        this.currentLevel = currentLevel;
    }

    public String getSimulatedLevel() {
        return simulatedLevel;
    }

    public void setSimulatedLevel(String simulatedLevel) {
        this.simulatedLevel = simulatedLevel;
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
