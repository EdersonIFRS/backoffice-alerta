package com.backoffice.alerta.rules;

/**
 * Papel de ownership de uma regra de negócio
 * 
 * Define o nível de responsabilidade sobre a regra
 */
public enum OwnershipRole {
    PRIMARY_OWNER("Owner Principal - Responsável direto"),
    SECONDARY_OWNER("Owner Secundário - Notificação e suporte"),
    BACKUP("Backup - Responsável em caso de ausência");

    private final String description;

    OwnershipRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
