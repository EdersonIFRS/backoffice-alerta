package com.backoffice.alerta.rag;

/**
 * Tipo de match usado pelo RAG para retornar uma regra
 * 
 * US#63 - Score de Similaridade Visível no RAG
 * 
 * Permite auditoria e transparência do mecanismo RAG
 */
public enum RagMatchType {
    
    /**
     * Regra retornada apenas por busca semântica (embedding similarity)
     */
    SEMANTIC,
    
    /**
     * Regra retornada apenas por busca por palavras-chave
     */
    KEYWORD,
    
    /**
     * Regra retornada por AMBOS: semântica E keyword
     * (maior confiança)
     */
    HYBRID,
    
    /**
     * Regra incluída por fallback quando threshold não foi atingido
     * (baixa confiança, mas incluída para evitar resposta vazia)
     */
    FALLBACK
}
