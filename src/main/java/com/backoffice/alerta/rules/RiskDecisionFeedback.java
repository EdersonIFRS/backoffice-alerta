package com.backoffice.alerta.rules;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade imutável de feedback humano pós-deploy sobre decisão de risco
 * Registra o que realmente aconteceu após o deploy, para aprendizado organizacional
 * 
 * ⚠️ IMUTÁVEL - não pode ser alterada após criação
 * ⚠️ NÃO modifica a auditoria original (RiskDecisionAudit)
 * ⚠️ Relação 1:1 com auditoria - apenas um feedback por auditoria
 * 
 * US#30 - Persistência com PostgreSQL/JPA
 */
@Entity
@Table(name = "risk_decision_feedback")
public class RiskDecisionFeedback {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "audit_id", nullable = false, unique = true)
    private UUID auditId;
    
    @Column(name = "pull_request_id", nullable = false)
    private String pullRequestId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "final_decision", nullable = false)
    private FinalDecision finalDecision;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    private FeedbackOutcome outcome;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
    
    @Column(name = "author", nullable = false)
    private String author;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Construtor protegido para JPA
     */
    protected RiskDecisionFeedback() {
    }

    /**
     * Construtor completo - todos os campos obrigatórios
     * @param auditId ID da auditoria de decisão de risco
     * @param pullRequestId ID do Pull Request (extraído da auditoria)
     * @param finalDecision Decisão final (extraída da auditoria)
     * @param riskLevel Nível de risco (extraído da auditoria)
     * @param outcome Resultado real do deploy
     * @param comments Comentários do autor
     * @param author Nome/ID do autor do feedback
     */
    public RiskDecisionFeedback(UUID auditId,
                               String pullRequestId,
                               FinalDecision finalDecision,
                               RiskLevel riskLevel,
                               FeedbackOutcome outcome,
                               String comments,
                               String author) {
        this.id = UUID.randomUUID();
        this.auditId = auditId;
        this.pullRequestId = pullRequestId;
        this.finalDecision = finalDecision;
        this.riskLevel = riskLevel;
        this.outcome = outcome;
        this.comments = comments;
        this.author = author;
        this.createdAt = Instant.now();
    }

    // Apenas getters - sem setters para garantir imutabilidade

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskDecisionFeedback that = (RiskDecisionFeedback) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
