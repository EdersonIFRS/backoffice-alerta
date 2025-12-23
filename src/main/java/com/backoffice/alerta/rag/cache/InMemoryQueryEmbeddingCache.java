package com.backoffice.alerta.rag.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * US#64 - Cache in-memory de embeddings de query.
 * 
 * Características:
 * - Thread-safe (ConcurrentHashMap)
 * - TTL configurável
 * - maxEntries configurável
 * - Limpeza lazy (on access)
 * - Fail-safe (nunca propaga exceções)
 * - Logs INFO para HIT/MISS/EXPIRED
 * 
 * Princípios:
 * - READ-ONLY (não persiste nada)
 * - Determinístico
 * - Zero side-effects no RAG
 */
@Component
public class InMemoryQueryEmbeddingCache implements QueryEmbeddingCacheProvider {

    private static final Logger log = LoggerFactory.getLogger(InMemoryQueryEmbeddingCache.class);

    private final ConcurrentHashMap<String, CachedEmbedding> cache = new ConcurrentHashMap<>();
    private final RagCacheProperties properties;

    // Métricas in-memory
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    public InMemoryQueryEmbeddingCache(RagCacheProperties properties) {
        this.properties = properties;
        log.info("🧠 [US#64] InMemoryQueryEmbeddingCache inicializado | enabled={} | ttl={}min | maxEntries={}", 
                properties.isEnabled(), properties.getTtlMinutes(), properties.getMaxEntries());
    }

    @Override
    public Optional<float[]> get(String normalizedQuery) {
        try {
            if (!properties.isEnabled()) {
                return Optional.empty();
            }

            totalQueries.incrementAndGet();

            CachedEmbedding cached = cache.get(normalizedQuery);
            
            if (cached == null) {
                cacheMisses.incrementAndGet();
                log.info("🧠 RAG Query Embedding Cache MISS | key=\"{}\"", normalizedQuery);
                return Optional.empty();
            }

            // Verificar expiração
            if (cached.isExpired(properties.getTtlMinutes())) {
                cache.remove(normalizedQuery);
                cacheMisses.incrementAndGet();
                log.info("🧹 RAG Query Embedding Cache EXPIRED | key=\"{}\"", normalizedQuery);
                return Optional.empty();
            }

            cacheHits.incrementAndGet();
            log.info("🧠 RAG Query Embedding Cache HIT | key=\"{}\"", normalizedQuery);
            return Optional.of(cached.getEmbedding());

        } catch (Exception e) {
            log.error("❌ [US#64] Erro ao buscar no cache, retornando empty (fail-safe)", e);
            cacheMisses.incrementAndGet();
            return Optional.empty();
        }
    }

    @Override
    public void put(String normalizedQuery, float[] embedding) {
        try {
            if (!properties.isEnabled()) {
                return;
            }

            // Verificar limite máximo de entradas
            if (cache.size() >= properties.getMaxEntries()) {
                evictExpired();
                
                // Se ainda estiver cheio após limpeza, remover entrada mais antiga
                if (cache.size() >= properties.getMaxEntries()) {
                    evictOldest();
                }
            }

            CachedEmbedding cachedEmbedding = new CachedEmbedding(embedding, Instant.now());
            cache.put(normalizedQuery, cachedEmbedding);
            
            log.debug("💾 [US#64] Embedding salvo no cache | key=\"{}\" | size={}", 
                    normalizedQuery, cache.size());

        } catch (Exception e) {
            log.error("❌ [US#64] Erro ao salvar no cache, ignorando (fail-safe)", e);
        }
    }

    @Override
    public void evictExpired() {
        try {
            if (!properties.isEnabled()) {
                return;
            }

            int evictedCount = 0;
            long ttl = properties.getTtlMinutes();

            for (String key : cache.keySet()) {
                CachedEmbedding cached = cache.get(key);
                if (cached != null && cached.isExpired(ttl)) {
                    cache.remove(key);
                    evictedCount++;
                    log.debug("🧹 [US#64] Embedding expirado removido | key=\"{}\"", key);
                }
            }

            if (evictedCount > 0) {
                log.info("🧹 [US#64] Limpeza de cache concluída | removed={} | remaining={}", 
                        evictedCount, cache.size());
            }

        } catch (Exception e) {
            log.error("❌ [US#64] Erro ao limpar cache expirado, ignorando (fail-safe)", e);
        }
    }

    @Override
    public CacheStats getStats() {
        return new CacheStats(
                totalQueries.get(),
                cacheHits.get(),
                cacheMisses.get()
        );
    }

    /**
     * Remove a entrada mais antiga do cache quando atingir maxEntries.
     * LRU simplificado baseado em timestamp de criação.
     */
    private void evictOldest() {
        try {
            String oldestKey = null;
            Instant oldestTime = Instant.now();

            for (var entry : cache.entrySet()) {
                Instant createdAt = entry.getValue().getCreatedAt();
                if (createdAt.isBefore(oldestTime)) {
                    oldestTime = createdAt;
                    oldestKey = entry.getKey();
                }
            }

            if (oldestKey != null) {
                cache.remove(oldestKey);
                log.debug("🗑️ [US#64] Entrada mais antiga removida por limite | key=\"{}\"", oldestKey);
            }

        } catch (Exception e) {
            log.error("❌ [US#64] Erro ao remover entrada mais antiga, ignorando (fail-safe)", e);
        }
    }

    /**
     * Retorna o tamanho atual do cache.
     */
    public int size() {
        return cache.size();
    }

    /**
     * Limpa todo o cache (útil para testes).
     */
    public void clear() {
        cache.clear();
        totalQueries.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        log.info("🧹 [US#64] Cache completamente limpo");
    }
}
