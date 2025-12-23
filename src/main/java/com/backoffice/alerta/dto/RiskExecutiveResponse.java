package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com explicação executiva de risco")
public class RiskExecutiveResponse {

    @Schema(description = "Identificador do Pull Request analisado", example = "PR-12345")
    private String pullRequestId;

    @Schema(description = "Resumo executivo do risco")
    private ExecutiveSummary executiveSummary;

    public RiskExecutiveResponse() {
    }

    public RiskExecutiveResponse(String pullRequestId, ExecutiveSummary executiveSummary) {
        this.pullRequestId = pullRequestId;
        this.executiveSummary = executiveSummary;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public ExecutiveSummary getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(ExecutiveSummary executiveSummary) {
        this.executiveSummary = executiveSummary;
    }
}
