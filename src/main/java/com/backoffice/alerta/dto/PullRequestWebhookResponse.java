package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Environment;
import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskLevel;

import java.util.List;
import java.util.UUID;

/**
 * Response retornado após processamento do webhook de Pull Request
 * 
 * Contém a decisão de risco e ações requeridas para o CI/CD pipeline
 * 
 * Pipeline CI/CD pode usar finalDecision para:
 * - BLOQUEADO → falhar o build
 * - APROVADO_COM_RESTRICOES → gerar warning
 * - APROVADO → seguir normalmente
 */
public class PullRequestWebhookResponse {
    
    private final String pullRequestId;
    private final FinalDecision finalDecision;
    private final RiskLevel riskLevel;
    private final Environment environment;
    private final List<String> requiredActions;
    private final UUID auditId;

    public PullRequestWebhookResponse(String pullRequestId,
                                     FinalDecision finalDecision,
                                     RiskLevel riskLevel,
                                     Environment environment,
                                     List<String> requiredActions,
                                     UUID auditId) {
        this.pullRequestId = pullRequestId;
        this.finalDecision = finalDecision;
        this.riskLevel = riskLevel;
        this.environment = environment;
        this.requiredActions = requiredActions;
        this.auditId = auditId;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public FinalDecision getFinalDecision() {
        return finalDecision;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public List<String> getRequiredActions() {
        return requiredActions;
    }

    public UUID getAuditId() {
        return auditId;
    }
}
