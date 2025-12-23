package com.backoffice.alerta.llm;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * US#71 - Response da comparação PRE vs POST
 * 
 * Contém deltas por dimensão e veredicto final
 */
@Schema(description = "Resultado da comparação de impacto PRE vs POST")
public class LLMImpactComparisonResponse {

    @Schema(description = "Delta do score final", example = "-18")
    private int finalScoreDelta;

    @Schema(description = "Veredicto final", example = "DEGRADED")
    private String finalVerdict; // IMPROVED | DEGRADED | UNCHANGED

    @Schema(description = "Lista de deltas por dimensão/métrica")
    private List<LLMImpactDelta> deltas = new ArrayList<>();

    @Schema(description = "Sumário executivo da comparação")
    private String executiveSummary;

    @Schema(description = "Referência base comparada", example = "main")
    private String baseRef;

    @Schema(description = "Referência comparada", example = "PR#123")
    private String compareRef;

    // Constructors
    public LLMImpactComparisonResponse() {
    }

    public LLMImpactComparisonResponse(int finalScoreDelta, String finalVerdict, 
                                      List<LLMImpactDelta> deltas, String executiveSummary) {
        this.finalScoreDelta = finalScoreDelta;
        this.finalVerdict = finalVerdict;
        this.deltas = deltas;
        this.executiveSummary = executiveSummary;
    }

    // Getters and Setters
    public int getFinalScoreDelta() {
        return finalScoreDelta;
    }

    public void setFinalScoreDelta(int finalScoreDelta) {
        this.finalScoreDelta = finalScoreDelta;
    }

    public String getFinalVerdict() {
        return finalVerdict;
    }

    public void setFinalVerdict(String finalVerdict) {
        this.finalVerdict = finalVerdict;
    }

    public List<LLMImpactDelta> getDeltas() {
        return deltas;
    }

    public void setDeltas(List<LLMImpactDelta> deltas) {
        this.deltas = deltas;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public String getBaseRef() {
        return baseRef;
    }

    public void setBaseRef(String baseRef) {
        this.baseRef = baseRef;
    }

    public String getCompareRef() {
        return compareRef;
    }

    public void setCompareRef(String compareRef) {
        this.compareRef = compareRef;
    }
}
