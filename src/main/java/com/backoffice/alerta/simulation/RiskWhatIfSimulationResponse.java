package com.backoffice.alerta.simulation;

/**
 * Response de simulação "What-If" de decisão de risco
 * 
 * US#33 - Simulação Executiva de Decisão de Risco
 */
public class RiskWhatIfSimulationResponse {
    
    private String pullRequestId;
    private SimulationResult baseline;
    private SimulationResult simulation;
    private SimulationDelta delta;
    private ExecutiveRecommendation executiveRecommendation;

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public SimulationResult getBaseline() {
        return baseline;
    }

    public void setBaseline(SimulationResult baseline) {
        this.baseline = baseline;
    }

    public SimulationResult getSimulation() {
        return simulation;
    }

    public void setSimulation(SimulationResult simulation) {
        this.simulation = simulation;
    }

    public SimulationDelta getDelta() {
        return delta;
    }

    public void setDelta(SimulationDelta delta) {
        this.delta = delta;
    }

    public ExecutiveRecommendation getExecutiveRecommendation() {
        return executiveRecommendation;
    }

    public void setExecutiveRecommendation(ExecutiveRecommendation executiveRecommendation) {
        this.executiveRecommendation = executiveRecommendation;
    }
}
