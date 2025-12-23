package com.backoffice.alerta.chat;

/**
 * Tipo de mensagem do chat unificado de análise de impacto
 * 
 * US#46 - Chat Unificado de Análise de Impacto
 */
public enum ChatMessageType {
    
    /**
     * Mensagem informativa - contexto e explicação
     */
    INFO("Informação"),
    
    /**
     * Mensagem de alerta - riscos e cuidados
     */
    WARNING("Atenção"),
    
    /**
     * Mensagem de ação recomendada - próximos passos
     */
    ACTION("Ação Recomendada");
    
    private final String description;
    
    ChatMessageType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
