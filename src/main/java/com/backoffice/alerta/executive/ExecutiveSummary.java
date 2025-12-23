package com.backoffice.alerta.executive;

import java.util.List;

/**
 * Sumário executivo da explicação de impacto
 * 
 * Contém interpretação em linguagem de negócio sobre o impacto sistêmico.
 * 
 * US#38 - Explicação Executiva Inteligente
 */
public class ExecutiveSummary {
    
    private final String headline;
    private final String businessImpact;
    private final List<String> areasAffected;
    private final String historicalContext;
    private final String riskInterpretation;
    private final String recommendation;
    
    public ExecutiveSummary(String headline,
                           String businessImpact,
                           List<String> areasAffected,
                           String historicalContext,
                           String riskInterpretation,
                           String recommendation) {
        this.headline = headline;
        this.businessImpact = businessImpact;
        this.areasAffected = areasAffected;
        this.historicalContext = historicalContext;
        this.riskInterpretation = riskInterpretation;
        this.recommendation = recommendation;
    }
    
    public String getHeadline() {
        return headline;
    }
    
    public String getBusinessImpact() {
        return businessImpact;
    }
    
    public List<String> getAreasAffected() {
        return areasAffected;
    }
    
    public String getHistoricalContext() {
        return historicalContext;
    }
    
    public String getRiskInterpretation() {
        return riskInterpretation;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
}
