package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response com sugestão de ajuste de risco (read-only)
 */
@Schema(description = "Sugestão de ajuste de risco baseada em aprendizado organizacional")
public class RiskAdjustmentSuggestionResponse {

    @Schema(description = "ID único da sugestão", example = "a1b2c3d4-e5f6-7890-abcd-1234567890ab")
    private UUID id;

    @Schema(description = "ID da regra de negócio", example = "PAY-001")
    private String businessRuleId;

    @Schema(description = "Nome da regra de negócio", example = "Processamento de Pagamentos")
    private String ruleName;

    @Schema(description = "Tipo de ajuste sugerido", example = "CRITICALITY_LEVEL")
    private AdjustmentSuggestionType suggestionType;

    @Schema(description = "Valor atual", example = "ALTA")
    private String currentValue;

    @Schema(description = "Valor sugerido", example = "CRITICA")
    private String suggestedValue;

    @Schema(description = "Nível de confiança da sugestão (0-100)", example = "85")
    private int confidenceLevel;

    @Schema(description = "Sinal de aprendizado detectado", example = "FALSE_NEGATIVE_TREND")
    private LearningSignal learningSignal;

    @Schema(description = "Resumo das evidências que embasam a sugestão",
            example = "Detectados 3 incidentes nos últimos 30 dias após aprovações de mudanças nesta regra. " +
                     "Todos os casos tiveram severidade HIGH ou CRITICAL em produção.")
    private String evidenceSummary;

    @Schema(description = "Data e hora da criação da sugestão", example = "2025-12-17T21:00:00Z")
    private Instant createdAt;

    public RiskAdjustmentSuggestionResponse() {
    }

    /**
     * Construtor a partir da entidade RiskAdjustmentSuggestion
     */
    public RiskAdjustmentSuggestionResponse(RiskAdjustmentSuggestion suggestion) {
        this.id = suggestion.getId();
        this.businessRuleId = suggestion.getBusinessRuleId();
        this.ruleName = suggestion.getRuleName();
        this.suggestionType = suggestion.getSuggestionType();
        this.currentValue = suggestion.getCurrentValue();
        this.suggestedValue = suggestion.getSuggestedValue();
        this.confidenceLevel = suggestion.getConfidenceLevel();
        this.learningSignal = suggestion.getLearningSignal();
        this.evidenceSummary = suggestion.getEvidenceSummary();
        this.createdAt = suggestion.getCreatedAt();
    }

    // Apenas getters - sem setters (read-only)

    public UUID getId() {
        return id;
    }

    public String getBusinessRuleId() {
        return businessRuleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public AdjustmentSuggestionType getSuggestionType() {
        return suggestionType;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public String getSuggestedValue() {
        return suggestedValue;
    }

    public int getConfidenceLevel() {
        return confidenceLevel;
    }

    public LearningSignal getLearningSignal() {
        return learningSignal;
    }

    public String getEvidenceSummary() {
        return evidenceSummary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
