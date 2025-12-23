package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Relatório consolidado de risco contendo decisão, explicação executiva e recomendações")
public class RiskReport {

    @Schema(description = "Nível de risco do Pull Request", example = "CRÍTICO")
    private String riskLevel;

    @Schema(description = "Decisão da política de risco", example = "BLOQUEADO")
    private String decision;

    @Schema(description = "Sumário executivo em linguagem de negócio")
    private ExecutiveSummary executiveSummary;

    @Schema(description = "Lista de recomendações de mitigação")
    private List<String> recommendations;

    @Schema(description = "Timestamp de geração do relatório (ISO-8601 UTC)", example = "2025-01-15T10:30:00Z")
    private String generatedAt;

    public RiskReport() {
    }

    public RiskReport(String riskLevel, String decision, ExecutiveSummary executiveSummary, 
                     List<String> recommendations, String generatedAt) {
        this.riskLevel = riskLevel;
        this.decision = decision;
        this.executiveSummary = executiveSummary;
        this.recommendations = recommendations;
        this.generatedAt = generatedAt;
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

    public ExecutiveSummary getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(ExecutiveSummary executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}
