package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * US#68 - Sumário de uma regra importada
 */
@Schema(description = "Sumário de regra de negócio importada do Git")
public class ImportedRuleSummary {

    @Schema(description = "ID da regra", example = "REGRA_VALIDACAO_PIX")
    private String ruleId;

    @Schema(description = "Nome da regra", example = "Validação de Chave PIX")
    private String ruleName;

    @Schema(description = "Arquivo fonte no repositório", example = "src/main/java/com/empresa/PixValidator.java")
    private String sourceFile;

    @Schema(description = "Ação executada: CREATED | UPDATED | SKIPPED", example = "CREATED")
    private String action;

    // Constructors
    public ImportedRuleSummary() {
    }

    public ImportedRuleSummary(String ruleId, String ruleName, String sourceFile, String action) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.sourceFile = sourceFile;
        this.action = action;
    }

    // Getters and Setters
    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
