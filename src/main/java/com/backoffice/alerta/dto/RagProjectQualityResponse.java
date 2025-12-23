package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * US#67 - Qualidade do RAG por projeto.
 */
@Schema(description = "Métricas de qualidade do RAG por projeto")
public class RagProjectQualityResponse {

    @Schema(description = "ID do projeto", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @Schema(description = "Nome do projeto", example = "Backoffice Pagamentos")
    private String projectName;

    @Schema(description = "Métricas de qualidade do RAG para este projeto")
    private RagQualityMetricsResponse metrics;

    // Constructors
    public RagProjectQualityResponse() {}

    // Getters and Setters
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

    public RagQualityMetricsResponse getMetrics() {
        return metrics;
    }

    public void setMetrics(RagQualityMetricsResponse metrics) {
        this.metrics = metrics;
    }
}
