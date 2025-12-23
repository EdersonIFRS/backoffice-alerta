package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.OwnershipRole;
import com.backoffice.alerta.rules.TeamType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Request para criar ownership organizacional de uma regra de negócio
 */
@Schema(description = "Request para atribuir ownership organizacional a uma regra de negócio")
public class BusinessRuleOwnershipRequest {

    @Schema(description = "ID da regra de negócio", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private UUID businessRuleId;

    @Schema(description = "Nome do time/equipe responsável", example = "Time de Pagamentos", required = true)
    private String teamName;

    @Schema(description = "Tipo organizacional do time", example = "ENGINEERING", required = true)
    private TeamType teamType;

    @Schema(description = "Papel do time no ownership da regra", example = "PRIMARY_OWNER", required = true)
    private OwnershipRole role;

    @Schema(description = "Email de contato do time", example = "payments-team@company.com", required = true)
    private String contactEmail;

    @Schema(description = "Se requer aprovação manual do time para mudanças", example = "true", required = true)
    private boolean approvalRequired;

    // Construtores
    public BusinessRuleOwnershipRequest() {
    }

    public BusinessRuleOwnershipRequest(UUID businessRuleId,
                                       String teamName,
                                       TeamType teamType,
                                       OwnershipRole role,
                                       String contactEmail,
                                       boolean approvalRequired) {
        this.businessRuleId = businessRuleId;
        this.teamName = teamName;
        this.teamType = teamType;
        this.role = role;
        this.contactEmail = contactEmail;
        this.approvalRequired = approvalRequired;
    }

    // Getters e Setters
    public UUID getBusinessRuleId() {
        return businessRuleId;
    }

    public void setBusinessRuleId(UUID businessRuleId) {
        this.businessRuleId = businessRuleId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public TeamType getTeamType() {
        return teamType;
    }

    public void setTeamType(TeamType teamType) {
        this.teamType = teamType;
    }

    public OwnershipRole getRole() {
        return role;
    }

    public void setRole(OwnershipRole role) {
        this.role = role;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }
}
