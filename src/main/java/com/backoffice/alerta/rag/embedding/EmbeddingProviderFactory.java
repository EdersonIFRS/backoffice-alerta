package com.backoffice.alerta.rag.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Factory para criação de providers de embeddings com fallback automático
 * 
 * Responsabilidades:
 * 1. Criar provider baseado em configuração
 * 2. Validar disponibilidade/viabilidade do provider
 * 3. Fazer fallback para DUMMY em caso de erro
 * 4. Logs claros e auditáveis
 * 
 * Fluxo de fallback:
 * SENTENCE_TRANSFORMER (erro) -> DUMMY
 * OPENAI (token ausente/erro) -> DUMMY
 * DUMMY -> sempre funciona (determinístico)
 * 
 * US#65 - Substituição do DummyEmbedding por Modelo Real
 */
@Component
public class EmbeddingProviderFactory {
    
    private static final Logger log = LoggerFactory.getLogger(EmbeddingProviderFactory.class);
    
    private final RagEmbeddingProperties properties;
    private final DummyBusinessRuleEmbeddingProvider dummyProvider;
    
    public EmbeddingProviderFactory(RagEmbeddingProperties properties, 
                                   DummyBusinessRuleEmbeddingProvider dummyProvider) {
        this.properties = properties;
        this.dummyProvider = dummyProvider;
    }
    
    /**
     * Cria provider de embeddings baseado na configuração
     * 
     * @return Provider configurado ou DUMMY (fallback)
     */
    public BusinessRuleEmbeddingProvider createProvider() {
        EmbeddingProviderType providerType = properties.getProvider();
        
        log.info("🧠 [US#65] Inicializando RAG Embedding Provider: {}", providerType);
        
        try {
            switch (providerType) {
                case SENTENCE_TRANSFORMER:
                    return createSentenceTransformerProvider();
                
                case OPENAI:
                    return createOpenAIProvider();
                
                case DUMMY:
                default:
                    return createDummyProvider();
            }
        } catch (Exception e) {
            return handleProviderCreationError(providerType, e);
        }
    }
    
    private BusinessRuleEmbeddingProvider createSentenceTransformerProvider() {
        log.info("🌐 [US#65] Criando Sentence Transformer Provider...");
        
        SentenceTransformerEmbeddingProvider provider = 
            new SentenceTransformerEmbeddingProvider(properties.getTimeoutSeconds());
        
        // Testa provider com texto simples
        testProvider(provider, "teste de inicialização");
        
        log.info("✅ [US#65] Sentence Transformer Provider inicializado com sucesso");
        return provider;
    }
    
    private BusinessRuleEmbeddingProvider createOpenAIProvider() {
        log.info("🌐 [US#65] Criando OpenAI Provider...");
        
        // Valida API key
        String apiKey = properties.getOpenaiApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "OPENAI_API_KEY não configurada. " +
                "Configure via ENV ou application.yml. " +
                "Fallback para DUMMY será ativado."
            );
        }
        
        OpenAIEmbeddingProvider provider = new OpenAIEmbeddingProvider(
            apiKey,
            properties.getOpenaiApiUrl(),
            properties.getTimeoutSeconds()
        );
        
        // Testa provider com texto simples
        testProvider(provider, "teste de inicialização");
        
        log.info("✅ [US#65] OpenAI Provider inicializado com sucesso");
        return provider;
    }
    
    private BusinessRuleEmbeddingProvider createDummyProvider() {
        log.info("🎲 [US#65] Usando Dummy Provider (hash-based, determinístico)");
        return dummyProvider;
    }
    
    private void testProvider(BusinessRuleEmbeddingProvider provider, String testText) {
        try {
            float[] embedding = provider.embed(testText);
            if (embedding == null || embedding.length == 0) {
                throw new RuntimeException("Provider retornou embedding vazio");
            }
            log.info("📊 [US#65] Provider testado com sucesso | dimensões={}", embedding.length);
        } catch (Exception e) {
            throw new RuntimeException("Falha no teste do provider: " + e.getMessage(), e);
        }
    }
    
    private BusinessRuleEmbeddingProvider handleProviderCreationError(
            EmbeddingProviderType attemptedType, 
            Exception error) {
        
        if (!properties.isEnableFallback()) {
            log.error("❌ [US#65] Erro ao criar provider {} e fallback desabilitado", attemptedType);
            throw new RuntimeException("Failed to create embedding provider", error);
        }
        
        log.warn("⚠️ [US#65] Erro ao criar provider {}. Ativando fallback para DUMMY.", attemptedType);
        log.warn("⚠️ [US#65] Motivo: {}", error.getMessage());
        log.info("🎲 [US#65] RAG Embedding fallback ativado (Dummy)");
        
        return dummyProvider;
    }
    
    /**
     * Wrapper que adiciona fallback em tempo de execução
     * 
     * Encapsula provider real e faz fallback para DUMMY em caso de erro
     * durante chamadas embed().
     */
    public BusinessRuleEmbeddingProvider createProviderWithRuntimeFallback() {
        BusinessRuleEmbeddingProvider primaryProvider = createProvider();
        
        // Se já é DUMMY, não precisa wrapper
        if (primaryProvider instanceof DummyBusinessRuleEmbeddingProvider) {
            return primaryProvider;
        }
        
        // Wrapper com fallback
        return new FallbackEmbeddingProvider(primaryProvider, dummyProvider);
    }
    
    /**
     * Provider wrapper que faz fallback automático em caso de erro
     */
    private static class FallbackEmbeddingProvider implements BusinessRuleEmbeddingProvider {
        
        private static final Logger log = LoggerFactory.getLogger(FallbackEmbeddingProvider.class);
        
        private final BusinessRuleEmbeddingProvider primary;
        private final BusinessRuleEmbeddingProvider fallback;
        private boolean primaryFailed = false;
        
        FallbackEmbeddingProvider(BusinessRuleEmbeddingProvider primary, 
                                 BusinessRuleEmbeddingProvider fallback) {
            this.primary = primary;
            this.fallback = fallback;
        }
        
        @Override
        public float[] embed(String text) {
            // Se primary já falhou antes, usa fallback direto
            if (primaryFailed) {
                return fallback.embed(text);
            }
            
            try {
                return primary.embed(text);
            } catch (Exception e) {
                log.warn("⚠️ [US#65] Erro ao gerar embedding real. Usando DummyEmbeddingProvider. Erro: {}", 
                         e.getMessage());
                primaryFailed = true;
                return fallback.embed(text);
            }
        }
        
        @Override
        public int getDimension() {
            return primary.getDimension();
        }
    }
}
