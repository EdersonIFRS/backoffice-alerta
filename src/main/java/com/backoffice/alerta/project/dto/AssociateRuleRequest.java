package com.backoffice.alerta.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * US#49 - Request para associar BusinessRule a Project
 */
@Schema(description = "Request para associar uma regra de negócio a um projeto")
public class AssociateRuleRequest {

    @NotBlank(message = "businessRuleId é obrigatório")
    @Schema(
        description = "ID da regra de negócio a ser associada",
        example = "550e8400-e29b-41d4-a716-446655440001",
        required = true
    )
    private String businessRuleId;

    public AssociateRuleRequest() {
    }

    public AssociateRuleRequest(String businessRuleId) {
        this.businessRuleId = businessRuleId;
    }

    public String getBusinessRuleId() {
        return businessRuleId;
    }

    public void setBusinessRuleId(String businessRuleId) {
        this.businessRuleId = businessRuleId;
    }
}
