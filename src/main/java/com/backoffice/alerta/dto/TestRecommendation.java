package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Recomendação de teste automatizado")
public class TestRecommendation {

    @Schema(description = "Caminho do arquivo que precisa de teste", example = "PaymentService.java")
    private String filePath;

    @Schema(description = "Tipo de teste recomendado", example = "UNIT")
    private String testType;

    @Schema(description = "Descrição da recomendação de teste", 
            example = "Adicionar testes unitários para validação de transações e cálculo de descontos")
    private String description;

    @Schema(description = "Impacto estimado na cobertura (%)", example = "30")
    private Integer estimatedImpact;

    public TestRecommendation() {
    }

    public TestRecommendation(String filePath, String testType, String description, Integer estimatedImpact) {
        this.filePath = filePath;
        this.testType = testType;
        this.description = description;
        this.estimatedImpact = estimatedImpact;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEstimatedImpact() {
        return estimatedImpact;
    }

    public void setEstimatedImpact(Integer estimatedImpact) {
        this.estimatedImpact = estimatedImpact;
    }
}
