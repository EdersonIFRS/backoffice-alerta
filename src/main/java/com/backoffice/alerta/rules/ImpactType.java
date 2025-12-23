package com.backoffice.alerta.rules;

/**
 * Tipo de impacto que um arquivo tem sobre uma regra de negócio
 */
public enum ImpactType {
    DIRECT("Direto - Arquivo implementa diretamente a regra"),
    INDIRECT("Indireto - Arquivo auxilia ou influencia a regra");

    private final String description;

    ImpactType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
