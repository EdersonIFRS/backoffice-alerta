package com.backoffice.alerta.rag;

/**
 * Foco da explicação RAG
 */
public enum ExplainFocus {
    BUSINESS("Foco em regras de negócio e impacto funcional"),
    TECHNICAL("Foco em implementação técnica e dependências"),
    EXECUTIVE("Foco em riscos, ownership e decisões estratégicas");
    
    private final String description;
    
    ExplainFocus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
