package com.backoffice.alerta.rag.vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Vector Store in-memory para embeddings de regras de negócio
 * 
 * Armazena embeddings e realiza busca por similaridade (cosine).
 * Thread-safe usando ConcurrentHashMap.
 * 
 * US#44 - Busca Semântica com Embeddings
 */
@Component
public class BusinessRuleVectorStore {
    
    private static final Logger log = LoggerFactory.getLogger(BusinessRuleVectorStore.class);
    
    private final Map<UUID, float[]> embeddings = new ConcurrentHashMap<>();
    
    /**
     * Salva embedding de uma regra
     * 
     * @param ruleId ID da regra de negócio
     * @param embedding Vetor de embedding
     */
    public void save(UUID ruleId, float[] embedding) {
        if (ruleId == null || embedding == null || embedding.length == 0) {
            log.warn("⚠️ Tentativa de salvar embedding inválido para ruleId={}", ruleId);
            return;
        }
        
        embeddings.put(ruleId, embedding);
        log.debug("✅ Embedding salvo para regra {}", ruleId);
    }
    
    /**
     * US#63: Recupera embedding de uma regra específica
     * 
     * @param ruleId ID da regra de negócio
     * @return Embedding ou null se não encontrado
     */
    public float[] getEmbedding(UUID ruleId) {
        return embeddings.get(ruleId);
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
            log.warn("⚠️ Query embedding inválido");
            return Collections.emptyList();
        }
        
        if (embeddings.isEmpty()) {
            log.warn("⚠️ Vector store VAZIO - nenhum embedding indexado!");
            log.warn("⚠️ Verifique se @PostConstruct indexAllRules() foi executado");
            return Collections.emptyList();
        }
        
        log.info("🔍 Vector store tem {} embeddings indexados", embeddings.size());
        
        // Threshold mínimo de similaridade
        final double SIMILARITY_THRESHOLD = 0.1;
        
        // Calcula similaridade com todas as regras
        List<SimilarityResult> results = embeddings.entrySet().stream()
            .map(entry -> {
                double similarity = cosineSimilarity(queryEmbedding, entry.getValue());
                log.info("📊 Similarity score for {}: {}", entry.getKey(), similarity);
                return new SimilarityResult(entry.getKey(), similarity);
            })
            .filter(result -> result.similarity() >= SIMILARITY_THRESHOLD)
            .sorted(Comparator.comparingDouble(SimilarityResult::similarity).reversed())
            .limit(k)
            .collect(Collectors.toList());
        
        log.info("🔍 Busca semântica retornou {} resultado(s) acima de threshold {} (k={})", 
                 results.size(), SIMILARITY_THRESHOLD, k);
        
        return results.stream()
            .map(SimilarityResult::ruleId)
            .collect(Collectors.toList());
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
            log.error("❌ Vetores nulos fornecidos para similaridade");
            return 0.0;
        }
        
        if (a.length != b.length) {
            log.error("❌ Dimensões incompatíveis: {} vs {}", a.length, b.length);
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
            log.warn("⚠️ Norma próxima de zero: A={}, B={}", sqrtNormA, sqrtNormB);
            return 0.0;
        }
        
        double denominator = sqrtNormA * sqrtNormB;
        return dotProduct / denominator;
    }
    
    /**
     * Retorna número de embeddings indexados
     */
    public int size() {
        return embeddings.size();
    }
    
    /**
     * Limpa todos os embeddings
     */
    public void clear() {
        embeddings.clear();
        log.info("🗑️ Vector store limpo");
    }
    
    /**
     * Record interno para resultados de similaridade
     */
    private record SimilarityResult(UUID ruleId, double similarity) {}
}
