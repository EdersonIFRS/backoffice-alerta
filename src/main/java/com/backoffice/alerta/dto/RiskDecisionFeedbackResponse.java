package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response com feedback humano pós-deploy (read-only)
 */
@Schema(description = "Feedback humano sobre decisão de risco (imutável)")
public class RiskDecisionFeedbackResponse {

    @Schema(description = "ID único do feedback", example = "a1b2c3d4-e5f6-7890-abcd-1234567890ab")
    private UUID id;

    @Schema(description = "ID da auditoria referenciada", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID auditId;

    @Schema(description = "ID do Pull Request", example = "PR-458")
    private String pullRequestId;

    @Schema(description = "Decisão final que foi tomada", example = "APROVADO_COM_RESTRICOES")
    private FinalDecision finalDecision;

    @Schema(description = "Nível de risco que foi calculado", example = "ALTO")
    private RiskLevel riskLevel;

    @Schema(description = "Resultado real do deploy", example = "SUCCESS")
    private FeedbackOutcome outcome;

    @Schema(description = "Comentários do autor", 
            example = "Deploy ocorreu sem problemas. Métricas dentro do esperado.")
    private String comments;

    @Schema(description = "Autor do feedback", example = "john.doe@company.com")
    private String author;

    @Schema(description = "Data e hora da criação do feedback", example = "2025-12-17T20:30:00Z")
    private Instant createdAt;

    public RiskDecisionFeedbackResponse() {
    }

    /**
     * Construtor a partir da entidade RiskDecisionFeedback
     */
    public RiskDecisionFeedbackResponse(RiskDecisionFeedback feedback) {
        this.id = feedback.getId();
        this.auditId = feedback.getAuditId();
        this.pullRequestId = feedback.getPullRequestId();
        this.finalDecision = feedback.getFinalDecision();
        this.riskLevel = feedback.getRiskLevel();
        this.outcome = feedback.getOutcome();
        this.comments = feedback.getComments();
        this.author = feedback.getAuthor();
        this.createdAt = feedback.getCreatedAt();
    }

    // Apenas getters - sem setters (read-only)

    public UUID getId() {
        return id;
    }

    public UUID getAuditId() {
        return auditId;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public FinalDecision getFinalDecision() {
        return finalDecision;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public FeedbackOutcome getOutcome() {
        return outcome;
    }

    public String getComments() {
        return comments;
    }

    public String getAuthor() {
        return author;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
