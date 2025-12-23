package com.backoffice.alerta.rag.cache;

import java.time.Instant;

/**
 * US#64 - Modelo de embedding cacheado.
 * 
 * Armazena o embedding gerado e timestamp de criação
 * para controle de TTL.
 */
public class CachedEmbedding {

    private final float[] embedding;
    private final Instant createdAt;

    public CachedEmbedding(float[] embedding, Instant createdAt) {
        this.embedding = embedding;
        this.createdAt = createdAt;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Verifica se o embedding expirou baseado no TTL.
     * 
     * @param ttlMinutes TTL em minutos
     * @return true se expirado
     */
    public boolean isExpired(long ttlMinutes) {
        Instant expirationTime = createdAt.plusSeconds(ttlMinutes * 60);
        return Instant.now().isAfter(expirationTime);
    }
}
