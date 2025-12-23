package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado da avaliação de um Pull Request contra uma política de risco")
public class RiskPolicyResponse {

    @Schema(description = "Identificador do Pull Request analisado", example = "PR-12345")
    private String pullRequestId;

    @Schema(description = "Versão das regras aplicada na análise", example = "v2")
    private String ruleVersion;

    @Schema(description = "Score de risco calculado", example = "85")
    private int riskScore;

    @Schema(description = "Nível de risco", example = "CRÍTICO", allowableValues = {"BAIXO", "MÉDIO", "ALTO", "CRÍTICO"})
    private String riskLevel;

    @Schema(
        description = "Decisão da política de risco",
        example = "BLOQUEADO",
        allowableValues = {"APROVADO", "REVISÃO OBRIGATÓRIA", "BLOQUEADO"}
    )
    private String policyDecision;

    @Schema(description = "Explicação da decisão", example = "O nível de risco CRÍTICO excede o máximo permitido (MÉDIO)")
    private String reason;

    public RiskPolicyResponse() {
    }

    public RiskPolicyResponse(String pullRequestId, String ruleVersion, int riskScore,
                              String riskLevel, String policyDecision, String reason) {
        this.pullRequestId = pullRequestId;
        this.ruleVersion = ruleVersion;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.policyDecision = policyDecision;
        this.reason = reason;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public String getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(String ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getPolicyDecision() {
        return policyDecision;
    }

    public void setPolicyDecision(String policyDecision) {
        this.policyDecision = policyDecision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
