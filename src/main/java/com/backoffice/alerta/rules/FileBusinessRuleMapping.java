package com.backoffice.alerta.rules;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade que mapeia arquivos às regras de negócio que eles implementam
 */
public class FileBusinessRuleMapping {
    
    private String id;
    private String filePath;
    private String businessRuleId;
    private ImpactType impactType;
    private Instant createdAt;
    private Instant updatedAt;

    public FileBusinessRuleMapping() {
    }

    public FileBusinessRuleMapping(String filePath, String businessRuleId, ImpactType impactType) {
        this.id = UUID.randomUUID().toString();
        this.filePath = filePath;
        this.businessRuleId = businessRuleId;
        this.impactType = impactType;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
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
