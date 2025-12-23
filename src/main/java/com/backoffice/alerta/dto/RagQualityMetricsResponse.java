package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * US#67 - Métricas de qualidade do RAG.
 * Read-only, determinísticas, calculadas on-the-fly.
 */
@Schema(description = "Métricas de qualidade do RAG (GLOBAL ou SCOPED por projeto)")
public class RagQualityMetricsResponse {

    @Schema(description = "Porcentagem de queries que usaram fallback (0.0 a 1.0)", example = "0.15")
    private Double fallbackRate;

    @Schema(description = "Média dos scores semânticos (apenas scores > 0)", example = "0.67")
    private Double avgSemanticScore;

    @Schema(description = "Média dos scores de keyword", example = "2.5")
    private Double avgKeywordScore;

    @Schema(description = "Porcentagem de matches híbridos (0.0 a 1.0)", example = "0.45")
    private Double hybridMatchRate;

    @Schema(description = "Porcentagem de matches apenas semânticos (0.0 a 1.0)", example = "0.35")
    private Double semanticOnlyRate;

    @Schema(description = "Porcentagem de matches apenas keyword (0.0 a 1.0)", example = "0.20")
    private Double keywordOnlyRate;

    @Schema(description = "Porcentagem de regras incluídas via fallback (0.0 a 1.0)", example = "0.10")
    private Double fallbackInclusionRate;

    @Schema(description = "Porcentagem de casos com confidence=HIGH mas score semântico médio < 0.4", example = "0.05")
    private Double confidenceMismatchRate;

    @Schema(description = "Contexto do projeto (null = GLOBAL)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectContext;

    @Schema(description = "Total de queries avaliadas", example = "100")
    private Integer totalQueriesEvaluated;

    @Schema(description = "Total de regras avaliadas", example = "25")
    private Integer totalRulesEvaluated;

    // Constructors
    public RagQualityMetricsResponse() {}

    // Getters and Setters
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

    public Double getAvgKeywordScore() {
        return avgKeywordScore;
    }

    public void setAvgKeywordScore(Double avgKeywordScore) {
        this.avgKeywordScore = avgKeywordScore;
    }

    public Double getHybridMatchRate() {
        return hybridMatchRate;
    }

    public void setHybridMatchRate(Double hybridMatchRate) {
        this.hybridMatchRate = hybridMatchRate;
    }

    public Double getSemanticOnlyRate() {
        return semanticOnlyRate;
    }

    public void setSemanticOnlyRate(Double semanticOnlyRate) {
        this.semanticOnlyRate = semanticOnlyRate;
    }

    public Double getKeywordOnlyRate() {
        return keywordOnlyRate;
    }

    public void setKeywordOnlyRate(Double keywordOnlyRate) {
        this.keywordOnlyRate = keywordOnlyRate;
    }

    public Double getFallbackInclusionRate() {
        return fallbackInclusionRate;
    }

    public void setFallbackInclusionRate(Double fallbackInclusionRate) {
        this.fallbackInclusionRate = fallbackInclusionRate;
    }

    public Double getConfidenceMismatchRate() {
        return confidenceMismatchRate;
    }

    public void setConfidenceMismatchRate(Double confidenceMismatchRate) {
        this.confidenceMismatchRate = confidenceMismatchRate;
    }

    public UUID getProjectContext() {
        return projectContext;
    }

    public void setProjectContext(UUID projectContext) {
        this.projectContext = projectContext;
    }

    public Integer getTotalQueriesEvaluated() {
        return totalQueriesEvaluated;
    }

    public void setTotalQueriesEvaluated(Integer totalQueriesEvaluated) {
        this.totalQueriesEvaluated = totalQueriesEvaluated;
    }

    public Integer getTotalRulesEvaluated() {
        return totalRulesEvaluated;
    }

    public void setTotalRulesEvaluated(Integer totalRulesEvaluated) {
        this.totalRulesEvaluated = totalRulesEvaluated;
    }
}
