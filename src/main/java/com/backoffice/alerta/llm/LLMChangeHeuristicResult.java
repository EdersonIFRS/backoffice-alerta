package com.backoffice.alerta.llm;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * US#70 - Resultado de uma heurística de detecção de código gerado por LLM
 */
@Schema(description = "Resultado de uma heurística de detecção LLM")
public class LLMChangeHeuristicResult {

    @Schema(description = "Nome da heurística aplicada", example = "MASSIVE_METHOD_CHANGE")
    private String heuristic;

    @Schema(description = "Score atribuído por esta heurística", example = "25")
    private int score;

    @Schema(description = "Explicação detalhada do porque esta heurística foi ativada")
    private String explanation;

    @Schema(description = "Arquivos afetados por esta heurística")
    private List<String> affectedFiles = new ArrayList<>();

    public LLMChangeHeuristicResult() {
    }

    public LLMChangeHeuristicResult(String heuristic, int score, String explanation) {
        this.heuristic = heuristic;
        this.score = score;
        this.explanation = explanation;
    }

    public String getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(String heuristic) {
        this.heuristic = heuristic;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getAffectedFiles() {
        return affectedFiles;
    }

    public void setAffectedFiles(List<String> affectedFiles) {
        this.affectedFiles = affectedFiles;
    }
}
