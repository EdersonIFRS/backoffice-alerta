package com.backoffice.alerta.rules;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa um incidente histórico de produção 
 * relacionado a uma regra de negócio
 * 
 * US#30 - Persistência com PostgreSQL/JPA
 */
@Entity
@Table(name = "business_rule_incident")
public class BusinessRuleIncident {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "business_rule_id", nullable = false)
    private UUID businessRuleId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private IncidentSeverity severity;
    
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Construtor protegido para JPA
     */
    protected BusinessRuleIncident() {
    }

    public BusinessRuleIncident(UUID businessRuleId, String title, String description,
                               IncidentSeverity severity, Instant occurredAt) {
        this.id = UUID.randomUUID();
        this.businessRuleId = businessRuleId;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.occurredAt = occurredAt;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getBusinessRuleId() {
        return businessRuleId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusinessRuleIncident that = (BusinessRuleIncident) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
