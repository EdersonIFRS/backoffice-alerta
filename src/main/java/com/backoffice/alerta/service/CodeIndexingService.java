package com.backoffice.alerta.service;

import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import com.backoffice.alerta.rag.embedding.BusinessRuleEmbeddingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço de indexação de código-fonte.
 * Mapeia arquivos de código às regras de negócio e cria embeddings para busca semântica.
 */
@Service
@ConditionalOnProperty(name = "rag.code.enabled", havingValue = "true", matchIfMissing = false)
public class CodeIndexingService {

    private static final Logger logger = LoggerFactory.getLogger(CodeIndexingService.class);

    private final BusinessRuleRepository businessRuleRepository;
    private final JdbcTemplate jdbcTemplate;
    private final BusinessRuleEmbeddingProvider embeddingProvider;

    @Value("${rag.code.max-file-size-kb:500}")
    private int maxFileSizeKb;

    // Padrões para detectar relações entre código e regras
    private static final List<Pattern> CODE_PATTERNS = Arrays.asList(
        Pattern.compile("@RuleId\\(\"([^\"]+)\"\\)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ruleId[\\s]*=[\\s]*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("businessRule[:\\s]+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("/\\*\\*[^*]*\\*\\s+Rule:\\s+([A-Z0-9_-]+)", Pattern.CASE_INSENSITIVE)
    );

    public CodeIndexingService(
            BusinessRuleRepository businessRuleRepository,
            JdbcTemplate jdbcTemplate,
            BusinessRuleEmbeddingProvider embeddingProvider) {
        this.businessRuleRepository = businessRuleRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingProvider = embeddingProvider;
    }

    /**
     * Indexa código de uma regra de negócio.
     * Chamado durante o onboarding quando rag.code.enabled=true.
     */
    @Transactional
    public void indexBusinessRuleCode(BusinessRule rule, UUID projectId) {
        if (rule.getSourceFile() == null || rule.getSourceFile().isBlank()) {
            logger.debug("Rule {} has no sourceFile, skipping code indexing", rule.getId());
            return;
        }

        if (rule.getContent() == null || rule.getContent().isBlank()) {
            logger.debug("Rule {} has no content, skipping code indexing", rule.getId());
            return;
        }

        try {
            // Detectar linguagem do arquivo
            String language = detectLanguage(rule.getSourceFile());
            
            // Criar embedding do código
            float[] embedding = embeddingProvider.embed(rule.getContent());
            
            // Salvar embedding do arquivo
            saveFileEmbedding(
                rule.getSourceFile(),
                projectId,
                rule.getContent(),
                language,
                embedding
            );

            // Criar mapeamento código -> regra
            createCodeRuleMapping(
                rule.getSourceFile(),
                rule.getId(),
                1.0, // Alta confiança pois veio do sourceFile da regra
                "DIRECT",
                "SOURCE_FILE_FIELD"
            );

            logger.info("Successfully indexed code for rule {}: {}", rule.getId(), rule.getSourceFile());

        } catch (Exception e) {
            logger.error("Failed to index code for rule {}: {}", rule.getId(), e.getMessage(), e);
        }
    }

    /**
     * Indexa múltiplas regras de negócio.
     * @deprecated Não usado no fluxo atual. Use indexBusinessRuleCode(rule, projectId) individual
     */
    /*
    @Transactional
    public void indexBusinessRules(List<BusinessRule> rules) {
        logger.info("Indexing code for {} business rules", rules.size());
        
        int indexed = 0;
        int skipped = 0;

        for (BusinessRule rule : rules) {
            if (rule.getSourceFile() != null && !rule.getSourceFile().isBlank()) {
                indexBusinessRuleCode(rule, null); // TODO: passar projectId
                indexed++;
            } else {
                skipped++;
            }
        }

        logger.info("Code indexing complete: {} indexed, {} skipped", indexed, skipped);
    }
    */

    /**
     * Salva embedding de um arquivo de código.
     */
    private void saveFileEmbedding(String filePath, UUID projectId, String content, String language, float[] embedding) {
        String sql = """
            INSERT INTO code_file_embeddings 
                (file_path, project_id, content, language, embedding, dimension, provider, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (file_path) 
            DO UPDATE SET 
                content = EXCLUDED.content,
                embedding = EXCLUDED.embedding,
                updated_at = EXCLUDED.updated_at
        """;

        LocalDateTime now = LocalDateTime.now();
        byte[] embeddingBytes = floatArrayToBytes(embedding);

        jdbcTemplate.update(sql, 
            filePath,
            projectId,
            content,
            language,
            embeddingBytes,
            embedding.length,
            "openai",
            now,
            now
        );
    }

    /**
     * Cria mapeamento entre código e regra de negócio.
     */
    private void createCodeRuleMapping(String filePath, String ruleId, double confidence, 
                                      String relationshipType, String detectionMethod) {
        String sql = """
            INSERT INTO code_business_rule_mapping 
                (file_path, business_rule_id, confidence, relationship_type, detected_at, detection_method)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (file_path, business_rule_id) 
            DO UPDATE SET 
                confidence = EXCLUDED.confidence,
                detected_at = EXCLUDED.detected_at
        """;

        jdbcTemplate.update(sql,
            filePath,
            ruleId,
            confidence,
            relationshipType,
            LocalDateTime.now(),
            detectionMethod
        );
    }

    /**
     * Detecta linguagem de programação pelo caminho do arquivo.
     */
    private String detectLanguage(String filePath) {
        if (filePath.endsWith(".java")) return "java";
        if (filePath.endsWith(".ts") || filePath.endsWith(".tsx")) return "typescript";
        if (filePath.endsWith(".js") || filePath.endsWith(".jsx")) return "javascript";
        if (filePath.endsWith(".py")) return "python";
        if (filePath.endsWith(".go")) return "go";
        if (filePath.endsWith(".rs")) return "rust";
        if (filePath.endsWith(".kt")) return "kotlin";
        if (filePath.endsWith(".cs")) return "csharp";
        return "unknown";
    }

    /**
     * Converte array de floats para bytes (PostgreSQL bytea).
     */
    private byte[] floatArrayToBytes(float[] floats) {
        byte[] bytes = new byte[floats.length * 4];
        for (int i = 0; i < floats.length; i++) {
            int bits = Float.floatToIntBits(floats[i]);
            bytes[i * 4] = (byte) (bits >> 24);
            bytes[i * 4 + 1] = (byte) (bits >> 16);
            bytes[i * 4 + 2] = (byte) (bits >> 8);
            bytes[i * 4 + 3] = (byte) bits;
        }
        return bytes;
    }

    /**
     * Busca arquivos de código relacionados a uma query.
     */
    public List<CodeSearchResult> searchCode(String query, int limit) {
        try {
            float[] queryEmbedding = embeddingProvider.embed(query);
            byte[] queryBytes = floatArrayToBytes(queryEmbedding);

            String sql = """
                SELECT 
                    c.file_path,
                    c.content,
                    c.language,
                    m.business_rule_id,
                    m.confidence,
                    m.relationship_type
                FROM code_file_embeddings c
                LEFT JOIN code_business_rule_mapping m ON c.file_path = m.file_path
                ORDER BY c.embedding <-> ?::bytea
                LIMIT ?
            """;

            return jdbcTemplate.query(sql, 
                (rs, rowNum) -> new CodeSearchResult(
                    rs.getString("file_path"),
                    rs.getString("content"),
                    rs.getString("language"),
                    rs.getString("business_rule_id"),
                    rs.getDouble("confidence"),
                    rs.getString("relationship_type")
                ),
                queryBytes,
                limit
            );

        } catch (Exception e) {
            logger.error("Failed to search code: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Resultado de busca de código.
     */
    public record CodeSearchResult(
        String filePath,
        String content,
        String language,
        String businessRuleId,
        double confidence,
        String relationshipType
    ) {}
}
