package com.backoffice.alerta.rules;

/**
 * Severidade de incidentes de produção relacionados a regras de negócio
 */
public enum IncidentSeverity {
    LOW("Baixa", 5),
    MEDIUM("Média", 10),
    HIGH("Alta", 15),
    CRITICAL("Crítica", 20);

    private final String description;
    private final int riskWeight;

    IncidentSeverity(String description, int riskWeight) {
        this.description = description;
        this.riskWeight = riskWeight;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Retorna o peso de risco que este incidente adiciona à análise
     * @return Peso de risco (LOW=5, MEDIUM=10, HIGH=15, CRITICAL=20)
     */
    public int getRiskWeight() {
        return riskWeight;
    }
}
