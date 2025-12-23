package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response com resumo estatístico de SLAs de risco
 */
@Schema(description = "Resumo estatístico de SLAs de resposta organizacional")
public class SlaSummaryResponse {

    @Schema(description = "Total de SLAs rastreados", example = "25")
    private final int totalTracked;

    @Schema(description = "Quantidade de SLAs vencidos", example = "3")
    private final int breachedCount;

    @Schema(description = "Quantidade de SLAs escalonados", example = "5")
    private final int escalatedCount;

    @Schema(description = "Tempo médio de resposta em minutos", example = "45.5")
    private final double averageResponseTimeMinutes;

    public SlaSummaryResponse(int totalTracked, 
                             int breachedCount, 
                             int escalatedCount,
                             double averageResponseTimeMinutes) {
        this.totalTracked = totalTracked;
        this.breachedCount = breachedCount;
        this.escalatedCount = escalatedCount;
        this.averageResponseTimeMinutes = averageResponseTimeMinutes;
    }

    public int getTotalTracked() {
        return totalTracked;
    }

    public int getBreachedCount() {
        return breachedCount;
    }

    public int getEscalatedCount() {
        return escalatedCount;
    }

    public double getAverageResponseTimeMinutes() {
        return averageResponseTimeMinutes;
    }
}
