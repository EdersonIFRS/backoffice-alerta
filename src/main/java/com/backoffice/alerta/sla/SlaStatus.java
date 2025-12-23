package com.backoffice.alerta.sla;

/**
 * Status de acompanhamento de SLA de risco
 * 
 * Define o estado atual de um SLA de resposta organizacional.
 */
public enum SlaStatus {
    
    /**
     * Aguardando resposta inicial
     */
    PENDING("Pendente"),
    
    /**
     * Reconhecido pelo time responsável
     */
    ACKNOWLEDGED("Reconhecido"),
    
    /**
     * SLA vencido sem resposta
     */
    BREACHED("Vencido"),
    
    /**
     * Escalonado para nível superior
     */
    ESCALATED("Escalonado"),
    
    /**
     * Resolvido com sucesso
     */
    RESOLVED("Resolvido");
    
    private final String description;
    
    SlaStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
