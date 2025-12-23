package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Requisição para sugestão automática de testes")
public class TestSuggestionRequest {

    @Schema(description = "Identificador único do Pull Request", example = "PR-001")
    @NotBlank(message = "pullRequestId é obrigatório")
    private String pullRequestId;

    @Schema(
        description = "Versão das regras para análise",
        example = "v2",
        allowableValues = {"v1", "v2"}
    )
    private String ruleVersion;

    @Schema(
        description = "Nível de cobertura de testes desejado",
        example = "ALTA",
        allowableValues = {"BAIXA", "MÉDIA", "ALTA"}
    )
    @NotNull(message = "targetCoverageLevel é obrigatório")
    private String targetCoverageLevel;

    @Schema(description = "Lista de arquivos alterados no Pull Request")
    @NotEmpty(message = "files não pode ser vazio")
    @Valid
    private List<PullRequestRequest.FileChange> files;

    public TestSuggestionRequest() {
    }

    public TestSuggestionRequest(String pullRequestId, String ruleVersion, 
                                String targetCoverageLevel, List<PullRequestRequest.FileChange> files) {
        this.pullRequestId = pullRequestId;
        this.ruleVersion = ruleVersion;
        this.targetCoverageLevel = targetCoverageLevel;
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

    public String getTargetCoverageLevel() {
        return targetCoverageLevel;
    }

    public void setTargetCoverageLevel(String targetCoverageLevel) {
        this.targetCoverageLevel = targetCoverageLevel;
    }

    public List<PullRequestRequest.FileChange> getFiles() {
        return files;
    }

    public void setFiles(List<PullRequestRequest.FileChange> files) {
        this.files = files;
    }
}
