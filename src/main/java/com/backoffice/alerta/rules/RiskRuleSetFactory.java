package com.backoffice.alerta.rules;

import org.springframework.stereotype.Component;

/**
 * Factory para obter a versão correta das regras de risco
 */
@Component
public class RiskRuleSetFactory {

    private final RiskRuleSetV1 riskRuleSetV1;
    private final RiskRuleSetV2 riskRuleSetV2;

    public RiskRuleSetFactory(RiskRuleSetV1 riskRuleSetV1, RiskRuleSetV2 riskRuleSetV2) {
        this.riskRuleSetV1 = riskRuleSetV1;
        this.riskRuleSetV2 = riskRuleSetV2;
    }

    /**
     * Retorna o conjunto de regras baseado na versão solicitada
     * Se versão não for informada ou for inválida, retorna V1 (padrão)
     */
    public RiskRuleSet getRuleSet(String version) {
        if (version == null || version.trim().isEmpty()) {
            return riskRuleSetV1;
        }

        String normalizedVersion = version.trim().toLowerCase();
        
        switch (normalizedVersion) {
            case "v2":
                return riskRuleSetV2;
            case "v1":
            default:
                return riskRuleSetV1;
        }
    }
}
