package com.backoffice.alerta.rag.persistence;

import com.backoffice.alerta.rag.embedding.EmbeddingProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Vector Store com persistência JPA para embeddings de regras de negócio
 * 
 * Armazena embeddings em banco de dados (PostgreSQL/H2) e mantém cache in-memory
 * para performance. Thread-safe usando ConcurrentHashMap.
 * 
 * Responsabilidades:
 * - Persistir embeddings em banco
 * - Carregar embeddings do banco no startup
 * - Busca por similaridade (cosine) em memória
 * - Fallback seguro em caso de erro
 * 
 * US#66 - Persistência de Vetores (Vector DB)
 */
public class JpaBusinessRuleVectorStore {
    
    private static final Logger log = LoggerFactory.getLogger(JpaBusinessRuleVectorStore.class);
    
    private final BusinessRuleEmbeddingRepository repository;
    private final String currentProvider;
    
    // Cache in-memory para performance
    private final Map<UUID, float[]> embeddingCache = new ConcurrentHashMap<>();
    
    public JpaBusinessRuleVectorStore(BusinessRuleEmbeddingRepository repository, 
                                     EmbeddingProviderType providerType) {
        this.repository = repository;
        this.currentProvider = providerType.name();
        
        log.info("📦 [US#66] JpaBusinessRuleVectorStore inicializado | provider={}", currentProvider);
        
        // Carrega embeddings existentes no startup
        loadEmbeddingsFromDatabase();
    }
    
    /**
     * Carrega todos os embeddings do banco para o cache in-memory
     */
    private void loadEmbeddingsFromDatabase() {
        try {
            List<BusinessRuleEmbeddingEntity> entities = repository.findAll();
            
            if (entities.isEmpty()) {
                log.info("📦 [US#66] Nenhum embedding persistido encontrado. Database vazio.");
                return;
            }
            
            int loaded = 0;
            int skipped = 0;
            
            for (BusinessRuleEmbeddingEntity entity : entities) {
                try {
                    float[] embedding = deserializeEmbedding(entity.getEmbedding(), entity.getDimension());
                    embeddingCache.put(entity.getBusinessRuleId(), embedding);
                    loaded++;
                    
                    log.debug("📦 [US#66] VectorStore | Loaded embedding | rule={} | provider={} | dim={}", 
                             entity.getBusinessRuleId(), entity.getProvider(), entity.getDimension());
                } catch (Exception e) {
                    log.error("❌ [US#66] Erro ao deserializar embedding rule={}: {}", 
                             entity.getBusinessRuleId(), e.getMessage());
                    skipped++;
                }
            }
            
            log.info("📦 [US#66] Embeddings carregados do database | loaded={} | skipped={} | total={}", 
                     loaded, skipped, entities.size());
            
        } catch (Exception e) {
            log.error("❌ [US#66] Erro ao carregar embeddings do database. Continuando com cache vazio.", e);
        }
    }
    
    /**
     * Salva embedding no banco e no cache
     * 
     * @param ruleId ID da regra de negócio
     * @param embedding Vetor de embedding
     */
    @Transactional
    public void save(UUID ruleId, float[] embedding) {
        if (ruleId == null || embedding == null || embedding.length == 0) {
            log.warn("⚠️ [US#66] Tentativa de salvar embedding inválido para ruleId={}", ruleId);
            return;
        }
        
        try {
            // Serializa embedding
            byte[] serialized = serializeEmbedding(embedding);
            
            // Cria entidade
            BusinessRuleEmbeddingEntity entity = new BusinessRuleEmbeddingEntity(
                ruleId,
                embedding.length,
                currentProvider,
                serialized
            );
            
            // Persiste no banco
            repository.save(entity);
            
            // Atualiza cache
            embeddingCache.put(ruleId, embedding);
            
            log.info("📦 [US#66] VectorStore | Saved embedding | rule={} | provider={} | dim={}", 
                     ruleId, currentProvider, embedding.length);
            
        } catch (Exception e) {
            log.error("❌ [US#66] Erro ao persistir embedding rule={}. Salvando apenas em cache.", ruleId, e);
            
            // Fallback: salva apenas em cache
            embeddingCache.put(ruleId, embedding);
        }
    }
    
    /**
     * Recupera embedding de uma regra específica
     * 
     * @param ruleId ID da regra de negócio
     * @return Embedding ou null se não encontrado
     */
    public float[] getEmbedding(UUID ruleId) {
        // Tenta cache primeiro
        float[] cached = embeddingCache.get(ruleId);
        if (cached != null) {
            return cached;
        }
        
        // Se não está em cache, tenta carregar do banco
        try {
            Optional<BusinessRuleEmbeddingEntity> entity = repository.findById(ruleId);
            
            if (entity.isPresent()) {
                float[] embedding = deserializeEmbedding(
                    entity.get().getEmbedding(), 
                    entity.get().getDimension()
                );
                
                // Atualiza cache
                embeddingCache.put(ruleId, embedding);
                
                log.info("📦 [US#66] VectorStore | Loaded embedding from DB | rule={}", ruleId);
                return embedding;
            }
        } catch (Exception e) {
            log.error("❌ [US#66] Erro ao carregar embedding do database rule={}", ruleId, e);
        }
        
        return null;
    }
    
    /**
     * Busca Top-K regras mais similares
     * 
     * @param queryEmbedding Embedding da pergunta
     * @param k Número máximo de resultados
     * @return Lista de IDs ordenados por similaridade (maior primeiro)
     */
    public List<UUID> findTopK(float[] queryEmbedding, int k) {
        if (queryEmbedding == null || queryEmbedding.length == 0) {
            log.warn("⚠️ [US#66] Query embedding inválido");
            return Collections.emptyList();
        }
        
        if (embeddingCache.isEmpty()) {
            log.warn("⚠️ [US#66] Vector store VAZIO - nenhum embedding em cache!");
            return Collections.emptyList();
        }
        
        log.info("🔍 [US#66] Vector store tem {} embeddings em cache", embeddingCache.size());
        
        // Threshold mínimo de similaridade
        final double SIMILARITY_THRESHOLD = 0.1;
        
        try {
            // Calcula similaridade com todas as regras
            List<SimilarityResult> results = embeddingCache.entrySet().stream()
                .map(entry -> {
                    double similarity = cosineSimilarity(queryEmbedding, entry.getValue());
                    log.info("📊 [US#66] Similarity computed | rule={} | score={}", entry.getKey(), similarity);
                    return new SimilarityResult(entry.getKey(), similarity);
                })
                .filter(result -> result.similarity() >= SIMILARITY_THRESHOLD)
                .sorted(Comparator.comparingDouble(SimilarityResult::similarity).reversed())
                .limit(k)
                .collect(Collectors.toList());
            
            log.info("📊 [US#66] VectorStore | Similarity computed | topK={} | threshold={} | total={}", 
                     results.size(), SIMILARITY_THRESHOLD, k);
            
            return results.stream()
                .map(SimilarityResult::ruleId)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ [US#66] Erro ao calcular similaridade", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Calcula cosine similarity entre dois vetores
     * 
     * @param a Vetor A
     * @param b Vetor B
     * @return Similaridade [-1, 1] (1 = idênticos)
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null) {
            log.error("❌ [US#66] Vetores nulos fornecidos para similaridade");
            return 0.0;
        }
        
        if (a.length != b.length) {
            log.error("❌ [US#66] Dimensões incompatíveis: {} vs {}", a.length, b.length);
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        
        // Proteção contra divisão por zero
        double sqrtNormA = Math.sqrt(normA);
        double sqrtNormB = Math.sqrt(normB);
        
        if (sqrtNormA < 1e-10 || sqrtNormB < 1e-10) {
            log.warn("⚠️ [US#66] Norma próxima de zero: A={}, B={}", sqrtNormA, sqrtNormB);
            return 0.0;
        }
        
        double denominator = sqrtNormA * sqrtNormB;
        return dotProduct / denominator;
    }
    
    /**
     * Serializa float[] para byte[]
     */
    private byte[] serializeEmbedding(float[] embedding) {
        ByteBuffer buffer = ByteBuffer.allocate(embedding.length * Float.BYTES);
        for (float value : embedding) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }
    
    /**
     * Deserializa byte[] para float[]
     */
    private float[] deserializeEmbedding(byte[] bytes, int dimension) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        float[] embedding = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            embedding[i] = buffer.getFloat();
        }
        return embedding;
    }
    
    /**
     * Retorna número de embeddings em cache
     */
    public int size() {
        return embeddingCache.size();
    }
    
    /**
     * Retorna número de embeddings persistidos no banco
     */
    public long persistedCount() {
        try {
            return repository.count();
        } catch (Exception e) {
            log.error("❌ [US#66] Erro ao contar embeddings persistidos", e);
            return 0;
        }
    }
    
    /**
     * Verifica se existe embedding para uma regra
     */
    public boolean hasEmbedding(UUID ruleId) {
        // Verifica cache primeiro
        if (embeddingCache.containsKey(ruleId)) {
            return true;
        }
        
        // Verifica banco
        try {
            return repository.existsByBusinessRuleId(ruleId);
        } catch (Exception e) {
            log.error("❌ [US#66] Erro ao verificar existência de embedding rule={}", ruleId, e);
            return false;
        }
    }
    
    /**
     * Limpa cache (não remove do banco)
     */
    public void clearCache() {
        embeddingCache.clear();
        log.info("🗑️ [US#66] Cache de embeddings limpo (banco não afetado)");
    }
    
    /**
     * Record interno para resultados de similaridade
     */
    private record SimilarityResult(UUID ruleId, double similarity) {}
}
