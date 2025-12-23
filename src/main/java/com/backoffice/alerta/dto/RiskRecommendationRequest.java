package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Requisição para obter recomendações de mitigação de risco")
public class RiskRecommendationRequest {

    @Schema(description = "Identificador único do Pull Request", example = "PR-12345")
    @NotBlank(message = "pullRequestId é obrigatório")
    private String pullRequestId;

    @Schema(
        description = "Versão das regras para análise",
        example = "v2",
        allowableValues = {"v1", "v2"}
    )
    private String ruleVersion;

    @Schema(
        description = "Nível de risco desejado",
        example = "MÉDIO",
        allowableValues = {"BAIXO", "MÉDIO", "ALTO"}
    )
    @NotBlank(message = "targetRiskLevel é obrigatório")
    private String targetRiskLevel;

    @Schema(description = "Lista de arquivos alterados no Pull Request")
    @NotEmpty(message = "files não pode ser vazio")
    @Valid
    private List<PullRequestRequest.FileChange> files;

    public RiskRecommendationRequest() {
    }

    public RiskRecommendationRequest(String pullRequestId, String ruleVersion, 
                                     String targetRiskLevel, List<PullRequestRequest.FileChange> files) {
        this.pullRequestId = pullRequestId;
        this.ruleVersion = ruleVersion;
        this.targetRiskLevel = targetRiskLevel;
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

    public String getTargetRiskLevel() {
        return targetRiskLevel;
    }

    public void setTargetRiskLevel(String targetRiskLevel) {
        this.targetRiskLevel = targetRiskLevel;
    }

    public List<PullRequestRequest.FileChange> getFiles() {
        return files;
    }

    public void setFiles(List<PullRequestRequest.FileChange> files) {
        this.files = files;
    }
}
