package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Dados do Pull Request para análise de risco")
public class PullRequestRequest {

    @Schema(description = "Identificador único do Pull Request", example = "PR-12345")
    @NotBlank(message = "pullRequestId é obrigatório")
    private String pullRequestId;

    @Schema(description = "Lista de arquivos alterados no Pull Request")
    @NotEmpty(message = "files não pode ser vazio")
    private List<@Valid FileChange> files;

    @Schema(
        description = "Versão das regras de análise de risco (v1 ou v2). Se não informado, usa v1",
        example = "v1",
        allowableValues = {"v1", "v2"}
    )
    private String ruleVersion;

    public PullRequestRequest() {
    }

    public PullRequestRequest(String pullRequestId, List<FileChange> files) {
        this.pullRequestId = pullRequestId;
        this.files = files;
    }

    public PullRequestRequest(String pullRequestId, List<FileChange> files, String ruleVersion) {
        this.pullRequestId = pullRequestId;
        this.files = files;
        this.ruleVersion = ruleVersion;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public List<FileChange> getFiles() {
        return files;
    }

    public void setFiles(List<FileChange> files) {
        this.files = files;
    }

    public String getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(String ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    @Schema(description = "Informações sobre um arquivo alterado")
    public static class FileChange {

        @Schema(
            description = "Caminho completo do arquivo. A criticidade é inferida automaticamente se o caminho contiver: billing, payment, pricing ou order",
            example = "src/main/java/com/app/billing/PaymentService.java"
        )
        @NotBlank(message = "filePath é obrigatório")
        private String filePath;

        @Schema(
            description = "Número de linhas alteradas no arquivo (>100: +20 pontos, 50-100: +10 pontos)",
            example = "120"
        )
        @NotNull(message = "linesChanged é obrigatório")
        private Integer linesChanged;

        @Schema(
            description = "Indica se o arquivo possui testes (false ou null: +20 pontos)",
            example = "false"
        )
        private Boolean hasTest;

        public FileChange() {
        }

        public FileChange(String filePath, Integer linesChanged, Boolean hasTest) {
            this.filePath = filePath;
            this.linesChanged = linesChanged;
            this.hasTest = hasTest;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public Integer getLinesChanged() {
            return linesChanged;
        }

        public void setLinesChanged(Integer linesChanged) {
            this.linesChanged = linesChanged;
        }

        public Boolean getHasTest() {
            return hasTest;
        }

        public void setHasTest(Boolean hasTest) {
            this.hasTest = hasTest;
        }
    }
}
