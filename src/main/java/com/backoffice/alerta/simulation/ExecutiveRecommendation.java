package com.backoffice.alerta.simulation;

/**
 * Recomendação executiva baseada na simulação
 * 
 * US#33 - Simulação Executiva de Decisão de Risco
 */
public class ExecutiveRecommendation {
    
    private String headline;
    private String confidence; // BAIXA, MEDIA, ALTA
    private String summary;

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
