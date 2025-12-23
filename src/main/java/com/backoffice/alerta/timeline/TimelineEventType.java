package com.backoffice.alerta.timeline;

/**
 * US#40 - Tipos de eventos na linha do tempo de decisão
 * 
 * Representa diferentes tipos de eventos que podem ocorrer
 * durante o ciclo de vida de uma mudança (Pull Request).
 */
public enum TimelineEventType {
    /**
     * Proposta inicial da mudança
     */
    PROPOSAL,
    
    /**
     * Análise de impacto sistêmico realizada
     */
    IMPACT_ANALYSIS,
    
    /**
     * Explicação executiva gerada
     */
    EXECUTIVE_EXPLANATION,
    
    /**
     * Simulação de risco executada
     */
    SIMULATION,
    
    /**
     * Decisão formal tomada (aprovação/rejeição)
     */
    DECISION,
    
    /**
     * Registro de auditoria criado
     */
    AUDIT,
    
    /**
     * Notificação enviada
     */
    NOTIFICATION,
    
    /**
     * SLA criado para acompanhamento
     */
    SLA_CREATED,
    
    /**
     * SLA escalonado por vencimento
     */
    SLA_ESCALATED,
    
    /**
     * Feedback pós-deploy recebido
     */
    FEEDBACK
}
