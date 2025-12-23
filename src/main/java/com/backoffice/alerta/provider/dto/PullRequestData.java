package com.backoffice.alerta.provider.dto;

import java.util.List;

/**
 * DTO externo representando dados completos de um Pull Request.
 * Simula formato de APIs externas (GitHub, GitLab, Bitbucket, etc).
 * NÃO é usado internamente pelo core do sistema.
 */
public class PullRequestData {

    private String pullRequestId;
    private String repository;
    private String author;
    private List<PullRequestFileData> files;

    public PullRequestData() {
    }

    public PullRequestData(String pullRequestId, String repository, String author, 
                          List<PullRequestFileData> files) {
        this.pullRequestId = pullRequestId;
        this.repository = repository;
        this.author = author;
        this.files = files;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<PullRequestFileData> getFiles() {
        return files;
    }

    public void setFiles(List<PullRequestFileData> files) {
        this.files = files;
    }
}
