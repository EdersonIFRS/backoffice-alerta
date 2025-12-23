package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.ChangeType;
import com.backoffice.alerta.rules.Environment;
import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Request para análise consultiva de IA
 */
@Schema(description = "Requisição para análise consultiva de IA sobre riscos")
public class AIAdvisoryRequest {

    @Schema(description = "ID do Pull Request", example = "PR-456", required = true)
    private String pullRequestId;

    @Schema(description = "Ambiente de deploy", example = "PRODUCTION", required = true)
    private Environment environment;

    @Schema(description = "Tipo de mudança", example = "FEATURE", required = true)
    private ChangeType changeType;

    @Schema(description = "Nível de risco calculado", example = "ALTO", required = true)
    private RiskLevel riskLevel;

    @Schema(description = "Decisão final do sistema", example = "APROVADO_COM_RESTRICOES", required = true)
    private FinalDecision finalDecision;

    @Schema(description = "Regras de negócio impactadas")
    private List<ImpactedBusinessRuleSummary> impactedBusinessRules;

    @Schema(description = "Ações obrigatórias definidas")
    private List<String> mandatoryActions;

    public AIAdvisoryRequest() {
    }

    public AIAdvisoryRequest(String pullRequestId, Environment environment, ChangeType changeType,
                            RiskLevel riskLevel, FinalDecision finalDecision,
                            List<ImpactedBusinessRuleSummary> impactedBusinessRules,
                            List<String> mandatoryActions) {
        this.pullRequestId = pullRequestId;
        this.environment = environment;
        this.changeType = changeType;
        this.riskLevel = riskLevel;
        this.finalDecision = finalDecision;
        this.impactedBusinessRules = impactedBusinessRules;
        this.mandatoryActions = mandatoryActions;
    }

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

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public FinalDecision getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(FinalDecision finalDecision) {
        this.finalDecision = finalDecision;
    }

    public List<ImpactedBusinessRuleSummary> getImpactedBusinessRules() {
        return impactedBusinessRules;
    }

    public void setImpactedBusinessRules(List<ImpactedBusinessRuleSummary> impactedBusinessRules) {
        this.impactedBusinessRules = impactedBusinessRules;
    }

    public List<String> getMandatoryActions() {
        return mandatoryActions;
    }

    public void setMandatoryActions(List<String> mandatoryActions) {
        this.mandatoryActions = mandatoryActions;
    }
}
