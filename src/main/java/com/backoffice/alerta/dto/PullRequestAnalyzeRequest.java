package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Requisição para análise de risco baseada em Pull Request ID.
 * Não requer envio manual de arquivos - dados são buscados via provider.
 */
@Schema(description = "Requisição para análise de risco baseada em Pull Request ID")
public class PullRequestAnalyzeRequest {

    @Schema(description = "Identificador único do Pull Request", example = "PR-12345")
    @NotBlank(message = "pullRequestId é obrigatório")
    private String pullRequestId;

    @Schema(
        description = "Versão das regras para análise",
        example = "v2",
        allowableValues = {"v1", "v2"}
    )
    private String ruleVersion;

    public PullRequestAnalyzeRequest() {
    }

    public PullRequestAnalyzeRequest(String pullRequestId, String ruleVersion) {
        this.pullRequestId = pullRequestId;
        this.ruleVersion = ruleVersion;
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
}
