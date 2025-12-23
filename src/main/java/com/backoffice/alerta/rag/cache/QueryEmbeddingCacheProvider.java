package com.backoffice.alerta.rag.cache;

import java.util.Optional;

/**
 * US#64 - Interface de cache de embeddings de query.
 * 
 * Permite futuras implementações (Redis, Caffeine, etc.)
 * sem alterar o código do RAG.
 * 
 * Princípios:
 * - Thread-safe obrigatório
 * - Fail-safe (nunca lança exceção para o caller)
 * - Implementações devem logar internamente
 */
public interface QueryEmbeddingCacheProvider {

    /**
     * Busca embedding no cache.
     * 
     * @param normalizedQuery Query normalizada (lowercase, trim, sem acentos)
     * @return Optional com embedding se encontrado e válido (não expirado)
     */
    Optional<float[]> get(String normalizedQuery);

    /**
     * Armazena embedding no cache.
     * 
     * @param normalizedQuery Query normalizada
     * @param embedding Vetor de embedding gerado
     */
    void put(String normalizedQuery, float[] embedding);

    /**
     * Remove embeddings expirados do cache.
     * Chamado de forma lazy (on access) ou periódica.
     */
    void evictExpired();

    /**
     * Retorna estatísticas do cache (opcional).
     * 
     * @return Informações sobre hits, misses, tamanho, etc.
     */
    default CacheStats getStats() {
        return new CacheStats(0, 0, 0);
    }

    /**
     * Estatísticas do cache.
     */
    class CacheStats {
        private final long totalQueries;
        private final long cacheHits;
        private final long cacheMisses;

        public CacheStats(long totalQueries, long cacheHits, long cacheMisses) {
            this.totalQueries = totalQueries;
            this.cacheHits = cacheHits;
            this.cacheMisses = cacheMisses;
        }

        public long getTotalQueries() {
            return totalQueries;
        }

        public long getCacheHits() {
            return cacheHits;
        }

        public long getCacheMisses() {
            return cacheMisses;
        }

        public double getHitRate() {
            return totalQueries > 0 ? (double) cacheHits / totalQueries : 0.0;
        }
    }
}
