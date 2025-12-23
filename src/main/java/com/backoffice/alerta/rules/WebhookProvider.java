package com.backoffice.alerta.rules;

/**
 * Provedor de webhook de CI/CD
 * 
 * Identifica a origem do webhook para adaptação de payload
 */
public enum WebhookProvider {
    GITHUB("GitHub"),
    GITLAB("GitLab");

    private final String description;

    WebhookProvider(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
