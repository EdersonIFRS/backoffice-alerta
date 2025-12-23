package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Domain;

/**
 * Response para busca de regras de negócio
 * 
 * US#35 - Consulta Inteligente e Explicação de Regras de Negócio
 */
public class BusinessRuleSearchResponse {
    
    private String id;
    private String name;
    private Domain domain;
    private Criticality criticality;
    private String shortDescription;

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

    public Criticality getCriticality() {
        return criticality;
    }

    public void setCriticality(Criticality criticality) {
        this.criticality = criticality;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
}
