package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * US#67 - Avaliação de qualidade por regra de negócio.
 */
@Schema(description = "Métricas de qualidade do RAG por regra de negócio")
public class RagRuleQualityResponse {

    @Schema(description = "ID da regra de negócio", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID businessRuleId;

    @Schema(description = "Nome da regra de negócio", example = "REGRA_CALCULO_HORAS_PJ")
    private String businessRuleName;

    @Schema(description = "Média dos scores semânticos para esta regra (0.0 a 1.0)", example = "0.72")
    private Double avgSemanticScore;

    @Schema(description = "Taxa de dependência de keyword (% matches keyword-only, 0.0 a 1.0)", example = "0.25")
    private Double keywordDependencyRate;

    @Schema(description = "Taxa de inclusão via fallback (0.0 a 1.0)", example = "0.15")
    private Double fallbackInclusionRate;

    @Schema(description = "Número de vezes que a regra foi retornada", example = "50")
    private Integer occurrences;

    @Schema(description = "Observações sobre a qualidade da regra", 
            example = "Regra com boa performance semântica, baixa dependência de keywords")
    private String observations;

    // Constructors
    public RagRuleQualityResponse() {}

    // Getters and Setters
    public UUID getBusinessRuleId() {
        return businessRuleId;
    }

    public void setBusinessRuleId(UUID businessRuleId) {
        this.businessRuleId = businessRuleId;
    }

    public String getBusinessRuleName() {
        return businessRuleName;
    }

    public void setBusinessRuleName(String businessRuleName) {
        this.businessRuleName = businessRuleName;
    }

    public Double getAvgSemanticScore() {
        return avgSemanticScore;
    }

    public void setAvgSemanticScore(Double avgSemanticScore) {
        this.avgSemanticScore = avgSemanticScore;
    }

    public Double getKeywordDependencyRate() {
        return keywordDependencyRate;
    }

    public void setKeywordDependencyRate(Double keywordDependencyRate) {
        this.keywordDependencyRate = keywordDependencyRate;
    }

    public Double getFallbackInclusionRate() {
        return fallbackInclusionRate;
    }

    public void setFallbackInclusionRate(Double fallbackInclusionRate) {
        this.fallbackInclusionRate = fallbackInclusionRate;
    }

    public Integer getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(Integer occurrences) {
        this.occurrences = occurrences;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
