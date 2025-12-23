package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response com decisão de aprovação de mudança
 */
@Schema(description = "Resposta com decisão de aprovação")
public class RiskDecisionResponse {

    @Schema(description = "ID do Pull Request", example = "PR-123")
    private String pullRequestId;

    @Schema(description = "Decisão final", example = "APROVADO_COM_RESTRICOES")
    private FinalDecision finalDecision;

    @Schema(description = "Nível de risco calculado", example = "ALTO")
    private RiskLevel riskLevel;

    @Schema(description = "Razão da decisão")
    private String decisionReason;

    @Schema(description = "Ações obrigatórias para aprovação")
    private List<String> requiredActions;

    @Schema(description = "Condições que devem ser atendidas")
    private List<String> conditions;

    @Schema(description = "Explicação detalhada da decisão")
    private String explanation;

    @Schema(description = "Ownerships relevantes (responsáveis que devem ser notificados/aprovadores)")
    private List<BusinessRuleOwnershipResponse> relevantOwnerships;

    public RiskDecisionResponse() {
    }

    public RiskDecisionResponse(String pullRequestId, FinalDecision finalDecision, 
                               RiskLevel riskLevel, String decisionReason,
                               List<String> requiredActions, List<String> conditions,
                               String explanation,
                               List<BusinessRuleOwnershipResponse> relevantOwnerships) {
        this.pullRequestId = pullRequestId;
        this.finalDecision = finalDecision;
        this.riskLevel = riskLevel;
        this.decisionReason = decisionReason;
        this.requiredActions = requiredActions;
        this.conditions = conditions;
        this.explanation = explanation;
        this.relevantOwnerships = relevantOwnerships;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

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

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public List<String> getRequiredActions() {
        return requiredActions;
    }

    public void setRequiredActions(List<String> requiredActions) {
        this.requiredActions = requiredActions;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<BusinessRuleOwnershipResponse> getRelevantOwnerships() {
        return relevantOwnerships;
    }

    public void setRelevantOwnerships(List<BusinessRuleOwnershipResponse> relevantOwnerships) {
        this.relevantOwnerships = relevantOwnerships;
    }
}
