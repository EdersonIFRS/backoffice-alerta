package com.backoffice.alerta.project.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * US#49 - Entidade que associa BusinessRules a Projects.
 * 
 * Entidade imutável que representa a associação many-to-many entre
 * projetos organizacionais e regras de negócio.
 * 
 * Regras:
 * - Imutável (sem setters)
 * - Sem update físico
 * - Unique constraint (projectId, businessRuleId)
 * - Auditoria via createdBy
 */
@Entity
@Table(
    name = "project_business_rules",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_project_business_rule",
        columnNames = {"project_id", "business_rule_id"}
    ),
    indexes = {
        @Index(name = "idx_pbr_project_id", columnList = "project_id"),
        @Index(name = "idx_pbr_business_rule_id", columnList = "business_rule_id"),
        @Index(name = "idx_pbr_created_at", columnList = "created_at")
    }
)
public final class ProjectBusinessRule {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private final UUID id;

    @Column(name = "project_id", nullable = false, updatable = false)
    private final UUID projectId;

    @Column(name = "business_rule_id", nullable = false, updatable = false, length = 255)
    private final String businessRuleId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private final Instant createdAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 255)
    private final String createdBy;

    /**
     * Construtor JPA (protected para uso exclusivo do Hibernate)
     */
    protected ProjectBusinessRule() {
        this.id = null;
        this.projectId = null;
        this.businessRuleId = null;
        this.createdAt = null;
        this.createdBy = null;
    }

    /**
     * Construtor público para criação de novas associações
     * 
     * @param projectId ID do projeto
     * @param businessRuleId ID da regra de negócio
     * @param createdBy Usuário que criou a associação
     */
    public ProjectBusinessRule(UUID projectId, String businessRuleId, String createdBy) {
        this.id = UUID.randomUUID();
        this.projectId = Objects.requireNonNull(projectId, "projectId cannot be null");
        this.businessRuleId = Objects.requireNonNull(businessRuleId, "businessRuleId cannot be null");
        this.createdAt = Instant.now();
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy cannot be null");
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getBusinessRuleId() {
        return businessRuleId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    // Equals & HashCode (baseado em projectId + businessRuleId)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectBusinessRule that = (ProjectBusinessRule) o;
        return Objects.equals(projectId, that.projectId) &&
               Objects.equals(businessRuleId, that.businessRuleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, businessRuleId);
    }

    @Override
    public String toString() {
        return "ProjectBusinessRule{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", businessRuleId=" + businessRuleId +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
}
