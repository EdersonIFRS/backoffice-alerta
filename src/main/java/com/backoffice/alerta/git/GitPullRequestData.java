package com.backoffice.alerta.git;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * US#51 - Metadados de Pull Request
 * 
 * Dados extraídos do PR para análise.
 * Apenas metadados, sem código fonte.
 */
@Schema(description = "Metadados do Pull Request")
public class GitPullRequestData {
    
    @Schema(description = "ID único do PR", example = "123")
    private String pullRequestId;
    
    @Schema(description = "Título do PR", example = "feat: adicionar validação de CPF em pagamentos PJ")
    private String title;
    
    @Schema(description = "Autor do PR", example = "joao.silva")
    private String author;
    
    @Schema(description = "Branch de origem", example = "feature/cpf-validation")
    private String sourceBranch;
    
    @Schema(description = "Branch de destino", example = "main")
    private String targetBranch;
    
    @Schema(description = "Status do PR", example = "OPEN")
    private PullRequestStatus status;
    
    @Schema(description = "Lista de arquivos alterados")
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
