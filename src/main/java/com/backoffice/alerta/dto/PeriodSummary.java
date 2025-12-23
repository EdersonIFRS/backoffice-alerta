package com.backoffice.alerta.dto;

import java.time.Instant;

/**
 * Período analisado no dashboard executivo
 * 
 * Contém timestamps e descrição do filtro aplicado
 */
public class PeriodSummary {
    
    private final Instant from;
    private final Instant to;
    private final String description;
    private final String focus;         // GLOBAL ou PRODUCTION_ONLY
    private final String environment;   // Filtro por ambiente, se aplicado

    public PeriodSummary(Instant from, 
                        Instant to, 
                        String description, 
                        String focus, 
                        String environment) {
        this.from = from;
        this.to = to;
        this.description = description;
        this.focus = focus;
        this.environment = environment;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }

    public String getDescription() {
        return description;
    }

    public String getFocus() {
        return focus;
    }

    public String getEnvironment() {
        return environment;
    }
}
