package com.backoffice.alerta.rag.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuração de embeddings para RAG
 * 
 * US#65 - Substituição do DummyEmbedding por Modelo Real
 */
@Component
@ConfigurationProperties(prefix = "rag.embedding")
public class RagEmbeddingProperties {
    
    /**
     * Tipo de provider de embeddings
     * 
     * Valores suportados:
     * - DUMMY: Hash-based, determinístico, sem API externa (default fallback)
     * - SENTENCE_TRANSFORMER: Modelo local multilíngue (recomendado)
     * - OPENAI: text-embedding-3-small (requer OPENAI_API_KEY em ENV)
     */
    private EmbeddingProviderType provider = EmbeddingProviderType.DUMMY;
    
    /**
     * Token da API OpenAI (lido de variável de ambiente OPENAI_API_KEY)
     * Usado apenas quando provider = OPENAI
     */
    private String openaiApiKey;
    
    /**
     * URL base da API OpenAI
     * Default: https://api.openai.com/v1
     */
    private String openaiApiUrl = "https://api.openai.com/v1";
    
    /**
     * Timeout em segundos para chamadas de embedding
     * Default: 10 segundos
     */
    private int timeoutSeconds = 10;
    
    /**
     * Habilitar fallback automático para DUMMY em caso de erro
     * Default: true (sempre usar fallback)
     */
    private boolean enableFallback = true;
    
    // Getters e Setters
    
    public EmbeddingProviderType getProvider() {
        return provider;
    }
    
    public void setProvider(EmbeddingProviderType provider) {
        this.provider = provider;
    }
    
    public String getOpenaiApiKey() {
        // Tenta ler de ENV se não configurado diretamente
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return System.getenv("OPENAI_API_KEY");
        }
        return openaiApiKey;
    }
    
    public void setOpenaiApiKey(String openaiApiKey) {
        this.openaiApiKey = openaiApiKey;
    }
    
    public String getOpenaiApiUrl() {
        return openaiApiUrl;
    }
    
    public void setOpenaiApiUrl(String openaiApiUrl) {
        this.openaiApiUrl = openaiApiUrl;
    }
    
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
    
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
    
    public boolean isEnableFallback() {
        return enableFallback;
    }
    
    public void setEnableFallback(boolean enableFallback) {
        this.enableFallback = enableFallback;
    }
}
