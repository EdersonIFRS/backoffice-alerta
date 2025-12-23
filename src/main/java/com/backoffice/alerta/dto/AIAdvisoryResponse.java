package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response com análise consultiva de IA sobre riscos
 */
@Schema(description = "Resposta com análise consultiva de IA")
public class AIAdvisoryResponse {

    @Schema(description = "Visão executiva para gestores")
    private String executiveInsight;

    @Schema(description = "Interpretação do nível de risco em linguagem natural")
    private String riskInterpretation;

    @Schema(description = "Alerta sobre padrões históricos identificados")
    private String historicalPatternAlert;

    @Schema(description = "Recomendações preventivas")
    private List<String> preventiveRecommendations;

    @Schema(description = "Nível de confiança da análise", example = "Alta")
    private String confidenceLevel;

    public AIAdvisoryResponse() {
    }

    public AIAdvisoryResponse(String executiveInsight, String riskInterpretation,
                             String historicalPatternAlert, List<String> preventiveRecommendations,
                             String confidenceLevel) {
        this.executiveInsight = executiveInsight;
        this.riskInterpretation = riskInterpretation;
        this.historicalPatternAlert = historicalPatternAlert;
        this.preventiveRecommendations = preventiveRecommendations;
        this.confidenceLevel = confidenceLevel;
    }

    public String getExecutiveInsight() {
        return executiveInsight;
    }

    public void setExecutiveInsight(String executiveInsight) {
        this.executiveInsight = executiveInsight;
    }

    public String getRiskInterpretation() {
        return riskInterpretation;
    }

    public void setRiskInterpretation(String riskInterpretation) {
        this.riskInterpretation = riskInterpretation;
    }

    public String getHistoricalPatternAlert() {
        return historicalPatternAlert;
    }

    public void setHistoricalPatternAlert(String historicalPatternAlert) {
        this.historicalPatternAlert = historicalPatternAlert;
    }

    public List<String> getPreventiveRecommendations() {
        return preventiveRecommendations;
    }

    public void setPreventiveRecommendations(List<String> preventiveRecommendations) {
        this.preventiveRecommendations = preventiveRecommendations;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }
}
