package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Política de aprovação configurável
 */
@Schema(description = "Política de aprovação de mudanças")
public class ApprovalPolicy {

    @Schema(description = "Nível máximo de risco permitido", example = "MEDIO")
    private RiskLevel maxAllowedRisk;

    @Schema(description = "Permitir aprovação condicional", example = "true")
    private boolean allowConditionalApproval;

    public ApprovalPolicy() {
    }

    public ApprovalPolicy(RiskLevel maxAllowedRisk, boolean allowConditionalApproval) {
        this.maxAllowedRisk = maxAllowedRisk;
        this.allowConditionalApproval = allowConditionalApproval;
    }

    public RiskLevel getMaxAllowedRisk() {
        return maxAllowedRisk;
    }

    public void setMaxAllowedRisk(RiskLevel maxAllowedRisk) {
        this.maxAllowedRisk = maxAllowedRisk;
    }

    public boolean isAllowConditionalApproval() {
        return allowConditionalApproval;
    }

    public void setAllowConditionalApproval(boolean allowConditionalApproval) {
        this.allowConditionalApproval = allowConditionalApproval;
    }
}
