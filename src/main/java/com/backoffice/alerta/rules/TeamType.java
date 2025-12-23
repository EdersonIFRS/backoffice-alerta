package com.backoffice.alerta.rules;

/**
 * Tipo de time responsável por regras de negócio
 * 
 * Define os times organizacionais que podem ser owners de regras
 */
public enum TeamType {
    ENGINEERING("Engenharia"),
    PRODUCT("Produto"),
    FINANCE("Financeiro"),
    SECURITY("Segurança"),
    OPERATIONS("Operações"),
    RISK("Gestão de Risco");

    private final String description;

    TeamType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
