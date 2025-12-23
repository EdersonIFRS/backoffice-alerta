package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/**
 * Response com resumo/agregação de notificações organizacionais
 */
@Schema(description = "Resumo estatístico de notificações organizacionais")
public class NotificationSummaryResponse {

    @Schema(description = "Total de notificações geradas", example = "42")
    private final int totalNotifications;

    @Schema(description = "Quantidade de notificações críticas", example = "7")
    private final int criticalCount;

    @Schema(description = "Times/equipes afetadas", example = "[\"Time de Pagamentos\", \"Engenharia Backend\"]")
    private final Set<String> affectedTeams;

    public NotificationSummaryResponse(int totalNotifications, 
                                      int criticalCount, 
                                      Set<String> affectedTeams) {
        this.totalNotifications = totalNotifications;
        this.criticalCount = criticalCount;
        this.affectedTeams = affectedTeams;
    }

    public int getTotalNotifications() {
        return totalNotifications;
    }

    public int getCriticalCount() {
        return criticalCount;
    }

    public Set<String> getAffectedTeams() {
        return affectedTeams;
    }
}
