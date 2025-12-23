package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * Request para análise de impacto de negócio em Pull Request
 */
@Schema(description = "Requisição para análise de impacto de negócio")
public class BusinessImpactRequest {

    @Schema(description = "ID do Pull Request", example = "PR-123", required = true)
    private String pullRequestId;

    @Schema(description = "Lista de arquivos alterados no Pull Request", 
            example = "[\"src/main/java/com/app/payment/PaymentService.java\", \"src/main/java/com/app/order/OrderController.java\"]",
            required = true)
    private List<String> changedFiles;
    
    @Schema(description = "ID do projeto para escopo (opcional)", example = "550e8400-e29b-41d4-a716-446655440010")
    private UUID projectId;

    public BusinessImpactRequest() {
    }

    public BusinessImpactRequest(String pullRequestId, List<String> changedFiles) {
        this.pullRequestId = pullRequestId;
        this.changedFiles = changedFiles;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(List<String> changedFiles) {
        this.changedFiles = changedFiles;
    }
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
