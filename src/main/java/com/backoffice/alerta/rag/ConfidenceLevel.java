package com.backoffice.alerta.rag;

/**
 * Nível de confiança da resposta RAG
 */
public enum ConfidenceLevel {
    HIGH("Alta confiança - múltiplas fontes consistentes"),
    MEDIUM("Confiança média - fontes parciais ou limitadas"),
    LOW("Baixa confiança - informação insuficiente ou conflitante");
    
    private final String description;
    
    ConfidenceLevel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
