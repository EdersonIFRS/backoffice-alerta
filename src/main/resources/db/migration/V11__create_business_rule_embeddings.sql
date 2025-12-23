-- US#66: Persistência de Vetores (Vector DB) para RAG
-- Cria tabela para armazenar embeddings de regras de negócio
-- NOTA: business_rule_id referencia regras em memória (não há FK)

CREATE TABLE business_rule_embeddings (
    business_rule_id UUID PRIMARY KEY,
    dimension INTEGER NOT NULL,
    provider VARCHAR(50) NOT NULL,
    embedding BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Índice para consultas por provider
CREATE INDEX idx_embedding_provider ON business_rule_embeddings(provider);

-- Índice para ordenação por data de criação
CREATE INDEX idx_embedding_created_at ON business_rule_embeddings(created_at);

-- Comentários para documentação
COMMENT ON TABLE business_rule_embeddings IS 'Armazena embeddings vetoriais de regras de negócio para busca semântica (RAG). Regras em memória.';
COMMENT ON COLUMN business_rule_embeddings.business_rule_id IS 'ID da regra de negócio (UUID, sem FK pois regras são in-memory)';
COMMENT ON COLUMN business_rule_embeddings.dimension IS 'Dimensão do vetor (128=DUMMY, 384=SENTENCE_TRANSFORMER, 1536=OPENAI)';
COMMENT ON COLUMN business_rule_embeddings.provider IS 'Tipo de provider que gerou o embedding (DUMMY, SENTENCE_TRANSFORMER, OPENAI)';
COMMENT ON COLUMN business_rule_embeddings.embedding IS 'Vetor de embedding serializado como byte array (float[] -> bytes)';
COMMENT ON COLUMN business_rule_embeddings.created_at IS 'Data/hora de criação do embedding';
