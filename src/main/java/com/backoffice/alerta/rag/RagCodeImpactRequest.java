package com.backoffice.alerta.rag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO para consulta RAG de impacto no código
 * 
 * US#45 - RAG com Mapeamento de Código Impactado
 * US#50 - Campo opcional projectId para filtrar por projeto
 * 
 * Permite perguntar "onde mexer no código" e "o que pode quebrar"
 */
@Schema(description = "Consulta RAG sobre impacto de regras no código")
public class RagCodeImpactRequest {
    
    @NotBlank(message = "Pergunta não pode ser vazia")
    @Schema(
        description = "Pergunta em linguagem natural sobre impacto no código",
        example = "Onde alterar o cálculo de horas para PJ?"
    )
    private String question;
    
    @Schema(description = "ID do projeto para filtrar análise (opcional)", example = "550e8400-e29b-41d4-a716-446655440010")
    private UUID projectId;
    
    @NotNull(message = "Foco da explicação é obrigatório")
    @Schema(
        description = "Foco da explicação: BUSINESS (negócio), TECHNICAL (técnico) ou EXECUTIVE (executivo)",
        example = "TECHNICAL"
    )
    private ExplainFocus focus;
    
    @Min(value = 1, message = "Máximo de arquivos deve ser pelo menos 1")
    @Max(value = 20, message = "Máximo de arquivos não pode exceder 20")
    @Schema(
        description = "Número máximo de arquivos a retornar",
        example = "10",
        defaultValue = "10"
    )
    private int maxFiles = 10;
    
    // Constructors
    public RagCodeImpactRequest() {}
    
    public RagCodeImpactRequest(String question, ExplainFocus focus, int maxFiles) {
        this.question = question;
        this.focus = focus;
        this.maxFiles = maxFiles;
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
    
    public int getMaxFiles() {
        return maxFiles;
    }
    
    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
