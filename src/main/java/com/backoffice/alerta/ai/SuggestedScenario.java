package com.backoffice.alerta.ai;

import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskLevel;

import java.util.List;

/**
 * Cenário sugerido pela IA com avaliação e explicação
 * 
 * US#34 - IA sugere automaticamente cenários ótimos de decisão
 */
public class SuggestedScenario {
    
    private String scenarioId;
    private String description;
    private RiskLevel riskLevel;
    private FinalDecision decision;
    private boolean slaRemoved;
    private List<String> teamsNotified;
    private int score;
    private String explanation;

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public FinalDecision getDecision() {
        return decision;
    }

    public void setDecision(FinalDecision decision) {
        this.decision = decision;
    }

    public boolean isSlaRemoved() {
        return slaRemoved;
    }

    public void setSlaRemoved(boolean slaRemoved) {
        this.slaRemoved = slaRemoved;
    }

    public List<String> getTeamsNotified() {
        return teamsNotified;
    }

    public void setTeamsNotified(List<String> teamsNotified) {
        this.teamsNotified = teamsNotified;
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
}
