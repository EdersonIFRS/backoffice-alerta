package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.FeedbackOutcome;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Request para criação de feedback humano pós-deploy
 */
@Schema(description = "Requisição para registrar feedback humano sobre decisão de risco")
public class RiskDecisionFeedbackRequest {

    @Schema(description = "ID da auditoria de decisão de risco", 
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true)
    private UUID auditId;

    @Schema(description = "Resultado real do deploy", 
            example = "SUCCESS",
            required = true)
    private FeedbackOutcome outcome;

    @Schema(description = "Comentários sobre o resultado do deploy",
            example = "Deploy ocorreu sem problemas. Métricas dentro do esperado.",
            required = true)
    private String comments;

    @Schema(description = "Nome ou ID do autor do feedback",
            example = "john.doe@company.com",
            required = true)
    private String author;

    public RiskDecisionFeedbackRequest() {
    }

    public RiskDecisionFeedbackRequest(UUID auditId, FeedbackOutcome outcome, 
                                      String comments, String author) {
        this.auditId = auditId;
        this.outcome = outcome;
        this.comments = comments;
        this.author = author;
    }

    public UUID getAuditId() {
        return auditId;
    }

    public void setAuditId(UUID auditId) {
        this.auditId = auditId;
    }

    public FeedbackOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(FeedbackOutcome outcome) {
        this.outcome = outcome;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
