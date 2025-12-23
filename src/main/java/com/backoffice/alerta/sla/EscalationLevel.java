package com.backoffice.alerta.sla;

/**
 * Nível de escalonamento de SLA
 * 
 * Define a hierarquia de escalonamento quando um SLA vence.
 * Segue a ordem: PRIMARY → SECONDARY → BACKUP → ORGANIZATIONAL
 */
public enum EscalationLevel {
    
    /**
     * Responsável primário (PRIMARY_OWNER)
     */
    PRIMARY("Responsável Primário"),
    
    /**
     * Responsável secundário (SECONDARY_OWNER)
     */
    SECONDARY("Responsável Secundário"),
    
    /**
     * Responsável de backup (BACKUP)
     */
    BACKUP("Backup"),
    
    /**
     * Escalonamento organizacional (alta liderança)
     */
    ORGANIZATIONAL("Organizacional");
    
    private final String description;
    
    EscalationLevel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Retorna o próximo nível de escalonamento
     */
    public EscalationLevel next() {
        return switch (this) {
            case PRIMARY -> SECONDARY;
            case SECONDARY -> BACKUP;
            case BACKUP -> ORGANIZATIONAL;
            case ORGANIZATIONAL -> ORGANIZATIONAL; // Já é o máximo
        };
    }
}
