package com.backoffice.alerta.rag.embedding;

/**
 * Tipos de providers de embeddings disponíveis
 * 
 * US#65 - Substituição do DummyEmbedding por Modelo Real
 */
public enum EmbeddingProviderType {
    
    /**
     * Provider simulado baseado em hash (determinístico, sem API externa)
     */
    DUMMY,
    
    /**
     * Sentence Transformers local (paraphrase-multilingual-mpnet-base-v2)
     * Não requer API externa, roda localmente
     */
    SENTENCE_TRANSFORMER,
    
    /**
     * OpenAI text-embedding-3-small (requer token via ENV)
     */
    OPENAI
}
