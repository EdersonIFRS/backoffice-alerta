-- Script de Teste Manual - Fase 1
-- Insere dados fictícios para validar indexação de código

-- 1. Inserir arquivo de código de teste
INSERT INTO code_file_embeddings (
    file_path, 
    project_id, 
    content, 
    language, 
    embedding, 
    dimension, 
    provider, 
    created_at, 
    updated_at
) VALUES (
    'src/main/java/com/example/AuthenticationService.java',
    '00000000-0000-0000-0000-000000000001'::uuid,
    'public class AuthenticationService { public boolean authenticate(User user) { return true; } }',
    'java',
    decode('0000000000000000', 'hex'), -- Embedding fake
    384,
    'openai',
    NOW(),
    NOW()
);

-- 2. Inserir mapeamento código -> regra
INSERT INTO code_business_rule_mapping (
    file_path,
    business_rule_id,
    confidence,
    relationship_type,
    detected_at,
    detection_method
) VALUES (
    'src/main/java/com/example/AuthenticationService.java',
    'AUTH_001',
    1.0,
    'DIRECT',
    NOW(),
    'SOURCE_FILE_FIELD'
);

-- 3. Verificar dados inseridos
SELECT 
    'CODE FILES' as tipo,
    COUNT(*) as total 
FROM code_file_embeddings
UNION ALL
SELECT 
    'MAPPINGS' as tipo,
    COUNT(*) as total 
FROM code_business_rule_mapping;

-- 4. Detalhar arquivo indexado
SELECT 
    file_path,
    language,
    LENGTH(content) as content_size,
    dimension,
    provider,
    created_at
FROM code_file_embeddings;

-- 5. Detalhar mapeamento
SELECT 
    file_path,
    business_rule_id,
    confidence,
    relationship_type,
    detection_method
FROM code_business_rule_mapping;
