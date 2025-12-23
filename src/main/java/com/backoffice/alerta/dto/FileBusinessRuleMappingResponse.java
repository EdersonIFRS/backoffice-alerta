package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.ImpactType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response de mapeamento entre arquivo e regra de negócio
 */
@Schema(description = "Resposta com dados do mapeamento arquivo-regra")
public class FileBusinessRuleMappingResponse {

    @Schema(description = "ID único do mapeamento")
    private String id;

    @Schema(description = "Caminho completo do arquivo", 
            example = "src/main/java/com/app/payment/PaymentService.java")
    private String filePath;

    @Schema(description = "ID da regra de negócio", example = "BR-001")
    private String businessRuleId;

    @Schema(description = "Tipo de impacto do arquivo na regra", example = "DIRECT")
    private ImpactType impactType;

    @Schema(description = "Data de criação do mapeamento")
    private Instant createdAt;

    @Schema(description = "Data da última atualização do mapeamento")
    private Instant updatedAt;

    public FileBusinessRuleMappingResponse() {
    }

    public FileBusinessRuleMappingResponse(String id, String filePath, String businessRuleId, 
                                          ImpactType impactType, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.filePath = filePath;
        this.businessRuleId = businessRuleId;
        this.impactType = impactType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
