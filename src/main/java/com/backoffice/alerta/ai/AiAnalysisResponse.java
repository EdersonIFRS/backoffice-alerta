package com.backoffice.alerta.ai;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta da análise inteligente de mudança")
public class AiAnalysisResponse {

    @Schema(description = "Identificador do Pull Request analisado", example = "PR-12345")
    private String pullRequestId;

    @Schema(description = "Avaliação inteligente da mudança")
    private AiAssessment aiAssessment;

    public AiAnalysisResponse() {
    }

    public AiAnalysisResponse(String pullRequestId, AiAssessment aiAssessment) {
        this.pullRequestId = pullRequestId;
        this.aiAssessment = aiAssessment;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public AiAssessment getAiAssessment() {
        return aiAssessment;
    }

    public void setAiAssessment(AiAssessment aiAssessment) {
        this.aiAssessment = aiAssessment;
    }
}
