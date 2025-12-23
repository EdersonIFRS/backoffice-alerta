package com.backoffice.alerta.dto;

/**
 * Insights executivos baseados em padrões históricos
 * 
 * US#41 - Comparação Histórica de Decisões de Risco
 * 
 * IMPORTANTE: Gerado de forma determinística (sem IA)
 */
public class ExecutiveHistoricalInsightResponse {
    
    private final boolean patternDetected;
    private final String patternDescription;
    private final String recommendation;
    
    public ExecutiveHistoricalInsightResponse(boolean patternDetected,
                                             String patternDescription,
                                             String recommendation) {
        this.patternDetected = patternDetected;
        this.patternDescription = patternDescription;
        this.recommendation = recommendation;
    }
    
    public boolean isPatternDetected() {
        return patternDetected;
    }
    
    public String getPatternDescription() {
        return patternDescription;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
}
