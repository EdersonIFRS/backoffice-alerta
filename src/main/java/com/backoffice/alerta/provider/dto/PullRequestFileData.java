package com.backoffice.alerta.provider.dto;

import java.util.List;

/**
 * DTO externo representando um arquivo alterado em um Pull Request.
 * Simula formato de APIs como GitHub, GitLab, Bitbucket.
 */
public class PullRequestFileData {

    private String filePath;
    private Integer additions;
    private Integer deletions;
    private Boolean hasTest;

    public PullRequestFileData() {
    }

    public PullRequestFileData(String filePath, Integer additions, Integer deletions, Boolean hasTest) {
        this.filePath = filePath;
        this.additions = additions;
        this.deletions = deletions;
        this.hasTest = hasTest;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getAdditions() {
        return additions;
    }

    public void setAdditions(Integer additions) {
        this.additions = additions;
    }

    public Integer getDeletions() {
        return deletions;
    }

    public void setDeletions(Integer deletions) {
        this.deletions = deletions;
    }

    public Boolean getHasTest() {
        return hasTest;
    }

    public void setHasTest(Boolean hasTest) {
        this.hasTest = hasTest;
    }
}
