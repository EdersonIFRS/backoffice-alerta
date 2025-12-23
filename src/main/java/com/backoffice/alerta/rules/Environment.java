package com.backoffice.alerta.rules;

/**
 * Ambiente de deploy
 */
public enum Environment {
    DEV("Desenvolvimento"),
    STAGING("Staging/Homologação"),
    PRODUCTION("Produção");

    private final String description;

    Environment(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Verifica se é ambiente crítico (produção)
     */
    public boolean isCritical() {
        return this == PRODUCTION;
    }
}
