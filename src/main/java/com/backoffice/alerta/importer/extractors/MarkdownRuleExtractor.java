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
 * US#68 - Extrator de regras de negócio a partir de arquivos Markdown
 * 
 * Formato esperado:
 * ## Business Rule: REGRA_VALIDACAO_PIX
 * - **Name**: Validação de Chave PIX
 * - **Description**: Valida formato da chave PIX
 * - **Domain**: PAGAMENTOS
 * - **Criticality**: HIGH
 * - **Owner**: time-pagamentos
 */
@Component
public class MarkdownRuleExtractor {

    private static final Logger log = LoggerFactory.getLogger(MarkdownRuleExtractor.class);

    private static final Pattern BUSINESS_RULE_HEADER = Pattern.compile(
        "#{1,2}\\s+Business Rule:\\s*([A-Z_0-9]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
        "-?\\s*\\*\\*(?:Name|Rule ID)\\*\\*:\\s*(.+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile(
        "-\\s*\\*\\*Description\\*\\*:\\s*(.+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
        "-\\s*\\*\\*Domain\\*\\*:\\s*([A-Z_]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CRITICALITY_PATTERN = Pattern.compile(
        "-\\s*\\*\\*Criticality\\*\\*:\\s*([A-Z]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OWNER_PATTERN = Pattern.compile(
        "-\\s*\\*\\*Owner\\*\\*:\\s*(.+)",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Extrai regra de negócio de arquivo Markdown
     * 
     * @param fileContent Conteúdo do arquivo Markdown
     * @param filePath Caminho do arquivo (para logs)
     * @return Optional com regra extraída, ou vazio se não encontrar
     */
    public Optional<ExtractedBusinessRule> extract(String fileContent, String filePath) {
        try {
            // Busca por cabeçalho Business Rule
            Matcher headerMatcher = BUSINESS_RULE_HEADER.matcher(fileContent);
            if (!headerMatcher.find()) {
                log.debug("🔍 [US#68] Nenhum 'Business Rule:' encontrado em Markdown | file={}", filePath);
                return Optional.empty();
            }

            String ruleId = headerMatcher.group(1).trim();

            // Extrai campos
            String name = extractField(NAME_PATTERN, fileContent);
            String description = extractField(DESCRIPTION_PATTERN, fileContent);

            String domainStr = extractField(DOMAIN_PATTERN, fileContent);
            Domain domain = parseDomain(domainStr);

            String criticalityStr = extractField(CRITICALITY_PATTERN, fileContent);
            Criticality criticality = parseCriticality(criticalityStr);

            String owner = extractField(OWNER_PATTERN, fileContent);

            ExtractedBusinessRule rule = new ExtractedBusinessRule(
                ruleId,
                name != null ? name.trim() : ruleId,
                description != null ? description.trim() : "",
                fileContent, // Conteúdo completo do markdown
                domain,
                criticality,
                filePath,
                owner != null ? owner.trim() : "unknown"
            );

            log.info("✅ [US#68] Markdown | Regra extraída | id={} | file={}", ruleId, filePath);
            return Optional.of(rule);

        } catch (Exception e) {
            log.error("❌ [US#68] Erro ao extrair regra de Markdown | file={} | error={}", filePath, e.getMessage());
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
