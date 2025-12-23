package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request para análise de ajustes de risco baseada em aprendizado
 */
@Schema(description = "Requisição para análise de sugestões de ajuste de risco")
public class RiskAdjustmentAnalysisRequest {

    @Schema(description = "Janela de tempo em dias para análise de histórico",
            example = "30",
            minimum = "1",
            defaultValue = "30")
    private Integer timeWindowDays;

    @Schema(description = "Nível mínimo de confiança para sugestões (0-100)",
            example = "70",
            minimum = "0",
            maximum = "100",
            defaultValue = "50")
    private Integer minimumConfidence;

    public RiskAdjustmentAnalysisRequest() {
        this.timeWindowDays = 30;
        this.minimumConfidence = 50;
    }

    public RiskAdjustmentAnalysisRequest(Integer timeWindowDays, Integer minimumConfidence) {
        this.timeWindowDays = timeWindowDays;
        this.minimumConfidence = minimumConfidence;
    }

    public Integer getTimeWindowDays() {
        return timeWindowDays;
    }

    public void setTimeWindowDays(Integer timeWindowDays) {
        this.timeWindowDays = timeWindowDays;
    }

    public Integer getMinimumConfidence() {
        return minimumConfidence;
    }

    public void setMinimumConfidence(Integer minimumConfidence) {
        this.minimumConfidence = minimumConfidence;
    }
}
