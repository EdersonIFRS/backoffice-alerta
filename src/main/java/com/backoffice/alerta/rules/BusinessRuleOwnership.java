package com.backoffice.alerta.rules;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade imutável de ownership organizacional por regra de negócio
 * 
 * Define responsáveis (owners) por regras de negócio para governança,
 * rastreabilidade e futuras aprovações humanas.
 * 
 * ⚠️ IMUTÁVEL - não pode ser alterada após criação
 * 
 * Regras:
 * - Apenas 1 PRIMARY_OWNER por businessRuleId
 * - Múltiplos SECONDARY_OWNER e BACKUP permitidos
 */
public final class BusinessRuleOwnership {

    private final UUID id;
    private final UUID businessRuleId;
    private final String teamName;
    private final TeamType teamType;
    private final OwnershipRole role;
    private final String contactEmail;
    private final boolean approvalRequired;
    private final Instant createdAt;

    /**
     * Construtor completo - todos os campos são obrigatórios no momento da criação
     */
    public BusinessRuleOwnership(UUID businessRuleId,
                                String teamName,
                                TeamType teamType,
                                OwnershipRole role,
                                String contactEmail,
                                boolean approvalRequired) {
        this.id = UUID.randomUUID();
        this.businessRuleId = businessRuleId;
        this.teamName = teamName;
        this.teamType = teamType;
        this.role = role;
        this.contactEmail = contactEmail;
        this.approvalRequired = approvalRequired;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
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

    public OwnershipRole getRole() {
        return role;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
