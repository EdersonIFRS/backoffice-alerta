package com.backoffice.alerta.rules;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade imutável de sugestão de ajuste de risco
 * Gerada através de análise de aprendizado organizacional
 * 
 * ⚠️ IMUTÁVEL - não pode ser alterada após criação
 * ⚠️ CONSULTIVA - não aplica mudanças automaticamente
 * ⚠️ Baseada em evidências reais de feedbacks e incidentes
 */
public class RiskAdjustmentSuggestion {

    private final UUID id;
    private final String businessRuleId;
    private final String ruleName;
    private final AdjustmentSuggestionType suggestionType;
    private final String currentValue;
    private final String suggestedValue;
    private final int confidenceLevel; // 0-100
    private final LearningSignal learningSignal;
    private final String evidenceSummary;
    private final Instant createdAt;

    /**
     * Construtor completo
     * @param businessRuleId ID da regra de negócio afetada
     * @param ruleName Nome da regra de negócio
     * @param suggestionType Tipo de ajuste sugerido
     * @param currentValue Valor atual
     * @param suggestedValue Valor sugerido
     * @param confidenceLevel Nível de confiança (0-100)
     * @param learningSignal Sinal de aprendizado detectado
     * @param evidenceSummary Resumo das evidências que embasam a sugestão
     */
    public RiskAdjustmentSuggestion(String businessRuleId,
                                   String ruleName,
                                   AdjustmentSuggestionType suggestionType,
                                   String currentValue,
                                   String suggestedValue,
                                   int confidenceLevel,
                                   LearningSignal learningSignal,
                                   String evidenceSummary) {
        this.id = UUID.randomUUID();
        this.businessRuleId = businessRuleId;
        this.ruleName = ruleName;
        this.suggestionType = suggestionType;
        this.currentValue = currentValue;
        this.suggestedValue = suggestedValue;
        this.confidenceLevel = Math.max(0, Math.min(100, confidenceLevel)); // Garante 0-100
        this.learningSignal = learningSignal;
        this.evidenceSummary = evidenceSummary;
        this.createdAt = Instant.now();
    }

    // Apenas getters - sem setters para garantir imutabilidade

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
