package com.backoffice.alerta.sla;

import com.backoffice.alerta.rules.RiskLevel;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade imutável de rastreamento de SLA de risco
 * 
 * Representa um SLA (Service Level Agreement) para resposta organizacional
 * a decisões de risco críticas, com controle de vencimento e escalonamento.
 * 
 * ⚠️ IMUTÁVEL - não pode ser alterada após criação
 * ⚠️ NÃO envia notificações reais (apenas registra estados)
 * 
 * US#30 - Persistência com PostgreSQL/JPA
 */
@Entity
@Table(name = "risk_sla_tracking")
public final class RiskSlaTracking {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;
    
    @Column(name = "audit_id", nullable = false)
    private UUID auditId;
    
    @Column(name = "pull_request_id", nullable = false)
    private String pullRequestId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false)
    private EscalationLevel currentLevel;
    
    @Column(name = "sla_deadline", nullable = false)
    private Instant slaDeadline;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SlaStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "last_escalation_at")
    private Instant lastEscalationAt;

    /**
     * Construtor protegido para JPA
     */
    protected RiskSlaTracking() {
    }

    /**
     * Construtor completo - todos os campos são obrigatórios no momento da criação
     */
    public RiskSlaTracking(UUID notificationId,
                          UUID auditId,
                          String pullRequestId,
                          RiskLevel riskLevel,
                          EscalationLevel currentLevel,
                          Instant slaDeadline,
                          SlaStatus status,
                          Instant lastEscalationAt) {
        this.id = UUID.randomUUID();
        this.notificationId = notificationId;
        this.auditId = auditId;
        this.pullRequestId = pullRequestId;
        this.riskLevel = riskLevel;
        this.currentLevel = currentLevel;
        this.slaDeadline = slaDeadline;
        this.status = status;
        this.createdAt = Instant.now();
        this.lastEscalationAt = lastEscalationAt;
    }

    /**
     * Cria nova instância com status atualizado (imutabilidade)
     */
    public RiskSlaTracking withStatus(SlaStatus newStatus) {
        return new RiskSlaTracking(
            this.notificationId,
            this.auditId,
            this.pullRequestId,
            this.riskLevel,
            this.currentLevel,
            this.slaDeadline,
            newStatus,
            this.lastEscalationAt
        );
    }

    /**
     * Cria nova instância com nível de escalonamento atualizado (imutabilidade)
     */
    public RiskSlaTracking withEscalation(EscalationLevel newLevel, SlaStatus newStatus) {
        return new RiskSlaTracking(
            this.notificationId,
            this.auditId,
            this.pullRequestId,
            this.riskLevel,
            newLevel,
            this.slaDeadline,
            newStatus,
            Instant.now() // Atualiza timestamp de último escalonamento
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public UUID getAuditId() {
        return auditId;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public EscalationLevel getCurrentLevel() {
        return currentLevel;
    }

    public Instant getSlaDeadline() {
        return slaDeadline;
    }

    public SlaStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastEscalationAt() {
        return lastEscalationAt;
    }

    /**
     * Verifica se o SLA está vencido
     */
    public boolean isOverdue() {
        return Instant.now().isAfter(slaDeadline);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskSlaTracking that = (RiskSlaTracking) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
