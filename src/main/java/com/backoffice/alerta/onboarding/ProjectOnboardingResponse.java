package com.backoffice.alerta.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * US#72 - Response do onboarding de projeto
 */
@Schema(description = "Resultado do onboarding de projeto real")
public class ProjectOnboardingResponse {

    @Schema(description = "ID do projeto")
    private UUID projectId;

    @Schema(description = "Nome do projeto")
    private String projectName;

    @Schema(description = "Status do onboarding", example = "ONBOARDED")
    private String status; // NEW | ONBOARDING | ONBOARDED | FAILED

    @Schema(description = "Quantidade de regras importadas")
    private int rulesImported;

    @Schema(description = "Quantidade de embeddings indexados")
    private int embeddingsIndexed;

    @Schema(description = "Cobertura AST", example = "PARTIAL")
    private String astCoverage; // NONE | PARTIAL | FULL

    @Schema(description = "Status RAG", example = "FULL")
    private String ragStatus; // LIMITED | FULL

    @Schema(description = "Risco baseline", example = "MEDIUM")
    private String baselineRisk; // LOW | MEDIUM | HIGH

    @Schema(description = "Limitações detectadas")
    private List<String> limitations = new ArrayList<>();

    public ProjectOnboardingResponse() {}

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRulesImported() {
        return rulesImported;
    }

    public void setRulesImported(int rulesImported) {
        this.rulesImported = rulesImported;
    }

    public int getEmbeddingsIndexed() {
        return embeddingsIndexed;
    }

    public void setEmbeddingsIndexed(int embeddingsIndexed) {
        this.embeddingsIndexed = embeddingsIndexed;
    }

    public String getAstCoverage() {
        return astCoverage;
    }

    public void setAstCoverage(String astCoverage) {
        this.astCoverage = astCoverage;
    }

    public String getRagStatus() {
        return ragStatus;
    }

    public void setRagStatus(String ragStatus) {
        this.ragStatus = ragStatus;
    }

    public String getBaselineRisk() {
        return baselineRisk;
    }

    public void setBaselineRisk(String baselineRisk) {
        this.baselineRisk = baselineRisk;
    }

    public List<String> getLimitations() {
        return limitations;
    }

    public void setLimitations(List<String> limitations) {
        this.limitations = limitations;
    }
}
