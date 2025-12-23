package com.backoffice.alerta.code;

import java.time.Instant;
import java.util.UUID;

/**
 * FASE 1: Mapeia relacionamento entre código e regras de negócio
 */
public class CodeBusinessRuleMapping {
    
    private String filePath;
    private String businessRuleId;
    private UUID projectId;
    private float confidence;
    private RelationshipType relationshipType;
    private DetectionMethod detectedBy;
    private Instant detectedAt;

    public CodeBusinessRuleMapping() {
    }

    public CodeBusinessRuleMapping(String filePath, String businessRuleId, UUID projectId,
                                  RelationshipType relationshipType, DetectionMethod detectedBy) {
        this.filePath = filePath;
        this.businessRuleId = businessRuleId;
        this.projectId = projectId;
        this.confidence = 1.0f;
        this.relationshipType = relationshipType;
        this.detectedBy = detectedBy;
        this.detectedAt = Instant.now();
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

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public DetectionMethod getDetectedBy() {
        return detectedBy;
    }

    public void setDetectedBy(DetectionMethod detectedBy) {
        this.detectedBy = detectedBy;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }

    public enum RelationshipType {
        IMPLEMENTS,   // Arquivo implementa a regra
        VALIDATES,    // Arquivo valida a regra
        REFERENCES    // Arquivo referencia a regra
    }

    public enum DetectionMethod {
        COMMENT,   // Detectado via @BusinessRule no código
        SEMANTIC,  // Detectado via análise semântica RAG
        IMPORT,    // Detectado via análise de imports
        MANUAL     // Adicionado manualmente
    }
}
