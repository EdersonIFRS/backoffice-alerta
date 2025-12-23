package com.backoffice.alerta.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Requisição para análise inteligente de mudança")
public class AiAnalysisRequest {

    @Schema(description = "Identificador único do Pull Request", example = "PR-12345")
    @NotBlank(message = "pullRequestId é obrigatório")
    private String pullRequestId;

    @Schema(description = "Lista de arquivos alterados")
    @NotEmpty(message = "files não pode ser vazio")
    @Valid
    private List<FileChange> files;

    public AiAnalysisRequest() {
    }

    public AiAnalysisRequest(String pullRequestId, List<FileChange> files) {
        this.pullRequestId = pullRequestId;
        this.files = files;
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

    @Schema(description = "Informações sobre um arquivo alterado")
    public static class FileChange {

        @Schema(description = "Caminho completo do arquivo", example = "src/main/java/com/app/billing/PaymentService.java")
        @NotBlank(message = "filePath é obrigatório")
        private String filePath;

        @Schema(description = "Número de linhas alteradas", example = "120")
        @NotNull(message = "linesChanged é obrigatório")
        private Integer linesChanged;

        public FileChange() {
        }

        public FileChange(String filePath, Integer linesChanged) {
            this.filePath = filePath;
            this.linesChanged = linesChanged;
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
    }
}
