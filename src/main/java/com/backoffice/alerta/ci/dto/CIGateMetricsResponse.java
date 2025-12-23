package com.backoffice.alerta.ci.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Response DTO para métricas gerais do Gate de Risco CI/CD
 * 
 * US#54 - Observabilidade e Métricas do Gate de Risco (CI/CD)
 * 
 * READ-ONLY: Este DTO apenas retorna dados agregados de auditorias existentes.
 * Não cria side-effects, auditorias ou decisões.
 */
@Schema(description = "Métricas agregadas do Gate de Risco CI/CD")
public class CIGateMetricsResponse {

    @Schema(description = "Total de execuções do gate no período", example = "150")
    private int totalExecutions;

    @Schema(description = "Quantidade de PRs aprovados (exitCode=0)", example = "90")
    private int approvedCount;

    @Schema(description = "Quantidade de PRs aprovados com restrições (exitCode=1)", example = "35")
    private int approvedWithRestrictionsCount;

    @Schema(description = "Quantidade de PRs bloqueados (exitCode=2)", example = "25")
    private int blockedCount;

    @Schema(description = "Taxa de bloqueio (percentual)", example = "16.67")
    private double blockRate;

    @Schema(description = "Taxa de warnings (percentual)", example = "23.33")
    private double warningRate;

    @Schema(description = "Nível de risco médio das execuções", example = "MEDIO")
    private String averageRiskLevel;

    @Schema(description = "Data inicial do período analisado", example = "2025-10-01")
    private LocalDate from;

    @Schema(description = "Data final do período analisado", example = "2025-12-20")
    private LocalDate to;

    // Constructors
    public CIGateMetricsResponse() {}

    public CIGateMetricsResponse(int totalExecutions, int approvedCount, 
                                  int approvedWithRestrictionsCount, int blockedCount,
                                  double blockRate, double warningRate, 
                                  String averageRiskLevel, LocalDate from, LocalDate to) {
        this.totalExecutions = totalExecutions;
        this.approvedCount = approvedCount;
        this.approvedWithRestrictionsCount = approvedWithRestrictionsCount;
        this.blockedCount = blockedCount;
        this.blockRate = blockRate;
        this.warningRate = warningRate;
        this.averageRiskLevel = averageRiskLevel;
        this.from = from;
        this.to = to;
    }

    // Getters and Setters
    public int getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(int totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public int getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(int approvedCount) {
        this.approvedCount = approvedCount;
    }

    public int getApprovedWithRestrictionsCount() {
        return approvedWithRestrictionsCount;
    }

    public void setApprovedWithRestrictionsCount(int approvedWithRestrictionsCount) {
        this.approvedWithRestrictionsCount = approvedWithRestrictionsCount;
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

    public double getWarningRate() {
        return warningRate;
    }

    public void setWarningRate(double warningRate) {
        this.warningRate = warningRate;
    }

    public String getAverageRiskLevel() {
        return averageRiskLevel;
    }

    public void setAverageRiskLevel(String averageRiskLevel) {
        this.averageRiskLevel = averageRiskLevel;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }
}
