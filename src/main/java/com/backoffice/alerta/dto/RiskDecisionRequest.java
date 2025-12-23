package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.ChangeType;
import com.backoffice.alerta.rules.Environment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Request para decisão de aprovação de mudança
 */
@Schema(description = "Requisição para decisão de aprovação de Pull Request")
public class RiskDecisionRequest {

    @Schema(description = "ID do Pull Request", example = "PR-123", required = true)
    private String pullRequestId;

    @Schema(description = "Versão da regra de decisão", example = "v2")
    private String ruleVersion;

    @Schema(description = "Ambiente de deploy", example = "PRODUCTION", required = true)
    private Environment environment;

    @Schema(description = "Tipo de mudança", example = "FEATURE", required = true)
    private ChangeType changeType;

    @Schema(description = "Política de aprovação")
    private ApprovalPolicy policy;

    @Schema(description = "Lista de arquivos alterados", required = true)
    private List<String> changedFiles;

    public RiskDecisionRequest() {
    }

    public RiskDecisionRequest(String pullRequestId, String ruleVersion, Environment environment,
                              ChangeType changeType, ApprovalPolicy policy, List<String> changedFiles) {
        this.pullRequestId = pullRequestId;
        this.ruleVersion = ruleVersion;
        this.environment = environment;
        this.changeType = changeType;
        this.policy = policy;
        this.changedFiles = changedFiles;
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

    public ApprovalPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(ApprovalPolicy policy) {
        this.policy = policy;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(List<String> changedFiles) {
        this.changedFiles = changedFiles;
    }
}
