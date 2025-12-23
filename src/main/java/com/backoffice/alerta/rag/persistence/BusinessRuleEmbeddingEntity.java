package com.backoffice.alerta.rag.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidade JPA para persistência de embeddings de regras de negócio
 * 
 * Armazena vetores de embeddings gerados por providers (DUMMY, SENTENCE_TRANSFORMER, OPENAI)
 * permitindo que o RAG seja durável e não precise regenerar embeddings a cada restart.
 * 
 * US#66 - Persistência de Vetores (Vector DB)
 */
@Entity
@Table(name = "business_rule_embeddings", indexes = {
    @Index(name = "idx_embedding_provider", columnList = "provider")
})
public class BusinessRuleEmbeddingEntity {
    
    @Id
    @Column(name = "business_rule_id", nullable = false)
    private UUID businessRuleId;
    
    /**
     * Dimensão do vetor (128 para DUMMY, 384 para SENTENCE_TRANSFORMER, 1536 para OPENAI)
     */
    @Column(name = "dimension", nullable = false)
    private int dimension;
    
    /**
     * Tipo de provider que gerou o embedding
     */
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;
    
    /**
     * Embedding serializado como byte array
     * float[] é convertido para byte[] antes de persistir
     */
    @Column(name = "embedding", nullable = false, columnDefinition = "BYTEA")
    private byte[] embedding;
    
    /**
     * Data/hora de criação do embedding
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    /**
     * Construtor protegido (JPA)
     */
    protected BusinessRuleEmbeddingEntity() {
    }
    
    /**
     * Construtor para criação de nova entidade
     * 
     * @param businessRuleId ID da regra de negócio
     * @param dimension Dimensão do vetor
     * @param provider Tipo de provider (DUMMY, SENTENCE_TRANSFORMER, OPENAI)
     * @param embedding Embedding serializado
     */
    public BusinessRuleEmbeddingEntity(UUID businessRuleId, int dimension, String provider, byte[] embedding) {
        this.businessRuleId = businessRuleId;
        this.dimension = dimension;
        this.provider = provider;
        this.embedding = embedding;
        this.createdAt = Instant.now();
    }
    
    // Getters (sem setters públicos para imutabilidade)
    
    public UUID getBusinessRuleId() {
        return businessRuleId;
    }
    
    public int getDimension() {
        return dimension;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public byte[] getEmbedding() {
        return embedding;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
}
