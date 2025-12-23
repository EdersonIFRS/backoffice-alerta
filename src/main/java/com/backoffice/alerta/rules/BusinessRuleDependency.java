package com.backoffice.alerta.rules;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa uma dependência entre duas regras de negócio
 * 
 * Entidade imutável que define relacionamentos direcionais entre regras,
 * permitindo análise de impacto cruzado e cascata.
 * 
 * Exemplo:
 * - sourceRuleId: "BR-PAYMENT-001" (regra de pagamento)
 * - targetRuleId: "BR-INVOICE-003" (regra de faturamento)
 * - type: FEEDS (pagamento alimenta faturamento)
 * 
 * US#36 - Análise de Impacto Cruzado (Cadeia de Regras Afetadas)
 */
public final class BusinessRuleDependency {
    
    private final UUID id;
    private final String sourceRuleId;
    private final String targetRuleId;
    private final BusinessRuleDependencyType dependencyType;
    private final String description;
    private final Instant createdAt;
    
    /**
     * Construtor completo para criar dependência entre regras
     * 
     * @param sourceRuleId ID da regra de origem (que causa impacto)
     * @param targetRuleId ID da regra de destino (que recebe impacto)
     * @param dependencyType Tipo de dependência entre as regras
     * @param description Explicação em linguagem de negócio da dependência
     */
    public BusinessRuleDependency(String sourceRuleId, 
                                 String targetRuleId,
                                 BusinessRuleDependencyType dependencyType,
                                 String description) {
        if (sourceRuleId == null || sourceRuleId.isBlank()) {
            throw new IllegalArgumentException("sourceRuleId não pode ser nulo ou vazio");
        }
        if (targetRuleId == null || targetRuleId.isBlank()) {
            throw new IllegalArgumentException("targetRuleId não pode ser nulo ou vazio");
        }
        if (sourceRuleId.equals(targetRuleId)) {
            throw new IllegalArgumentException("Regra não pode depender de si mesma");
        }
        if (dependencyType == null) {
            throw new IllegalArgumentException("dependencyType não pode ser nulo");
        }
        
        this.id = UUID.randomUUID();
        this.sourceRuleId = sourceRuleId;
        this.targetRuleId = targetRuleId;
        this.dependencyType = dependencyType;
        this.description = description;
        this.createdAt = Instant.now();
    }
    
    public UUID getId() {
        return id;
    }
    
    public String getSourceRuleId() {
        return sourceRuleId;
    }
    
    public String getTargetRuleId() {
        return targetRuleId;
    }
    
    public BusinessRuleDependencyType getDependencyType() {
        return dependencyType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusinessRuleDependency that = (BusinessRuleDependency) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("BusinessRuleDependency[%s -> %s (%s)]", 
            sourceRuleId, targetRuleId, dependencyType);
    }
}
