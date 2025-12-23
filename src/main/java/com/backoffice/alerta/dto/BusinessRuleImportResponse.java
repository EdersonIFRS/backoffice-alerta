package com.backoffice.alerta.dto;

import com.backoffice.alerta.project.dto.ProjectContext;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * US#68 - Response da importação de regras de negócio do Git
 */
@Schema(description = "Resultado da importação automática de regras de negócio a partir do Git")
public class BusinessRuleImportResponse {

    @Schema(description = "Total de regras detectadas no repositório", example = "5")
    private int rulesDetected;

    @Schema(description = "Total de regras criadas", example = "3")
    private int rulesCreated;

    @Schema(description = "Total de regras atualizadas", example = "1")
    private int rulesUpdated;

    @Schema(description = "Total de regras ignoradas (duplicadas)", example = "1")
    private int rulesSkipped;

    @Schema(description = "Lista detalhada das regras importadas")
    private List<ImportedRuleSummary> rules = new ArrayList<>();

    @Schema(description = "Contexto do projeto associado")
    private ProjectContext projectContext;

    @Schema(description = "Se true, foram usados extractors de fallback (parsing simples)", example = "false")
    private boolean usedFallback;

    // Constructors
    public BusinessRuleImportResponse() {
    }

    public BusinessRuleImportResponse(int rulesDetected, int rulesCreated, int rulesUpdated, int rulesSkipped, 
                                     List<ImportedRuleSummary> rules, ProjectContext projectContext, boolean usedFallback) {
        this.rulesDetected = rulesDetected;
        this.rulesCreated = rulesCreated;
        this.rulesUpdated = rulesUpdated;
        this.rulesSkipped = rulesSkipped;
        this.rules = rules;
        this.projectContext = projectContext;
        this.usedFallback = usedFallback;
    }

    // Getters and Setters
    public int getRulesDetected() {
        return rulesDetected;
    }

    public void setRulesDetected(int rulesDetected) {
        this.rulesDetected = rulesDetected;
    }

    public int getRulesCreated() {
        return rulesCreated;
    }

    public void setRulesCreated(int rulesCreated) {
        this.rulesCreated = rulesCreated;
    }

    public int getRulesUpdated() {
        return rulesUpdated;
    }

    public void setRulesUpdated(int rulesUpdated) {
        this.rulesUpdated = rulesUpdated;
    }

    public int getRulesSkipped() {
        return rulesSkipped;
    }

    public void setRulesSkipped(int rulesSkipped) {
        this.rulesSkipped = rulesSkipped;
    }

    public List<ImportedRuleSummary> getRules() {
        return rules;
    }

    public void setRules(List<ImportedRuleSummary> rules) {
        this.rules = rules;
    }

    public ProjectContext getProjectContext() {
        return projectContext;
    }

    public void setProjectContext(ProjectContext projectContext) {
        this.projectContext = projectContext;
    }

    public boolean isUsedFallback() {
        return usedFallback;
    }

    public void setUsedFallback(boolean usedFallback) {
        this.usedFallback = usedFallback;
    }
}
