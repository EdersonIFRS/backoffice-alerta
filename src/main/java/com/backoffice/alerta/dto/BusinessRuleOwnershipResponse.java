package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.BusinessRuleOwnership;
import com.backoffice.alerta.rules.OwnershipRole;
import com.backoffice.alerta.rules.TeamType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response contendo informações de ownership organizacional de uma regra
 */
@Schema(description = "Informações de ownership organizacional de uma regra de negócio")
public class BusinessRuleOwnershipResponse {

    @Schema(description = "ID único do ownership", example = "123e4567-e89b-12d3-a456-426614174000")
    private final UUID id;

    @Schema(description = "ID da regra de negócio", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID businessRuleId;

    @Schema(description = "Nome do time/equipe responsável", example = "Time de Pagamentos")
    private final String teamName;

    @Schema(description = "Tipo organizacional do time", example = "ENGINEERING")
    private final TeamType teamType;

    @Schema(description = "Papel do time no ownership da regra", example = "PRIMARY_OWNER")
    private final OwnershipRole role;

    @Schema(description = "Email de contato do time", example = "payments-team@company.com")
    private final String contactEmail;

    @Schema(description = "Se requer aprovação manual do time para mudanças", example = "true")
    private final boolean approvalRequired;

    @Schema(description = "Data/hora de criação do ownership", example = "2024-03-15T14:30:00Z")
    private final Instant createdAt;

    /**
     * Construtor a partir da entidade
     */
    public BusinessRuleOwnershipResponse(BusinessRuleOwnership ownership) {
        this.id = ownership.getId();
        this.businessRuleId = ownership.getBusinessRuleId();
        this.teamName = ownership.getTeamName();
        this.teamType = ownership.getTeamType();
        this.role = ownership.getRole();
        this.contactEmail = ownership.getContactEmail();
        this.approvalRequired = ownership.isApprovalRequired();
        this.createdAt = ownership.getCreatedAt();
    }

    // Getters
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
