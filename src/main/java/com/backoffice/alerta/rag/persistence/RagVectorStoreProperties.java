package com.backoffice.alerta.rag.persistence;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuração de persistência de vector store para RAG
 * 
 * US#66 - Persistência de Vetores (Vector DB)
 */
@Component
@ConfigurationProperties(prefix = "rag.vector-store")
public class RagVectorStoreProperties {
    
    /**
     * Tipo de vector store
     * 
     * Valores suportados:
     * - MEMORY: In-memory apenas (sem persistência)
     * - JPA: Persistência em banco via JPA (H2/PostgreSQL)
     */
    private VectorStoreType type = VectorStoreType.JPA;
    
    /**
     * Habilitar persistência de embeddings
     * 
     * Se false, funciona apenas em memória (mesmo com type=JPA)
     */
    private boolean persist = true;
    
    /**
     * Enum de tipos de vector store
     */
    public enum VectorStoreType {
        MEMORY,
        JPA
    }
    
    // Getters e Setters
    
    public VectorStoreType getType() {
        return type;
    }
    
    public void setType(VectorStoreType type) {
        this.type = type;
    }
    
    public boolean isPersist() {
        return persist;
    }
    
    public void setPersist(boolean persist) {
        this.persist = persist;
    }
}
