package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Recomendação de ação para reduzir o risco")
public class RiskRecommendation {

    @Schema(
        description = "Tipo de ação recomendada",
        example = "ADICIONAR_TESTES",
        allowableValues = {"ADICIONAR_TESTES", "REDUZIR_LINHAS_ALTERADAS", "SEGMENTAR_MODULO", "REVISAO_MANUAL"}
    )
    private String action;

    @Schema(description = "Descrição detalhada da ação", example = "Adicionar testes automatizados para BillingService.java")
    private String description;

    @Schema(description = "Impacto estimado no score (negativo = redução de risco)", example = "-25")
    private int estimatedImpact;

    public RiskRecommendation() {
    }

    public RiskRecommendation(String action, String description, int estimatedImpact) {
        this.action = action;
        this.description = description;
        this.estimatedImpact = estimatedImpact;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEstimatedImpact() {
        return estimatedImpact;
    }

    public void setEstimatedImpact(int estimatedImpact) {
        this.estimatedImpact = estimatedImpact;
    }
}
