package com.backoffice.alerta.rules;

/**
 * Domínio de negócio da regra
 */
public enum Domain {
    PAYMENT("Pagamento"),
    BILLING("Faturamento"),
    ORDER("Pedido"),
    USER("Usuário"),
    GENERIC("Genérico");

    private final String description;

    Domain(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
