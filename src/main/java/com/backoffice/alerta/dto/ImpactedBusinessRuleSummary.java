package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.ImpactType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Sumário de regra de negócio impactada para análise de IA
 */
@Schema(description = "Resumo de regra de negócio impactada")
public class ImpactedBusinessRuleSummary {

    @Schema(description = "ID da regra de negócio", example = "BR-001")
    private String ruleId;

    @Schema(description = "Nome da regra", example = "Processamento de Pagamentos")
    private String name;

    @Schema(description = "Criticidade da regra", example = "CRITICA")
    private Criticality criticality;

    @Schema(description = "Tipo de impacto", example = "DIRECT")
    private ImpactType impactType;

    @Schema(description = "Quantidade de incidentes históricos", example = "3")
    private int incidentCount;

    public ImpactedBusinessRuleSummary() {
    }

    public ImpactedBusinessRuleSummary(String ruleId, String name, Criticality criticality,
                                      ImpactType impactType, int incidentCount) {
        this.ruleId = ruleId;
        this.name = name;
        this.criticality = criticality;
        this.impactType = impactType;
        this.incidentCount = incidentCount;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public void setCriticality(Criticality criticality) {
        this.criticality = criticality;
    }

    public ImpactType getImpactType() {
        return impactType;
    }

    public void setImpactType(ImpactType impactType) {
        this.impactType = impactType;
    }

    public int getIncidentCount() {
        return incidentCount;
    }

    public void setIncidentCount(int incidentCount) {
        this.incidentCount = incidentCount;
    }
}
