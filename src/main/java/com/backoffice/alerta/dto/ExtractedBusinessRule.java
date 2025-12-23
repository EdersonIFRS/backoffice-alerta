package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Domain;

/**
 * US#68 - Regra de negócio extraída do repositório Git
 */
public class ExtractedBusinessRule {

    private String ruleId;
    private String name;
    private String description;
    private String content; // Conteúdo completo do arquivo (markdown, código, etc.)
    private Domain domain;
    private Criticality criticality;
    private String sourceFile;
    private String owner;

    // Constructors
    public ExtractedBusinessRule() {
    }

    public ExtractedBusinessRule(String ruleId, String name, String description, String content,
                                Domain domain, Criticality criticality, String sourceFile, String owner) {
        this.ruleId = ruleId;
        this.name = name;
        this.description = description;
        this.content = content;
        this.domain = domain;
        this.criticality = criticality;
        this.sourceFile = sourceFile;
        this.owner = owner;
    }

    // Getters and Setters
    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public void setCriticality(Criticality criticality) {
        this.criticality = criticality;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
