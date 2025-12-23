package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Domain;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request para criação de Regra de Negócio
 */
@Schema(description = "Requisição para criar uma regra de negócio")
public class BusinessRuleRequest {

    @Schema(description = "ID único da regra", example = "BR-001", required = true)
    private String id;

    @Schema(description = "Nome da regra", example = "Validação de Limite de Crédito", required = true)
    private String name;

    @Schema(description = "Domínio da regra", example = "PAYMENT", required = true)
    private Domain domain;

    @Schema(description = "Descrição detalhada da regra", 
            example = "Todo pagamento acima de R$ 10.000 deve passar por aprovação manual", 
            required = true)
    private String description;

    @Schema(description = "Nível de criticidade da regra", example = "ALTA", required = true)
    private Criticality criticality;

    @Schema(description = "Área responsável pela regra", example = "Financeiro", required = true)
    private String owner;

    public BusinessRuleRequest() {
    }

    public BusinessRuleRequest(String id, String name, Domain domain, String description, 
                              Criticality criticality, String owner) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.description = description;
        this.criticality = criticality;
        this.owner = owner;
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
}
