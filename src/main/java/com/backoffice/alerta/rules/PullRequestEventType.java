package com.backoffice.alerta.rules;

/**
 * Tipo de evento de Pull Request
 * 
 * Permite filtrar eventos relevantes para análise de risco
 */
public enum PullRequestEventType {
    CREATED("Pull Request criado"),
    UPDATED("Pull Request atualizado"),
    MERGED("Pull Request mergeado");

    private final String description;

    PullRequestEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
