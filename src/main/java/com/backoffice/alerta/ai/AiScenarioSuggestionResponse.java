package com.backoffice.alerta.ai;

import java.util.List;

/**
 * Response com baseline e cenários sugeridos ordenados por score
 * 
 * US#34 - IA sugere automaticamente cenários ótimos de decisão
 */
public class AiScenarioSuggestionResponse {
    
    private BaselineInfo baseline;
    private List<SuggestedScenario> suggestedScenarios;

    public BaselineInfo getBaseline() {
        return baseline;
    }

    public void setBaseline(BaselineInfo baseline) {
        this.baseline = baseline;
    }

    public List<SuggestedScenario> getSuggestedScenarios() {
        return suggestedScenarios;
    }

    public void setSuggestedScenarios(List<SuggestedScenario> suggestedScenarios) {
        this.suggestedScenarios = suggestedScenarios;
    }

    /**
     * Informação resumida do cenário baseline (atual)
     */
    public static class BaselineInfo {
        private String riskLevel;
        private String decision;

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public String getDecision() {
            return decision;
        }

        public void setDecision(String decision) {
            this.decision = decision;
        }
    }
}
