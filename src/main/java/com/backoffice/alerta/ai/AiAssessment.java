package com.backoffice.alerta.ai;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Avaliação inteligente da mudança")
public class AiAssessment {

    @Schema(description = "Resumo textual da análise", example = "Mudança altera lógica sensível de billing")
    private String summary;

    @Schema(description = "Nível de confiança da análise (0.0 a 1.0)", example = "0.85")
    private double confidence;

    @Schema(description = "Nível de atenção recomendado", example = "ALTA", allowableValues = {"BAIXA", "MEDIA", "ALTA"})
    private AttentionLevel recommendedAttention;

    @Schema(description = "Sinais identificados pela análise")
    private List<AiSignal> signals;

    public AiAssessment() {
    }

    public AiAssessment(String summary, double confidence, AttentionLevel recommendedAttention, List<AiSignal> signals) {
        this.summary = summary;
        this.confidence = confidence;
        this.recommendedAttention = recommendedAttention;
        this.signals = signals;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public AttentionLevel getRecommendedAttention() {
        return recommendedAttention;
    }

    public void setRecommendedAttention(AttentionLevel recommendedAttention) {
        this.recommendedAttention = recommendedAttention;
    }

    public List<AiSignal> getSignals() {
        return signals;
    }

    public void setSignals(List<AiSignal> signals) {
        this.signals = signals;
    }
}
