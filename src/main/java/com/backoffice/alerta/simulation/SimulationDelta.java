package com.backoffice.alerta.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Delta entre baseline e simulação
 * 
 * US#33 - Simulação Executiva de Decisão de Risco
 */
public class SimulationDelta {
    
    private String riskReduction;
    private List<String> rulesNoLongerImpacted = new ArrayList<>();
    private String slaImpact;

    public String getRiskReduction() {
        return riskReduction;
    }

    public void setRiskReduction(String riskReduction) {
        this.riskReduction = riskReduction;
    }

    public List<String> getRulesNoLongerImpacted() {
        return rulesNoLongerImpacted;
    }

    public void setRulesNoLongerImpacted(List<String> rulesNoLongerImpacted) {
        this.rulesNoLongerImpacted = rulesNoLongerImpacted != null ? rulesNoLongerImpacted : new ArrayList<>();
    }

    public String getSlaImpact() {
        return slaImpact;
    }

    public void setSlaImpact(String slaImpact) {
        this.slaImpact = slaImpact;
    }
}
