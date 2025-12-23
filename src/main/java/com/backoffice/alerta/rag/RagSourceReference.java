package com.backoffice.alerta.rag;

import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Domain;

/**
 * Referência de fonte utilizada na resposta RAG
 */
public class RagSourceReference {
    
    private String type; // "BUSINESS_RULE", "INCIDENT", "OWNERSHIP", "DEPENDENCY"
    private String id;
    private String title;
    private Domain domain;
    private Criticality criticality;
    private String summary;
    
    public RagSourceReference() {}
    
    public RagSourceReference(String type, String id, String title, Domain domain, Criticality criticality) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.domain = domain;
        this.criticality = criticality;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
}
