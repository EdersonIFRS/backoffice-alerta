package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.ImpactType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request para criação de mapeamento entre arquivo e regra de negócio
 */
@Schema(description = "Requisição para mapear arquivo a uma regra de negócio")
public class FileBusinessRuleMappingRequest {

    @Schema(description = "Caminho completo do arquivo", 
            example = "src/main/java/com/app/payment/PaymentService.java", 
            required = true)
    private String filePath;

    @Schema(description = "ID da regra de negócio", example = "BR-001", required = true)
    private String businessRuleId;

    @Schema(description = "Tipo de impacto do arquivo na regra", example = "DIRECT", required = true)
    private ImpactType impactType;

    public FileBusinessRuleMappingRequest() {
    }

    public FileBusinessRuleMappingRequest(String filePath, String businessRuleId, ImpactType impactType) {
        this.filePath = filePath;
        this.businessRuleId = businessRuleId;
        this.impactType = impactType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getBusinessRuleId() {
        return businessRuleId;
    }

    public void setBusinessRuleId(String businessRuleId) {
        this.businessRuleId = businessRuleId;
    }

    public ImpactType getImpactType() {
        return impactType;
    }

    public void setImpactType(ImpactType impactType) {
        this.impactType = impactType;
    }
}
