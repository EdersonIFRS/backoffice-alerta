package com.backoffice.alerta.notification;

import com.backoffice.alerta.rules.OwnershipRole;
import com.backoffice.alerta.rules.TeamType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade imutável de notificação organizacional
 * 
 * Representa um evento de notificação gerado automaticamente após
 * decisões de risco que requerem atenção dos times responsáveis.
 * 
 * ⚠️ IMUTÁVEL - não pode ser alterada após criação
 * ⚠️ NÃO envia mensagens reais (apenas registra evento)
 * 
 * US#30 - Persistência com PostgreSQL/JPA
 */
@Entity
@Table(name = "risk_notification")
public final class RiskNotification {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "audit_id", nullable = false)
    private UUID auditId;
    
    @Column(name = "pull_request_id", nullable = false)
    private String pullRequestId;
    
    @Column(name = "business_rule_id", nullable = false)
    private UUID businessRuleId;
    
    @Column(name = "team_name", nullable = false)
    private String teamName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "team_type", nullable = false)
    private TeamType teamType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_role", nullable = false)
    private OwnershipRole ownershipRole;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_trigger", nullable = false)
    private NotificationTrigger notificationTrigger;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private NotificationSeverity severity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Construtor protegido para JPA
     */
    protected RiskNotification() {
    }

    /**
     * Construtor completo - todos os campos são obrigatórios no momento da criação
     */
    public RiskNotification(UUID auditId,
                           String pullRequestId,
                           UUID businessRuleId,
                           String teamName,
                           TeamType teamType,
                           OwnershipRole ownershipRole,
                           NotificationTrigger notificationTrigger,
                           NotificationSeverity severity,
                           NotificationChannel channel,
                           String message) {
        this.id = UUID.randomUUID();
        this.auditId = auditId;
        this.pullRequestId = pullRequestId;
        this.businessRuleId = businessRuleId;
        this.teamName = teamName;
        this.teamType = teamType;
        this.ownershipRole = ownershipRole;
        this.notificationTrigger = notificationTrigger;
        this.severity = severity;
        this.channel = channel;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuditId() {
        return auditId;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public UUID getBusinessRuleId() {
        return businessRuleId;
    }

    public String getTeamName() {
        return teamName;
    }

    public TeamType getTeamType() {
        return teamType;
    }

    public OwnershipRole getOwnershipRole() {
        return ownershipRole;
    }

    public NotificationTrigger getNotificationTrigger() {
        return notificationTrigger;
    }

    public NotificationSeverity getSeverity() {
        return severity;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskNotification that = (RiskNotification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
