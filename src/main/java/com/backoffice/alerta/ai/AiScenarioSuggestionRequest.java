package com.backoffice.alerta.ai;

import com.backoffice.alerta.rules.ChangeType;
import com.backoffice.alerta.rules.Environment;

/**
 * Request para sugestão automática de cenários de decisão via IA
 * 
 * US#34 - IA sugere automaticamente cenários ótimos de decisão
 */
public class AiScenarioSuggestionRequest {
    
    private String pullRequestId;
    private Environment environment;
    private ChangeType changeType;
    private Integer maxScenarios = 3;

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
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

    public Integer getMaxScenarios() {
        return maxScenarios;
    }

    public void setMaxScenarios(Integer maxScenarios) {
        this.maxScenarios = maxScenarios;
    }
}
