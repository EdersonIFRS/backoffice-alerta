package com.backoffice.alerta.dto;

import com.backoffice.alerta.project.dto.ProjectContext;

import java.util.List;

/**
 * Response completo da comparação histórica de decisões
 * 
 * US#41 - Comparação Histórica de Decisões de Risco
 */
public class DecisionHistoricalComparisonResponse {
    
    private final CurrentDecisionContextResponse currentContextSummary;
    private final List<HistoricalDecisionComparisonResponse> historicalComparisons;
    private final ExecutiveHistoricalInsightResponse executiveInsights;
    private final ProjectContext projectContext;
    
    public DecisionHistoricalComparisonResponse(
            CurrentDecisionContextResponse currentContextSummary,
            List<HistoricalDecisionComparisonResponse> historicalComparisons,
            ExecutiveHistoricalInsightResponse executiveInsights,
            ProjectContext projectContext) {
        this.currentContextSummary = currentContextSummary;
        this.historicalComparisons = historicalComparisons;
        this.executiveInsights = executiveInsights;
        this.projectContext = projectContext;
    }
    
    public CurrentDecisionContextResponse getCurrentContextSummary() {
        return currentContextSummary;
    }
    
    public List<HistoricalDecisionComparisonResponse> getHistoricalComparisons() {
        return historicalComparisons;
    }
    
    public ExecutiveHistoricalInsightResponse getExecutiveInsights() {
        return executiveInsights;
    }
    
    public ProjectContext getProjectContext() {
        return projectContext;
    }
}
