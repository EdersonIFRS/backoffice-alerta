package com.backoffice.alerta.ci.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO para métricas do Gate de Risco agrupadas por Regra de Negócio
 * 
 * US#54 - Observabilidade e Métricas do Gate de Risco (CI/CD)
 * 
 * READ-ONLY: Apenas retorna dados agregados, sem side-effects.
 */
@Schema(description = "Métricas do Gate de Risco agrupadas por regra de negócio")
public class CIGateRuleMetrics {

    @Schema(description = "ID da regra de negócio", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID businessRuleId;

    @Schema(description = "Nome da regra", example = "REGRA_CALCULO_HORAS_PJ")
    private String ruleName;

    @Schema(description = "Criticidade da regra", example = "ALTA")
    private String criticality;

    @Schema(description = "Quantidade de vezes que causou bloqueio", example = "8")
    private int blockCount;

    @Schema(description = "Quantidade de vezes que causou warning", example = "15")
    private int warningCount;

    @Schema(description = "Data/hora em que foi acionada pela última vez", example = "2025-12-20T14:22:00Z")
    private Instant lastTriggeredAt;

    // Constructors
    public CIGateRuleMetrics() {}

    public CIGateRuleMetrics(UUID businessRuleId, String ruleName, String criticality,
                             int blockCount, int warningCount, Instant lastTriggeredAt) {
        this.businessRuleId = businessRuleId;
        this.ruleName = ruleName;
        this.criticality = criticality;
        this.blockCount = blockCount;
        this.warningCount = warningCount;
        this.lastTriggeredAt = lastTriggeredAt;
    }

    // Getters and Setters
    public UUID getBusinessRuleId() {
        return businessRuleId;
    }

    public void setBusinessRuleId(UUID businessRuleId) {
        this.businessRuleId = businessRuleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getCriticality() {
        return criticality;
    }

    public void setCriticality(String criticality) {
        this.criticality = criticality;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public Instant getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(Instant lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }
}
