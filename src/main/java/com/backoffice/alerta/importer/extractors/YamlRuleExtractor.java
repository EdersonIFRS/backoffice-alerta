package com.backoffice.alerta.importer.extractors;

import com.backoffice.alerta.dto.ExtractedBusinessRule;
import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Domain;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * US#68 - Extrator de regras de negócio a partir de arquivos YAML
 * 
 * Formato esperado:
 * businessRule:
 *   id: REGRA_VALIDACAO_PIX
 *   name: Validação de Chave PIX
 *   description: Valida formato da chave PIX conforme BC
 *   domain: PAGAMENTOS
 *   criticality: HIGH
 *   owner: time-pagamentos
 */
@Component
public class YamlRuleExtractor {

    private static final Logger log = LoggerFactory.getLogger(YamlRuleExtractor.class);

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * Extrai regra de negócio de arquivo YAML
     * 
     * @param fileContent Conteúdo do arquivo YAML
     * @param filePath Caminho do arquivo (para logs)
     * @return Optional com regra extraída, ou vazio se não encontrar
     */
    public Optional<ExtractedBusinessRule> extract(String fileContent, String filePath) {
        try {
            // Parse YAML
            @SuppressWarnings("unchecked")
            Map<String, Object> yaml = yamlMapper.readValue(fileContent, Map.class);

            // Verifica se há seção businessRule
            if (!yaml.containsKey("businessRule")) {
                log.debug("🔍 [US#68] Nenhuma seção 'businessRule' encontrada em YAML | file={}", filePath);
                return Optional.empty();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> ruleData = (Map<String, Object>) yaml.get("businessRule");

            // Extrai campos obrigatórios
            String ruleId = getStringField(ruleData, "id");
            if (ruleId == null || ruleId.isBlank()) {
                log.warn("⚠️ [US#68] businessRule sem 'id' | file={}", filePath);
                return Optional.empty();
            }

            String name = getStringField(ruleData, "name");
            String description = getStringField(ruleData, "description");

            String domainStr = getStringField(ruleData, "domain");
            Domain domain = parseDomain(domainStr);

            String criticalityStr = getStringField(ruleData, "criticality");
            Criticality criticality = parseCriticality(criticalityStr);

            String owner = getStringField(ruleData, "owner");

            ExtractedBusinessRule rule = new ExtractedBusinessRule(
                ruleId.trim(),
                name != null ? name.trim() : ruleId,
                description != null ? description.trim() : "",
                fileContent, // Conteúdo completo do arquivo YAML
                domain,
                criticality,
                filePath,
                owner != null ? owner.trim() : "unknown"
            );

            log.info("✅ [US#68] YAML | Regra extraída | id={} | file={}", ruleId, filePath);
            return Optional.of(rule);

        } catch (Exception e) {
            log.error("❌ [US#68] Erro ao extrair regra de YAML | file={} | error={}", filePath, e.getMessage());
            return Optional.empty();
        }
    }

    private String getStringField(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : null;
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
