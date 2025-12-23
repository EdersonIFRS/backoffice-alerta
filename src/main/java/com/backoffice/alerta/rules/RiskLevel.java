package com.backoffice.alerta.rules;

/**
 * Nível de risco calculado para uma regra de negócio impactada
 */
public enum RiskLevel {
    BAIXO("Baixo"),
    MEDIO("Médio"),
    ALTO("Alto"),
    CRITICO("Crítico");

    private final String description;

    RiskLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Calcula o nível de risco baseado na criticidade e tipo de impacto
     * @param criticality Criticidade da regra
     * @param impactType Tipo de impacto
     * @return Nível de risco calculado
     */
    public static RiskLevel calculateRiskLevel(Criticality criticality, ImpactType impactType) {
        if (criticality == Criticality.CRITICA) {
            return impactType == ImpactType.DIRECT ? CRITICO : ALTO;
        } else if (criticality == Criticality.ALTA) {
            return impactType == ImpactType.DIRECT ? ALTO : MEDIO;
        } else if (criticality == Criticality.MEDIA) {
            return impactType == ImpactType.DIRECT ? MEDIO : BAIXO;
        } else {
            return BAIXO;
        }
    }

    /**
     * Retorna o maior nível de risco entre dois
     * @param level1 Primeiro nível
     * @param level2 Segundo nível
     * @return O maior nível de risco
     */
    public static RiskLevel max(RiskLevel level1, RiskLevel level2) {
        if (level1 == null) return level2;
        if (level2 == null) return level1;
        return level1.ordinal() > level2.ordinal() ? level1 : level2;
    }
}
