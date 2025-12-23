package com.backoffice.alerta.simulation;

import com.backoffice.alerta.rules.ChangeType;
import com.backoffice.alerta.rules.Environment;
import com.backoffice.alerta.rules.FinalDecision;

/**
 * Request para simulação "What-If" de decisão de risco
 * 
 * US#33 - Simulação Executiva de Decisão de Risco
 */
public class RiskWhatIfSimulationRequest {
    
    private String pullRequestId;
    private Environment environment;
    private ChangeType changeType;
    private FinalDecision baselineDecision;
    private WhatIfScenario whatIf;

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public FinalDecision getBaselineDecision() {
        return baselineDecision;
    }

    public void setBaselineDecision(FinalDecision baselineDecision) {
        this.baselineDecision = baselineDecision;
    }

    public WhatIfScenario getWhatIf() {
        return whatIf;
    }

    public void setWhatIf(WhatIfScenario whatIf) {
        this.whatIf = whatIf;
    }
}
