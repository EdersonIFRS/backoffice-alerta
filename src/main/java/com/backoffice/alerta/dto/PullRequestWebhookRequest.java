package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.ChangeType;
import com.backoffice.alerta.rules.Environment;
import com.backoffice.alerta.rules.PullRequestEventType;
import com.backoffice.alerta.rules.WebhookProvider;

import java.util.List;

/**
 * Request recebido via webhook de Pull Request (GitHub ou GitLab)
 * 
 * Contém todas as informações necessárias para análise de risco
 * do Pull Request através da orquestração de serviços existentes
 */
public class PullRequestWebhookRequest {
    
    private WebhookProvider provider;
    private PullRequestEventType eventType;
    private String projectId;
    private String repository;
    private PullRequestInfo pullRequest;
    private Environment environment;
    private ChangeType changeType;
    private List<String> changedFiles;

    // Construtor vazio para desserialização JSON
    public PullRequestWebhookRequest() {
    }

    public PullRequestWebhookRequest(WebhookProvider provider,
                                    PullRequestEventType eventType,
                                    String projectId,
                                    String repository,
                                    PullRequestInfo pullRequest,
                                    Environment environment,
                                    ChangeType changeType,
                                    List<String> changedFiles) {
        this.provider = provider;
        this.eventType = eventType;
        this.projectId = projectId;
        this.repository = repository;
        this.pullRequest = pullRequest;
        this.environment = environment;
        this.changeType = changeType;
        this.changedFiles = changedFiles;
    }

    public WebhookProvider getProvider() {
        return provider;
    }

    public void setProvider(WebhookProvider provider) {
        this.provider = provider;
    }

    public PullRequestEventType getEventType() {
        return eventType;
    }

    public void setEventType(PullRequestEventType eventType) {
        this.eventType = eventType;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public PullRequestInfo getPullRequest() {
        return pullRequest;
    }

    public void setPullRequest(PullRequestInfo pullRequest) {
        this.pullRequest = pullRequest;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(List<String> changedFiles) {
        this.changedFiles = changedFiles;
    }
}
