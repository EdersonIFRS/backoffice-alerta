package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.ChangeType;
import com.backoffice.alerta.rules.Environment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

/**
 * Request para comparação histórica de decisões de risco
 * 
 * US#41 - Comparação Histórica de Decisões de Risco
 * 
 * IMPORTANTE: Endpoint READ-ONLY - não altera estado do sistema
 */
public class DecisionHistoricalComparisonRequest {
    
    @NotBlank(message = "Pull request ID é obrigatório")
    private String currentPullRequestId;
    
    private Environment environment;
    
    private ChangeType changeType;
    
    @NotEmpty(message = "Lista de arquivos alterados não pode ser vazia")
    private List<String> changedFiles;
    
    @Positive(message = "lookbackDays deve ser maior que 0")
    private int lookbackDays = 180; // default
    
    @Min(value = 1, message = "maxComparisons deve ser no mínimo 1")
    @Max(value = 10, message = "maxComparisons deve ser no máximo 10")
    private int maxComparisons = 5; // default
    
    @Schema(description = "ID do projeto para escopo (opcional)", example = "550e8400-e29b-41d4-a716-446655440010")
    private UUID projectId;
    
    public DecisionHistoricalComparisonRequest() {
    }
    
    public DecisionHistoricalComparisonRequest(String currentPullRequestId,
                                              List<String> changedFiles) {
        this.currentPullRequestId = currentPullRequestId;
        this.changedFiles = changedFiles;
    }
    
    public String getCurrentPullRequestId() {
        return currentPullRequestId;
    }
    
    public void setCurrentPullRequestId(String currentPullRequestId) {
        this.currentPullRequestId = currentPullRequestId;
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
    
    public int getLookbackDays() {
        return lookbackDays;
    }
    
    public void setLookbackDays(int lookbackDays) {
        this.lookbackDays = lookbackDays;
    }
    
    public int getMaxComparisons() {
        return maxComparisons;
    }
    
    public void setMaxComparisons(int maxComparisons) {
        this.maxComparisons = maxComparisons;
    }
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
