package com.backoffice.alerta.importer.extractors;

import com.backoffice.alerta.dto.ExtractedBusinessRule;
import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * US#68 - Extrator de regras de negócio a partir de comentários estruturados em código
 * 
 * Formato esperado:
 * // @BusinessRule
 * // id: REGRA_VALIDACAO_PIX
 * // name: Validação de Chave PIX
 * // description: Valida formato da chave PIX conforme BC
 * // domain: PAGAMENTOS
 * // criticality: HIGH
 * // owner: time-pagamentos
 */
@Component
public class CodeCommentRuleExtractor {

    private static final Logger log = LoggerFactory.getLogger(CodeCommentRuleExtractor.class);

    private static final Pattern BUSINESS_RULE_MARKER = Pattern.compile("@BusinessRule", Pattern.CASE_INSENSITIVE);
    private static final Pattern ID_PATTERN = Pattern.compile("id:\\s*([A-Z_0-9]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_PATTERN = Pattern.compile("name:\\s*(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("description:\\s*(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("domain:\\s*([A-Z_]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CRITICALITY_PATTERN = Pattern.compile("criticality:\\s*([A-Z]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern OWNER_PATTERN = Pattern.compile("owner:\\s*(.+)", Pattern.CASE_INSENSITIVE);

    /**
     * Extrai regra de negócio de comentários estruturados
     * 
     * @param fileContent Conteúdo do arquivo
     * @param filePath Caminho do arquivo (para logs)
     * @return Optional com regra extraída, ou vazio se não encontrar
     */
    public Optional<ExtractedBusinessRule> extract(String fileContent, String filePath) {
        try {
            // Verifica se há marcador @BusinessRule
            if (!BUSINESS_RULE_MARKER.matcher(fileContent).find()) {
                log.debug("🔍 [US#68] Nenhum marcador @BusinessRule encontrado | file={}", filePath);
                return Optional.empty();
            }

            // Extrai campos obrigatórios
            String ruleId = extractField(ID_PATTERN, fileContent);
            if (ruleId == null || ruleId.isBlank()) {
                log.warn("⚠️ [US#68] @BusinessRule sem 'id' | file={}", filePath);
                return Optional.empty();
            }

            String name = extractField(NAME_PATTERN, fileContent);
            String description = extractField(DESCRIPTION_PATTERN, fileContent);

            // Campos opcionais
            String domainStr = extractField(DOMAIN_PATTERN, fileContent);
            Domain domain = parseDomain(domainStr);

            String criticalityStr = extractField(CRITICALITY_PATTERN, fileContent);
            Criticality criticality = parseCriticality(criticalityStr);

            String owner = extractField(OWNER_PATTERN, fileContent);

            ExtractedBusinessRule rule = new ExtractedBusinessRule(
                ruleId.trim(),
                name != null ? name.trim() : ruleId,
                description != null ? description.trim() : "",
                fileContent, // Conteúdo completo do arquivo
                domain,
                criticality,
                filePath,
                owner != null ? owner.trim() : "unknown"
            );

            log.info("✅ [US#68] CodeComment | Regra extraída | id={} | file={}", ruleId, filePath);
            return Optional.of(rule);

        } catch (Exception e) {
            log.error("❌ [US#68] Erro ao extrair regra de comentário | file={} | error={}", filePath, e.getMessage());
            return Optional.empty();
        }
    }

    private String extractField(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private Domain parseDomain(String domainStr) {
        if (domainStr == null || domainStr.isBlank()) {
            return Domain.GENERIC;
        }
        try {
            return Domain.valueOf(domainStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ [US#68] Domain inválido '{}', usando GENERIC", domainStr);
            return Domain.GENERIC;
        }
    }

    private Criticality parseCriticality(String criticalityStr) {
        if (criticalityStr == null || criticalityStr.isBlank()) {
            return Criticality.MEDIA;
        }
        try {
            return Criticality.valueOf(criticalityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ [US#68] Criticality inválida '{}', usando MEDIA", criticalityStr);
            return Criticality.MEDIA;
        }
    }
}
