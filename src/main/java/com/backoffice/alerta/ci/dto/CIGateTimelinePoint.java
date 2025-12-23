package com.backoffice.alerta.ci.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Response DTO para um ponto na timeline de execuções do Gate
 * 
 * US#54 - Observabilidade e Métricas do Gate de Risco (CI/CD)
 * 
 * READ-ONLY: Apenas retorna dados agregados por data, sem side-effects.
 */
@Schema(description = "Ponto na linha do tempo de execuções do Gate de Risco")
public class CIGateTimelinePoint {

    @Schema(description = "Data da agregação", example = "2025-12-20")
    private LocalDate date;

    @Schema(description = "Total de execuções neste dia", example = "12")
    private int executions;

    @Schema(description = "Quantidade de aprovados", example = "7")
    private int approved;

    @Schema(description = "Quantidade de warnings", example = "3")
    private int warnings;

    @Schema(description = "Quantidade de bloqueados", example = "2")
    private int blocked;

    // Constructors
    public CIGateTimelinePoint() {}

    public CIGateTimelinePoint(LocalDate date, int executions, int approved, 
                               int warnings, int blocked) {
        this.date = date;
        this.executions = executions;
        this.approved = approved;
        this.warnings = warnings;
        this.blocked = blocked;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getExecutions() {
        return executions;
    }

    public void setExecutions(int executions) {
        this.executions = executions;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public int getWarnings() {
        return warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public int getBlocked() {
        return blocked;
    }

    public void setBlocked(int blocked) {
        this.blocked = blocked;
    }
}
