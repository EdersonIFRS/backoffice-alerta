package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Domain;
import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskLevel;

import java.util.List;

/**
 * Contexto da decisão atual para comparação histórica
 * 
 * US#41 - Comparação Histórica de Decisões de Risco
 */
public class CurrentDecisionContextResponse {
    
    private final RiskLevel riskLevel;
    private final FinalDecision finalDecision;
    private final List<Domain> businessDomains;
    private final int criticalRules;
    
    public CurrentDecisionContextResponse(RiskLevel riskLevel,
                                         FinalDecision finalDecision,
                                         List<Domain> businessDomains,
                                         int criticalRules) {
        this.riskLevel = riskLevel;
        this.finalDecision = finalDecision;
        this.businessDomains = businessDomains;
        this.criticalRules = criticalRules;
    }
    
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    public FinalDecision getFinalDecision() {
        return finalDecision;
    }
    
    public List<Domain> getBusinessDomains() {
        return businessDomains;
    }
    
    public int getCriticalRules() {
        return criticalRules;
    }
}
