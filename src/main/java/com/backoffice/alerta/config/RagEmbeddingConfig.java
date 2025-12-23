package com.backoffice.alerta.config;

import com.backoffice.alerta.rag.embedding.BusinessRuleEmbeddingProvider;
import com.backoffice.alerta.rag.embedding.EmbeddingProviderFactory;
import com.backoffice.alerta.rag.embedding.RagEmbeddingProperties;
import com.backoffice.alerta.rag.persistence.BusinessRuleEmbeddingRepository;
import com.backoffice.alerta.rag.persistence.JpaBusinessRuleVectorStore;
import com.backoffice.alerta.rag.persistence.RagVectorStoreProperties;
import com.backoffice.alerta.rag.vector.BusinessRuleVectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuração de providers de embeddings e vector store para RAG
 * 
 * Cria beans para:
 * - Embedding Provider (US#65)
 * - Vector Store persistente (US#66)
 * 
 * US#65 - Substituição do DummyEmbedding por Modelo Real
 * US#66 - Persistência de Vetores (Vector DB)
 */
@Configuration
public class RagEmbeddingConfig {
    
    private static final Logger log = LoggerFactory.getLogger(RagEmbeddingConfig.class);
    
    /**
     * Bean principal de embedding provider
     * 
     * Usa EmbeddingProviderFactory para criar provider configurado
     * com fallback automático para DUMMY em caso de erro.
     * 
     * @Primary garante que este bean será usado quando houver múltiplas implementações
     */
    @Bean
    @Primary
    public BusinessRuleEmbeddingProvider embeddingProvider(EmbeddingProviderFactory factory) {
        log.info("🧠 [US#65] Criando bean BusinessRuleEmbeddingProvider via factory...");
        
        BusinessRuleEmbeddingProvider provider = factory.createProviderWithRuntimeFallback();
        
        log.info("✅ [US#65] RAG Embedding Provider inicializado: {} | dimensões: {}", 
                 provider.getClass().getSimpleName(), 
                 provider.getDimension());
        
        return provider;
    }
    
    /**
     * Bean de Vector Store com persistência JPA (US#66)
     * 
     * Criado quando rag.vector-store.type=JPA e rag.vector-store.persist=true
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "rag.vector-store.type", havingValue = "JPA", matchIfMissing = true)
    public JpaBusinessRuleVectorStore jpaVectorStore(
            BusinessRuleEmbeddingRepository repository,
            RagEmbeddingProperties embeddingProperties) {
        
        log.info("📦 [US#66] Criando JpaBusinessRuleVectorStore...");
        
        JpaBusinessRuleVectorStore vectorStore = new JpaBusinessRuleVectorStore(
            repository,
            embeddingProperties.getProvider()
        );
        
        log.info("✅ [US#66] JpaBusinessRuleVectorStore inicializado | embeddings em cache: {}", 
                 vectorStore.size());
        
        return vectorStore;
    }
}

