package com.backoffice.alerta.dto;

/**
 * Informações do Pull Request extraídas do webhook
 * 
 * Contém dados básicos necessários para identificação e rastreabilidade
 */
public class PullRequestInfo {
    
    private final String id;
    private final String title;
    private final String author;
    private final String targetBranch;
    private final String sourceBranch;

    public PullRequestInfo(String id, String title, String author, String targetBranch, String sourceBranch) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.targetBranch = targetBranch;
        this.sourceBranch = sourceBranch;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }
}
