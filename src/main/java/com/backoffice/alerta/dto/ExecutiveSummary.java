package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resumo executivo do risco em linguagem de negócio")
public class ExecutiveSummary {

    @Schema(description = "Nível de risco", example = "CRÍTICO", allowableValues = {"BAIXO", "MÉDIO", "ALTO", "CRÍTICO"})
    private String riskLevel;

    @Schema(description = "Decisão da política", example = "BLOQUEADO", allowableValues = {"APROVADO", "REVISÃO OBRIGATÓRIA", "BLOQUEADO"})
    private String decision;

    @Schema(description = "Frase de impacto executiva", example = "Pull Request apresenta risco crítico para operação")
    private String headline;

    @Schema(description = "Resumo executivo do risco em linguagem de negócio")
    private String summary;

    @Schema(description = "Fatores-chave que contribuem para o risco")
    private List<String> keyFactors;

    @Schema(description = "Ações recomendadas para mitigação")
    private List<String> recommendedActions;

    public ExecutiveSummary() {
    }

    public ExecutiveSummary(String riskLevel, String decision, String headline, 
                           String summary, List<String> keyFactors, List<String> recommendedActions) {
        this.riskLevel = riskLevel;
        this.decision = decision;
        this.headline = headline;
        this.summary = summary;
        this.keyFactors = keyFactors;
        this.recommendedActions = recommendedActions;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getKeyFactors() {
        return keyFactors;
    }

    public void setKeyFactors(List<String> keyFactors) {
        this.keyFactors = keyFactors;
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions;
    }
}
