package com.backoffice.alerta.chat;

import com.backoffice.alerta.rag.ExplainFocus;
import com.backoffice.alerta.rules.Environment;
import com.backoffice.alerta.rules.ChangeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Request para consulta no chat unificado de análise de impacto
 * 
 * US#46 - Chat Unificado de Análise de Impacto (Engenharia + Negócio)
 * US#50 - Campo opcional projectId para filtrar por projeto
 */
@Schema(description = "Consulta no chat unificado de análise de impacto")
public class ChatQueryRequest {
    
    @NotBlank(message = "A pergunta não pode ser vazia")
    @Schema(
        description = "Pergunta em linguagem natural sobre impacto de mudanças",
        example = "Onde alterar o cálculo de horas para Pessoa Jurídica e quem preciso avisar?"
    )
    private String question;
    
    @Schema(description = "ID do projeto para filtrar análise (opcional)", example = "550e8400-e29b-41d4-a716-446655440010")
    private UUID projectId;
    
    @Schema(
        description = "Foco da explicação (opcional, será inferido se omitido)",
        example = "TECHNICAL"
    )
    private ExplainFocus focus;
    
    @Schema(
        description = "ID do Pull Request para contexto (opcional)",
        example = "PR-2024-001"
    )
    private String pullRequestId;
    
    @Schema(
        description = "Ambiente para análise (opcional)",
        example = "PRODUCTION"
    )
    private Environment environment;
    
    @Schema(
        description = "Tipo de mudança para contexto (opcional)",
        example = "HOTFIX"
    )
    private ChangeType changeType;
    
    public ChatQueryRequest() {}
    
    public ChatQueryRequest(String question) {
        this.question = question;
    }
    
    // Getters and Setters
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public ExplainFocus getFocus() {
        return focus;
    }
    
    public void setFocus(ExplainFocus focus) {
        this.focus = focus;
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
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
