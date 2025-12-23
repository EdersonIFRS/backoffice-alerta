package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response com análise completa de sugestões de ajuste de risco
 */
@Schema(description = "Resultado da análise de aprendizado organizacional com sugestões de ajuste")
public class RiskAdjustmentAnalysisResponse {

    @Schema(description = "Lista de sugestões de ajuste geradas")
    private List<RiskAdjustmentSuggestionResponse> suggestions;

    @Schema(description = "Total de feedbacks analisados", example = "45")
    private int totalFeedbacksAnalyzed;

    @Schema(description = "Total de auditorias analisadas", example = "120")
    private int totalAuditsAnalyzed;

    @Schema(description = "Total de incidentes analisados", example = "8")
    private int totalIncidentsAnalyzed;

    @Schema(description = "Janela de tempo analisada em dias", example = "30")
    private int timeWindowDays;

    @Schema(description = "Resumo da análise realizada")
    private String analysisSummary;

    public RiskAdjustmentAnalysisResponse() {
    }

    public RiskAdjustmentAnalysisResponse(List<RiskAdjustmentSuggestionResponse> suggestions,
                                         int totalFeedbacksAnalyzed,
                                         int totalAuditsAnalyzed,
                                         int totalIncidentsAnalyzed,
                                         int timeWindowDays,
                                         String analysisSummary) {
        this.suggestions = suggestions;
        this.totalFeedbacksAnalyzed = totalFeedbacksAnalyzed;
        this.totalAuditsAnalyzed = totalAuditsAnalyzed;
        this.totalIncidentsAnalyzed = totalIncidentsAnalyzed;
        this.timeWindowDays = timeWindowDays;
        this.analysisSummary = analysisSummary;
    }

    public List<RiskAdjustmentSuggestionResponse> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<RiskAdjustmentSuggestionResponse> suggestions) {
        this.suggestions = suggestions;
    }

    public int getTotalFeedbacksAnalyzed() {
        return totalFeedbacksAnalyzed;
    }

    public void setTotalFeedbacksAnalyzed(int totalFeedbacksAnalyzed) {
        this.totalFeedbacksAnalyzed = totalFeedbacksAnalyzed;
    }

    public int getTotalAuditsAnalyzed() {
        return totalAuditsAnalyzed;
    }

    public void setTotalAuditsAnalyzed(int totalAuditsAnalyzed) {
        this.totalAuditsAnalyzed = totalAuditsAnalyzed;
    }

    public int getTotalIncidentsAnalyzed() {
        return totalIncidentsAnalyzed;
    }

    public void setTotalIncidentsAnalyzed(int totalIncidentsAnalyzed) {
        this.totalIncidentsAnalyzed = totalIncidentsAnalyzed;
    }

    public int getTimeWindowDays() {
        return timeWindowDays;
    }

    public void setTimeWindowDays(int timeWindowDays) {
        this.timeWindowDays = timeWindowDays;
    }

    public String getAnalysisSummary() {
        return analysisSummary;
    }

    public void setAnalysisSummary(String analysisSummary) {
        this.analysisSummary = analysisSummary;
    }
}
