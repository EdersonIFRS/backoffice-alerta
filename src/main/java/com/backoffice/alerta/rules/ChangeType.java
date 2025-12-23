package com.backoffice.alerta.rules;

/**
 * Tipo de mudança no Pull Request
 */
public enum ChangeType {
    FEATURE("Nova Funcionalidade", false),
    HOTFIX("Correção Urgente", true),
    REFACTOR("Refatoração", false),
    CONFIG("Configuração", false);

    private final String description;
    private final boolean urgent;

    ChangeType(String description, boolean urgent) {
        this.description = description;
        this.urgent = urgent;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Verifica se é mudança urgente
     */
    public boolean isUrgent() {
        return urgent;
    }
}
