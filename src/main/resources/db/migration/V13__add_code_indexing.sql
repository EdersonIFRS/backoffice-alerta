-- FASE 1: Indexação de Código-Fonte
-- Permite buscar código além de regras de negócio

-- Tabela para armazenar embeddings de arquivos de código
CREATE TABLE code_file_embeddings (
    file_path TEXT PRIMARY KEY,
    project_id UUID NOT NULL,
    content TEXT NOT NULL,
    language VARCHAR(50) NOT NULL, -- java, typescript, python, etc.
    embedding BYTEA NOT NULL,
    dimension INTEGER NOT NULL,
    provider VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Índice para consultas por projeto
CREATE INDEX idx_code_file_project ON code_file_embeddings(project_id);

-- Índice para consultas por linguagem
CREATE INDEX idx_code_file_language ON code_file_embeddings(language);

-- Tabela para mapear relacionamentos código ↔ regras de negócio
CREATE TABLE code_business_rule_mapping (
    file_path TEXT NOT NULL,
    business_rule_id VARCHAR(255) NOT NULL,
    confidence FLOAT NOT NULL DEFAULT 0.0, -- 0.0 a 1.0
    relationship_type VARCHAR(50) NOT NULL, -- IMPLEMENTS, VALIDATES, TRIGGERS, REFERENCES
    detected_at TIMESTAMP NOT NULL,
    detection_method VARCHAR(100), -- COMMENT_ANNOTATION, SEMANTIC_ANALYSIS, PR_HISTORY
    PRIMARY KEY (file_path, business_rule_id)
);

-- Índice para buscar regras por arquivo
CREATE INDEX idx_code_rule_file ON code_business_rule_mapping(file_path);

-- Índice para buscar arquivos por regra
CREATE INDEX idx_code_rule_business_rule ON code_business_rule_mapping(business_rule_id);

-- Comentários para documentação
COMMENT ON TABLE code_file_embeddings IS 'Armazena embeddings de arquivos de código para busca semântica';
COMMENT ON COLUMN code_file_embeddings.file_path IS 'Caminho relativo do arquivo no repositório (ex: src/main/java/AuthController.java)';
COMMENT ON COLUMN code_file_embeddings.content IS 'Conteúdo completo do arquivo de código';
COMMENT ON COLUMN code_file_embeddings.language IS 'Linguagem de programação detectada';

COMMENT ON TABLE code_business_rule_mapping IS 'Relaciona arquivos de código com regras de negócio afetadas';
COMMENT ON COLUMN code_business_rule_mapping.confidence IS 'Nível de confiança da associação (0.0 = baixa, 1.0 = alta)';
COMMENT ON COLUMN code_business_rule_mapping.relationship_type IS 'Tipo de relacionamento entre código e regra';
