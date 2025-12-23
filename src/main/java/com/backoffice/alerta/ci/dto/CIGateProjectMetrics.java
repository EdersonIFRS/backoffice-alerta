package com.backoffice.alerta.ci.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO para métricas do Gate de Risco agrupadas por Projeto
 * 
 * US#54 - Observabilidade e Métricas do Gate de Risco (CI/CD)
 * 
 * READ-ONLY: Apenas retorna dados agregados, sem side-effects.
 */
@Schema(description = "Métricas do Gate de Risco agrupadas por projeto")
public class CIGateProjectMetrics {

    @Schema(description = "ID do projeto", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID projectId;

    @Schema(description = "Nome do projeto", example = "Backoffice Pagamentos")
    private String projectName;

    @Schema(description = "Total de execuções do gate neste projeto", example = "45")
    private int totalExecutions;

    @Schema(description = "Quantidade de bloqueios neste projeto", example = "12")
    private int blockedCount;

    @Schema(description = "Taxa de bloqueio (percentual)", example = "26.67")
    private double blockRate;

    @Schema(description = "Nível de risco mais frequente", example = "ALTO")
    private String mostFrequentRiskLevel;

    @Schema(description = "Data/hora da última execução", example = "2025-12-20T15:30:00Z")
    private Instant lastExecutionAt;

    // Constructors
    public CIGateProjectMetrics() {}

    public CIGateProjectMetrics(UUID projectId, String projectName, int totalExecutions,
                                 int blockedCount, double blockRate, 
                                 String mostFrequentRiskLevel, Instant lastExecutionAt) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.totalExecutions = totalExecutions;
        this.blockedCount = blockedCount;
        this.blockRate = blockRate;
        this.mostFrequentRiskLevel = mostFrequentRiskLevel;
        this.lastExecutionAt = lastExecutionAt;
    }

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

    public int getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(int totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public int getBlockedCount() {
        return blockedCount;
    }

    public void setBlockedCount(int blockedCount) {
        this.blockedCount = blockedCount;
    }

    public double getBlockRate() {
        return blockRate;
    }

    public void setBlockRate(double blockRate) {
        this.blockRate = blockRate;
    }

    public String getMostFrequentRiskLevel() {
        return mostFrequentRiskLevel;
    }

    public void setMostFrequentRiskLevel(String mostFrequentRiskLevel) {
        this.mostFrequentRiskLevel = mostFrequentRiskLevel;
    }

    public Instant getLastExecutionAt() {
        return lastExecutionAt;
    }

    public void setLastExecutionAt(Instant lastExecutionAt) {
        this.lastExecutionAt = lastExecutionAt;
    }
}
