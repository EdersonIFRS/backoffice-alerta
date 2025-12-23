package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta contendo relatório consolidado de risco")
public class RiskReportResponse {

    @Schema(description = "Identificador único do Pull Request", example = "PR-12345")
    private String pullRequestId;

    @Schema(description = "Relatório consolidado de risco")
    private RiskReport riskReport;

    public RiskReportResponse() {
    }

    public RiskReportResponse(String pullRequestId, RiskReport riskReport) {
        this.pullRequestId = pullRequestId;
        this.riskReport = riskReport;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public RiskReport getRiskReport() {
        return riskReport;
    }

    public void setRiskReport(RiskReport riskReport) {
        this.riskReport = riskReport;
    }
}
