package com.backoffice.alerta.rules;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade que representa uma Regra de Negócio
 */
public class BusinessRule {
    
    private String id;
    private String name;
    private Domain domain;
    private String description;
    private String content; // Conteúdo completo do markdown/arquivo
    private String sourceFile; // Caminho do arquivo no repositório Git
    private Criticality criticality;
    private String owner;
    private UUID projectId;
    private Instant createdAt;
    private Instant updatedAt;

    public BusinessRule() {
    }

    public BusinessRule(String id, String name, Domain domain, String description, 
                       Criticality criticality, String owner) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.description = description;
        this.criticality = criticality;
        this.owner = owner;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public void setCriticality(Criticality criticality) {
        this.criticality = criticality;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
