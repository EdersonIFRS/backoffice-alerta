package com.backoffice.alerta.rag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Request para consulta RAG sobre regras de negócio
 * 
 * US#50 - Campo opcional projectId para filtrar por projeto
 */
public class RagQueryRequest {
    
    @NotBlank(message = "A pergunta não pode ser vazia")
    private String question;
    
    private ExplainFocus focus = ExplainFocus.BUSINESS;
    
    @Min(value = 1, message = "Mínimo de 1 fonte")
    @Max(value = 10, message = "Máximo de 10 fontes")
    private Integer maxSources = 5;
    
    @Schema(description = "ID do projeto para escopo (opcional)", example = "550e8400-e29b-41d4-a716-446655440010")
    private UUID projectId;
    
    public RagQueryRequest() {}
    
    public RagQueryRequest(String question) {
        this.question = question;
    }
    
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
        this.focus = focus != null ? focus : ExplainFocus.BUSINESS;
    }
    
    public Integer getMaxSources() {
        return maxSources;
    }
    
    public void setMaxSources(Integer maxSources) {
        this.maxSources = maxSources != null ? maxSources : 5;
    }
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
