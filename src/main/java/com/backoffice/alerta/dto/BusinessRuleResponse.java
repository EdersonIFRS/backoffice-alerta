package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Domain;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response de Regra de Negócio
 */
@Schema(description = "Resposta com dados de uma regra de negócio")
public class BusinessRuleResponse {

    @Schema(description = "ID único da regra", example = "BR-001")
    private String id;

    @Schema(description = "Nome da regra", example = "Validação de Limite de Crédito")
    private String name;

    @Schema(description = "Domínio da regra", example = "PAYMENT")
    private Domain domain;

    @Schema(description = "Descrição detalhada da regra", 
            example = "Todo pagamento acima de R$ 10.000 deve passar por aprovação manual")
    private String description;

    @Schema(description = "Nível de criticidade da regra", example = "ALTA")
    private Criticality criticality;

    @Schema(description = "Área responsável pela regra", example = "Financeiro")
    private String owner;

    @Schema(description = "Data de criação da regra")
    private Instant createdAt;

    @Schema(description = "Data da última atualização da regra")
    private Instant updatedAt;

    public BusinessRuleResponse() {
    }

    public BusinessRuleResponse(String id, String name, Domain domain, String description, 
                               Criticality criticality, String owner, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.description = description;
        this.criticality = criticality;
        this.owner = owner;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
