package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Requisição para comparar risco entre duas versões de regras")
public class RiskComparisonRequest {

    @Schema(description = "Identificador único do Pull Request", example = "PR-12345")
    @NotBlank(message = "pullRequestId é obrigatório")
    private String pullRequestId;

    @Schema(
        description = "Versão inicial das regras para comparação",
        example = "v1",
        allowableValues = {"v1", "v2"}
    )
    @NotBlank(message = "fromVersion é obrigatório")
    private String fromVersion;

    @Schema(
        description = "Versão final das regras para comparação",
        example = "v2",
        allowableValues = {"v1", "v2"}
    )
    @NotBlank(message = "toVersion é obrigatório")
    private String toVersion;

    @Schema(description = "Lista de arquivos alterados no Pull Request")
    @NotEmpty(message = "files não pode ser vazio")
    @Valid
    private List<PullRequestRequest.FileChange> files;

    public RiskComparisonRequest() {
    }

    public RiskComparisonRequest(String pullRequestId, String fromVersion, String toVersion, List<PullRequestRequest.FileChange> files) {
        this.pullRequestId = pullRequestId;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.files = files;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public String getFromVersion() {
        return fromVersion;
    }

    public void setFromVersion(String fromVersion) {
        this.fromVersion = fromVersion;
    }

    public String getToVersion() {
        return toVersion;
    }

    public void setToVersion(String toVersion) {
        this.toVersion = toVersion;
    }

    public List<PullRequestRequest.FileChange> getFiles() {
        return files;
    }

    public void setFiles(List<PullRequestRequest.FileChange> files) {
        this.files = files;
    }
}
