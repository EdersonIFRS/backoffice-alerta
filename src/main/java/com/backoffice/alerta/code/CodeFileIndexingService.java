package com.backoffice.alerta.code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * FASE 1: Serviço para indexação de código-fonte
 * Extrai arquivos de código do repositório e prepara para embedding
 */
@Service
@ConditionalOnProperty(name = "rag.code.enabled", havingValue = "true", matchIfMissing = false)
public class CodeFileIndexingService {

    private static final Logger log = LoggerFactory.getLogger(CodeFileIndexingService.class);

    // Extensões de arquivos suportadas
    private static final Pattern JAVA_FILE = Pattern.compile(".*\\.java$");
    private static final Pattern TS_FILE = Pattern.compile(".*\\.(ts|tsx)$");
    private static final Pattern JS_FILE = Pattern.compile(".*\\.(js|jsx)$");
    private static final Pattern PY_FILE = Pattern.compile(".*\\.py$");
    
    // Padrões para identificar tipo de arquivo
    private static final Pattern TEST_FILE = Pattern.compile(".*(Test|Spec|test|spec)\\.(java|ts|js|py)$");
    private static final Pattern CONFIG_FILE = Pattern.compile(".*(application|config|settings)\\.(yml|yaml|properties|json)$");

    /**
     * Identifica se arquivo deve ser indexado
     */
    public boolean shouldIndexFile(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        
        // Ignorar diretórios comuns
        if (path.contains("/node_modules/") || 
            path.contains("/target/") || 
            path.contains("/.git/") ||
            path.contains("/build/") ||
            path.contains("/dist/")) {
            return false;
        }
        
        // Aceitar apenas extensões suportadas
        return JAVA_FILE.matcher(path).matches() ||
               TS_FILE.matcher(path).matches() ||
               JS_FILE.matcher(path).matches() ||
               PY_FILE.matcher(path).matches() ||
               CONFIG_FILE.matcher(path).matches();
    }

    /**
     * Detecta linguagem do arquivo
     */
    public String detectLanguage(String path) {
        if (path.endsWith(".java")) return "java";
        if (path.endsWith(".ts") || path.endsWith(".tsx")) return "typescript";
        if (path.endsWith(".js") || path.endsWith(".jsx")) return "javascript";
        if (path.endsWith(".py")) return "python";
        if (path.endsWith(".yml") || path.endsWith(".yaml")) return "yaml";
        if (path.endsWith(".properties")) return "properties";
        if (path.endsWith(".json")) return "json";
        return "unknown";
    }

    /**
     * Detecta tipo de arquivo
     */
    public CodeFile.CodeFileType detectFileType(String path) {
        if (TEST_FILE.matcher(path).matches()) {
            return CodeFile.CodeFileType.TEST;
        }
        if (CONFIG_FILE.matcher(path).matches()) {
            return CodeFile.CodeFileType.CONFIG;
        }
        return CodeFile.CodeFileType.SOURCE;
    }

    /**
     * Cria objeto CodeFile a partir do conteúdo
     */
    public CodeFile createCodeFile(String path, String content, UUID projectId) {
        String language = detectLanguage(path);
        CodeFile.CodeFileType fileType = detectFileType(path);
        
        return new CodeFile(path, projectId, content, language, fileType);
    }

    /**
     * Detecta regras de negócio referenciadas no código via comentários
     * Procura por padrões como:
     * - // @BusinessRule: RULE_AUTH_MFA
     * - /* Business Rule: RULE_PAYMENT_PIX *\/
     */
    public List<String> detectBusinessRuleReferences(String content) {
        List<String> references = new ArrayList<>();
        
        if (content == null || content.isBlank()) {
            return references;
        }
        
        // Pattern para encontrar RULE_* em comentários
        Pattern rulePattern = Pattern.compile("(?://|/\\*|\\*).*?(@BusinessRule:|Business Rule:)\\s*(RULE_[A-Z_0-9]+)");
        var matcher = rulePattern.matcher(content);
        
        while (matcher.find()) {
            String ruleId = matcher.group(2);
            if (!references.contains(ruleId)) {
                references.add(ruleId);
                log.debug("📌 [CODE] Rule reference detected: {} in code", ruleId);
            }
        }
        
        return references;
    }

    /**
     * Log de estatísticas de indexação
     */
    public void logIndexingStats(int totalFiles, int javaFiles, int tsFiles, int testFiles) {
        log.info("📊 [CODE] Indexing stats: total={}, java={}, typescript={}, tests={}", 
                totalFiles, javaFiles, tsFiles, testFiles);
    }
}
