package com.backoffice.alerta.rules;

/**
 * Nível de criticidade da regra de negócio
 */
public enum Criticality {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta"),
    CRITICA("Crítica");

    private final String description;

    Criticality(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
