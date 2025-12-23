package com.backoffice.alerta.rules;

/**
 * Decisão final sobre aprovação de mudança
 */
public enum FinalDecision {
    APROVADO("Aprovado", "Mudança aprovada sem restrições"),
    APROVADO_COM_RESTRICOES("Aprovado com Restrições", "Mudança aprovada mediante condições específicas"),
    BLOQUEADO("Bloqueado", "Mudança bloqueada por exceder limites de risco");

    private final String displayName;
    private final String description;

    FinalDecision(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
