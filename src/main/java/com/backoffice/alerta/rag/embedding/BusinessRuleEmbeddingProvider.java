package com.backoffice.alerta.rag.embedding;

/**
 * Interface pluggável para geração de embeddings de regras de negócio
 * 
 * Permite trocar implementações (dummy, OpenAI, local models, etc.)
 * sem alterar código dependente.
 * 
 * US#44 - Busca Semântica com Embeddings
 */
public interface BusinessRuleEmbeddingProvider {
    
    /**
     * Gera embedding vetorial para um texto
     * 
     * @param text Texto para gerar embedding (nome, descrição, contexto)
     * @return Vetor de floats representando o embedding
     */
    float[] embed(String text);
    
    /**
     * Retorna dimensão dos embeddings gerados
     * 
     * @return Número de dimensões do vetor
     */
    int getDimension();
}
