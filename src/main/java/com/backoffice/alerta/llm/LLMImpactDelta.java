package com.backoffice.alerta.llm;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * US#71 - Delta de impacto para uma dimensão/métrica específica
 * 
 * Representa a variação entre PRE_STATE e POST_STATE
 */
@Schema(description = "Delta de impacto entre estado PRE e POST")
public class LLMImpactDelta {

    @Schema(description = "Dimensão analisada", example = "AST")
    private String dimension;

    @Schema(description = "Métrica comparada", example = "cyclomaticComplexity")
    private String metric;

    @Schema(description = "Valor antes da mudança", example = "2.1")
    private double beforeValue;

    @Schema(description = "Valor depois da mudança", example = "3.4")
    private double afterValue;

    @Schema(description = "Variação (afterValue - beforeValue)", example = "1.3")
    private double delta;

    @Schema(description = "Interpretação do delta", example = "DEGRADED")
    private String interpretation; // IMPROVED | DEGRADED | NEUTRAL

    // Constructors
    public LLMImpactDelta() {
    }

    public LLMImpactDelta(String dimension, String metric, double beforeValue, 
                         double afterValue, double delta, String interpretation) {
        this.dimension = dimension;
        this.metric = metric;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.delta = delta;
        this.interpretation = interpretation;
    }

    // Getters and Setters
    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public double getBeforeValue() {
        return beforeValue;
    }

    public void setBeforeValue(double beforeValue) {
        this.beforeValue = beforeValue;
    }

    public double getAfterValue() {
        return afterValue;
    }

    public void setAfterValue(double afterValue) {
        this.afterValue = afterValue;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }
}
