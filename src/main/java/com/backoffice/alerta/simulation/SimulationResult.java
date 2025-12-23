package com.backoffice.alerta.simulation;

import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de uma simulação de decisão de risco
 * 
 * US#33 - Simulação Executiva de Decisão de Risco
 */
public class SimulationResult {
    
    private FinalDecision finalDecision;
    private RiskLevel riskLevel;
    private int impactedRules;
    private boolean slaTriggered;
    private List<String> notifiedTeams = new ArrayList<>();
    private List<String> restrictions = new ArrayList<>();

    public FinalDecision getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(FinalDecision finalDecision) {
        this.finalDecision = finalDecision;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getImpactedRules() {
        return impactedRules;
    }

    public void setImpactedRules(int impactedRules) {
        this.impactedRules = impactedRules;
    }

    public boolean isSlaTriggered() {
        return slaTriggered;
    }

    public void setSlaTriggered(boolean slaTriggered) {
        this.slaTriggered = slaTriggered;
    }

    public List<String> getNotifiedTeams() {
        return notifiedTeams;
    }

    public void setNotifiedTeams(List<String> notifiedTeams) {
        this.notifiedTeams = notifiedTeams != null ? notifiedTeams : new ArrayList<>();
    }

    public List<String> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<String> restrictions) {
        this.restrictions = restrictions != null ? restrictions : new ArrayList<>();
    }
}
