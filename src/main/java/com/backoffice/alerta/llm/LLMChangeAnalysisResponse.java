package com.backoffice.alerta.llm;

import com.backoffice.alerta.project.dto.ProjectContext;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * US#70 - Resposta da análise de mudanças suspeitas de LLM
 */
@Schema(description = "Resultado da análise de mudanças potencialmente geradas por LLM")
public class LLMChangeAnalysisResponse {

    @Schema(description = "Score total de suspeição (0-100)", example = "72")
    private int totalScore;

    @Schema(description = "Nível de suspeição baseado no score", example = "HIGH")
    private LLMSuspicionLevel suspicionLevel;

    @Schema(description = "Lista de heurísticas aplicadas e seus resultados")
    private List<LLMChangeHeuristicResult> heuristics = new ArrayList<>();

    @Schema(description = "Se a mudança afeta regra de negócio crítica", example = "true")
    private boolean affectsCriticalRule;

    @Schema(description = "Se a mudança excede o escopo das regras impactadas", example = "true")
    private boolean exceedsRuleScope;

    @Schema(description = "Contexto de projeto (se aplicável)")
    private ProjectContext projectContext;

    @Schema(description = "Resumo executivo da análise em linguagem humana")
    private String summary;

    @Schema(description = "ID do Pull Request analisado", example = "123")
    private String pullRequestId;

    @Schema(description = "Total de arquivos analisados", example = "5")
    private int totalFilesAnalyzed;

    @Schema(description = "Total de arquivos Java analisados com AST", example = "3")
    private int javaFilesAnalyzed;

    public LLMChangeAnalysisResponse() {
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public LLMSuspicionLevel getSuspicionLevel() {
        return suspicionLevel;
    }

    public void setSuspicionLevel(LLMSuspicionLevel suspicionLevel) {
        this.suspicionLevel = suspicionLevel;
    }

    public List<LLMChangeHeuristicResult> getHeuristics() {
        return heuristics;
    }

    public void setHeuristics(List<LLMChangeHeuristicResult> heuristics) {
        this.heuristics = heuristics;
    }

    public boolean isAffectsCriticalRule() {
        return affectsCriticalRule;
    }

    public void setAffectsCriticalRule(boolean affectsCriticalRule) {
        this.affectsCriticalRule = affectsCriticalRule;
    }

    public boolean isExceedsRuleScope() {
        return exceedsRuleScope;
    }

    public void setExceedsRuleScope(boolean exceedsRuleScope) {
        this.exceedsRuleScope = exceedsRuleScope;
    }

    public ProjectContext getProjectContext() {
        return projectContext;
    }

    public void setProjectContext(ProjectContext projectContext) {
        this.projectContext = projectContext;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public int getTotalFilesAnalyzed() {
        return totalFilesAnalyzed;
    }

    public void setTotalFilesAnalyzed(int totalFilesAnalyzed) {
        this.totalFilesAnalyzed = totalFilesAnalyzed;
    }

    public int getJavaFilesAnalyzed() {
        return javaFilesAnalyzed;
    }

    public void setJavaFilesAnalyzed(int javaFilesAnalyzed) {
        this.javaFilesAnalyzed = javaFilesAnalyzed;
    }
}
