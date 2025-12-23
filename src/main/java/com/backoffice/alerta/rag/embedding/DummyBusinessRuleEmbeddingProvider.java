package com.backoffice.alerta.rag.embedding;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementação simulada de embeddings
 * 
 * NÃO faz chamadas externas - gera embeddings determinísticos usando hash.
 * Mesmo texto sempre gera mesmo vetor.
 * 
 * US#44 - Busca Semântica com Embeddings
 * US#65 - Usado como fallback quando provider real falha
 */
@Component
public class DummyBusinessRuleEmbeddingProvider implements BusinessRuleEmbeddingProvider {
    
    private static final int DIMENSION = 128;
    
    @Override
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[DIMENSION];
        }
        
        try {
            // Normaliza texto
            String normalized = text.toLowerCase().trim();
            
            // Gera hash determinístico
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            
            // Converte hash para embedding
            float[] embedding = new float[DIMENSION];
            for (int i = 0; i < DIMENSION; i++) {
                // Usa múltiplos bytes do hash para gerar cada dimensão
                int byteIndex = (i * hash.length / DIMENSION) % hash.length;
                embedding[i] = (hash[byteIndex] & 0xFF) / 255.0f;
            }
            
            // Normaliza vetor (importante para cosine similarity)
            normalize(embedding);
            
            return embedding;
            
        } catch (NoSuchAlgorithmException e) {
            // Fallback: vetor aleatório baseado em hashCode
            return generateFallbackEmbedding(text);
        }
    }
    
    @Override
    public int getDimension() {
        return DIMENSION;
    }
    
    private float[] generateFallbackEmbedding(String text) {
        float[] embedding = new float[DIMENSION];
        int hash = text.hashCode();
        
        for (int i = 0; i < DIMENSION; i++) {
            // Gera valores pseudo-aleatórios determinísticos
            hash = hash * 31 + i;
            embedding[i] = (Math.abs(hash) % 1000) / 1000.0f;
        }
        
        normalize(embedding);
        return embedding;
    }
    
    private void normalize(float[] vector) {
        float sum = 0;
        for (float v : vector) {
            sum += v * v;
        }
        
        float magnitude = (float) Math.sqrt(sum);
        if (magnitude > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= magnitude;
            }
        }
    }
}
