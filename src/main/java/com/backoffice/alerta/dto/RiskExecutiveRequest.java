package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Requisição para gerar explicação executiva de risco")
public class RiskExecutiveRequest {

    @Schema(description = "Identificador único do Pull Request", example = "PR-12345")
    @NotBlank(message = "pullRequestId é obrigatório")
    private String pullRequestId;

    @Schema(
        description = "Versão das regras para análise",
        example = "v2",
        allowableValues = {"v1", "v2"}
    )
    private String ruleVersion;

    @Schema(description = "Política de aceite de risco")
    @NotNull(message = "policy é obrigatório")
    @Valid
    private RiskPolicyRequest.Policy policy;

    @Schema(description = "Lista de arquivos alterados no Pull Request")
    @NotEmpty(message = "files não pode ser vazio")
    @Valid
    private List<PullRequestRequest.FileChange> files;

    public RiskExecutiveRequest() {
    }

    public RiskExecutiveRequest(String pullRequestId, String ruleVersion, 
                                RiskPolicyRequest.Policy policy, List<PullRequestRequest.FileChange> files) {
        this.pullRequestId = pullRequestId;
        this.ruleVersion = ruleVersion;
        this.policy = policy;
        this.files = files;
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

    public RiskPolicyRequest.Policy getPolicy() {
        return policy;
    }

    public void setPolicy(RiskPolicyRequest.Policy policy) {
        this.policy = policy;
    }

    public List<PullRequestRequest.FileChange> getFiles() {
        return files;
    }

    public void setFiles(List<PullRequestRequest.FileChange> files) {
        this.files = files;
    }
}
