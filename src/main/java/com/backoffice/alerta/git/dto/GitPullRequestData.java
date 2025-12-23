package com.backoffice.alerta.git.dto;

import com.backoffice.alerta.git.PullRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * US#51 - Dados do Pull Request (metadados apenas)
 * 
 * Read-Only: sem código-fonte, apenas metadados
 */
@Schema(description = "Metadados do Pull Request")
public class GitPullRequestData {

    @Schema(description = "ID do Pull Request", example = "PR-2024-123")
    private String pullRequestId;

    @Schema(description = "Título do Pull Request", example = "feat: Adicionar validação de CPF no pagamento PJ")
    private String title;

    @Schema(description = "Autor do Pull Request", example = "developer@company.com")
    private String author;

    @Schema(description = "Branch de origem", example = "feature/cpf-validation")
    private String sourceBranch;

    @Schema(description = "Branch de destino", example = "main")
    private String targetBranch;

    @Schema(description = "Status do Pull Request", example = "OPEN")
    private PullRequestStatus status;

    @Schema(description = "Arquivos alterados no Pull Request")
    private List<GitPullRequestFile> changedFiles = new ArrayList<>();

    public GitPullRequestData() {}

    public GitPullRequestData(String pullRequestId, String title, String author, 
                             String sourceBranch, String targetBranch, PullRequestStatus status) {
        this.pullRequestId = pullRequestId;
        this.title = title;
        this.author = author;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
        this.status = status;
    }

    // Getters e Setters
    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public PullRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PullRequestStatus status) {
        this.status = status;
    }

    public List<GitPullRequestFile> getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(List<GitPullRequestFile> changedFiles) {
        this.changedFiles = changedFiles;
    }
}
