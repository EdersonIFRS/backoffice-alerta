package com.backoffice.alerta.rag.index;

import com.backoffice.alerta.rag.embedding.BusinessRuleEmbeddingProvider;
import com.backoffice.alerta.rag.persistence.JpaBusinessRuleVectorStore;
import com.backoffice.alerta.rag.vector.BusinessRuleVectorStore;
import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Serviço de indexação de embeddings de regras de negócio
 * 
 * Executa automaticamente no startup (@PostConstruct) e indexa
 * todas as regras existentes no vector store.
 * 
 * US#44 - Busca Semântica com Embeddings
 * US#66 - Persistência de Vetores (evita regeneração desnecessária)
 */
@Service
public class BusinessRuleEmbeddingIndexService {
    
    private static final Logger log = LoggerFactory.getLogger(BusinessRuleEmbeddingIndexService.class);
    
    private final BusinessRuleRepository ruleRepository;
    private final BusinessRuleEmbeddingProvider embeddingProvider;
    private final BusinessRuleVectorStore vectorStore;
    
    // US#66: VectorStore persistente (opcional, pode ser null se usando in-memory)
    @Autowired(required = false)
    private JpaBusinessRuleVectorStore jpaVectorStore;
    
    public BusinessRuleEmbeddingIndexService(
            BusinessRuleRepository ruleRepository,
            BusinessRuleEmbeddingProvider embeddingProvider,
            BusinessRuleVectorStore vectorStore) {
        this.ruleRepository = ruleRepository;
        this.embeddingProvider = embeddingProvider;
        this.vectorStore = vectorStore;
    }
    
    /**
     * Indexa todas as regras no startup
     * 
     * Usa ApplicationReadyEvent para garantir que execute DEPOIS
     * do DemoDataInitializer carregar os dados
     * 
     * US#66: Verifica embeddings persistidos antes de regenerar
     */
    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void indexAllRules() {
        log.info("🚀 Iniciando indexação de embeddings...");
        
        try {
            List<BusinessRule> allRules = ruleRepository.findAll();
            
            if (allRules.isEmpty()) {
                log.warn("⚠️ Nenhuma regra encontrada para indexar");
                return;
            }
            
            // US#66: Verifica embeddings persistidos
            long persistedCount = jpaVectorStore != null ? jpaVectorStore.persistedCount() : 0;
            
            if (persistedCount > 0) {
                log.info("🧠 [US#66] Found {} persisted embeddings. Skipping regeneration.", persistedCount);
                log.info("📊 Vector store: {} embeddings em cache (dimensão: {})", 
                         jpaVectorStore.size(), embeddingProvider.getDimension());
                return;
            }
            
            log.info("🧠 [US#66] No embeddings found. Generating and persisting...");
            
            int indexed = 0;
            int failed = 0;
            
            for (BusinessRule rule : allRules) {
                try {
                    UUID ruleId = UUID.fromString(rule.getId());
                    
                    // US#66: Verifica se embedding já existe para esta regra específica
                    if (jpaVectorStore != null && jpaVectorStore.hasEmbedding(ruleId)) {
                        log.debug("⏭️ [US#66] Embedding já existe para rule={}, pulando", ruleId);
                        indexed++;
                        continue;
                    }
                    
                    String textToEmbed = buildTextForEmbedding(rule);
                    
                    if (textToEmbed == null || textToEmbed.trim().isEmpty()) {
                        log.warn("⚠️ Regra {} tem texto vazio, pulando indexação", rule.getId());
                        failed++;
                        continue;
                    }
                    
                    float[] embedding = embeddingProvider.embed(textToEmbed);
                    
                    if (embedding == null || embedding.length == 0) {
                        log.warn("⚠️ Embedding nulo/vazio para regra {}", rule.getId());
                        failed++;
                        continue;
                    }
                    
                    // Salva no vector store (que pode persistir ou não)
                    if (jpaVectorStore != null) {
                        jpaVectorStore.save(ruleId, embedding);
                    } else {
                        vectorStore.save(ruleId, embedding);
                    }
                    
                    log.info("✅ Indexed rule [{}] '{}' with embedding size {}", 
                             rule.getId(), rule.getName(), embedding.length);
                    
                    indexed++;
                    
                } catch (Exception e) {
                    log.error("❌ Erro ao indexar regra {}: {}", rule.getId(), e.getMessage());
                    failed++;
                }
            }
            
            log.info("✅ Indexação concluída: {} regras indexadas, {} falharam", indexed, failed);
            
            int finalSize = jpaVectorStore != null ? jpaVectorStore.size() : vectorStore.size();
            log.info("📊 Vector store: {} embeddings (dimensão: {})", 
                     finalSize, embeddingProvider.getDimension());
            
        } catch (Exception e) {
            log.error("❌ Erro crítico na indexação de embeddings", e);
        }
    }
    
    /**
     * Constrói texto rico para gerar embedding
     * 
     * Combina múltiplos campos para melhor qualidade semântica
     */
    private String buildTextForEmbedding(BusinessRule rule) {
        StringBuilder text = new StringBuilder();
        
        // Nome da regra (peso alto)
        if (rule.getName() != null && !rule.getName().trim().isEmpty()) {
            text.append(rule.getName().trim()).append(". ");
        }
        
        // Descrição (peso alto)
        if (rule.getDescription() != null && !rule.getDescription().trim().isEmpty()) {
            text.append(rule.getDescription().trim()).append(". ");
        }
        
        // Domínio (contexto)
        if (rule.getDomain() != null) {
            text.append("Domínio: ").append(rule.getDomain().name()).append(". ");
        }
        
        // Criticidade (contexto)
        if (rule.getCriticality() != null) {
            text.append("Criticidade: ").append(rule.getCriticality().name()).append(". ");
        }
        
        String result = text.toString().trim();
        
        // Garantir que nunca retorna vazio
        if (result.isEmpty()) {
            log.warn("⚠️ Regra {} gerou texto vazio para embedding", rule.getId());
            return "REGRA_SEM_DESCRICAO_" + rule.getId();
        }
        
        return result;
    }
    
    /**
     * Indexa uma única regra (útil para novos cadastros)
     * 
     * @param rule Regra a ser indexada
     */
    public void indexRule(BusinessRule rule) {
        try {
            String textToEmbed = buildTextForEmbedding(rule);
            float[] embedding = embeddingProvider.embed(textToEmbed);
            
            UUID ruleId = UUID.fromString(rule.getId());
            vectorStore.save(ruleId, embedding);
            
            log.debug("✅ Regra {} re-indexada", rule.getId());
            
        } catch (Exception e) {
            log.error("❌ Erro ao indexar regra {}: {}", rule.getId(), e.getMessage());
        }
    }
    
    /**
     * Reindexar todas as regras (útil para atualizações em lote)
     */
    public void reindexAll() {
        log.info("🔄 Iniciando reindexação completa...");
        vectorStore.clear();
        indexAllRules();
    }
}
