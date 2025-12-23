package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * US#67 - Ponto de tendência de qualidade do RAG ao longo do tempo.
 * READ-ONLY (calculado sob demanda, sem persistência).
 */
@Schema(description = "Ponto de tendência de qualidade do RAG")
public class RagQualityTrendPoint {

    @Schema(description = "Data do ponto de medição", example = "2025-12-21")
    private LocalDate date;

    @Schema(description = "Taxa de fallback nesta data (0.0 a 1.0)", example = "0.12")
    private Double fallbackRate;

    @Schema(description = "Score semântico médio nesta data (0.0 a 1.0)", example = "0.68")
    private Double avgSemanticScore;

    @Schema(description = "Número de queries avaliadas nesta data", example = "45")
    private Integer queryCount;

    // Constructors
    public RagQualityTrendPoint() {}

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getFallbackRate() {
        return fallbackRate;
    }

    public void setFallbackRate(Double fallbackRate) {
        this.fallbackRate = fallbackRate;
    }

    public Double getAvgSemanticScore() {
        return avgSemanticScore;
    }

    public void setAvgSemanticScore(Double avgSemanticScore) {
        this.avgSemanticScore = avgSemanticScore;
    }

    public Integer getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(Integer queryCount) {
        this.queryCount = queryCount;
    }
}
