package com.backoffice.alerta.executive;

import com.backoffice.alerta.rules.ChangeType;
import com.backoffice.alerta.rules.Environment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request para geração de explicação executiva de impacto sistêmico
 * 
 * US#38 - Explicação Executiva Inteligente
 * 
 * IMPORTANTE: Endpoint consultivo - não altera estado do sistema
 */
public class ExecutiveImpactExplainRequest {
    
    @NotBlank(message = "Pull request ID é obrigatório")
    private String pullRequestId;
    
    @NotNull(message = "Ambiente é obrigatório")
    private Environment environment;
    
    @NotNull(message = "Tipo de mudança é obrigatório")
    private ChangeType changeType;
    
    private ExplainFocus focus = ExplainFocus.EXECUTIVE; // default
    
    @Schema(description = "ID do projeto para escopo (opcional)", example = "550e8400-e29b-41d4-a716-446655440010")
    private UUID projectId;
    
    public ExecutiveImpactExplainRequest() {
    }
    
    public ExecutiveImpactExplainRequest(String pullRequestId, 
                                         Environment environment, 
                                         ChangeType changeType, 
                                         ExplainFocus focus) {
        this.pullRequestId = pullRequestId;
        this.environment = environment;
        this.changeType = changeType;
        this.focus = focus != null ? focus : ExplainFocus.EXECUTIVE;
    }
    
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
    
    public ExplainFocus getFocus() {
        return focus;
    }
    
    public void setFocus(ExplainFocus focus) {
        this.focus = focus != null ? focus : ExplainFocus.EXECUTIVE;
    }
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
