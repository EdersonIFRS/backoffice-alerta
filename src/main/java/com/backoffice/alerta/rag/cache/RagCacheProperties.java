package com.backoffice.alerta.rag.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * US#64 - Configuração do cache de embeddings de query.
 * 
 * Lê configurações de application.yml:
 * 
 * rag:
 *   query-embedding-cache:
 *     enabled: true
 *     ttl-minutes: 30
 *     max-entries: 1000
 */
@Component
@ConfigurationProperties(prefix = "rag.query-embedding-cache")
public class RagCacheProperties {

    /**
     * Habilita/desabilita o cache.
     * Default: true
     */
    private boolean enabled = true;

    /**
     * TTL em minutos.
     * Default: 30 minutos
     */
    private long ttlMinutes = 30;

    /**
     * Número máximo de entradas no cache.
     * Default: 1000
     */
    private int maxEntries = 1000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTtlMinutes() {
        return ttlMinutes;
    }

    public void setTtlMinutes(long ttlMinutes) {
        this.ttlMinutes = ttlMinutes;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }
}
