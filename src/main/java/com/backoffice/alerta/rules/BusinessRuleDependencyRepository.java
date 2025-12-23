package com.backoffice.alerta.rules;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repositório in-memory para dependências entre regras de negócio
 * 
 * Gerencia relacionamentos direcionais entre regras para análise de impacto cruzado.
 * 
 * US#36 - Análise de Impacto Cruzado (Cadeia de Regras Afetadas)
 */
@Repository
public class BusinessRuleDependencyRepository {
    
    private final Map<UUID, BusinessRuleDependency> storage = new ConcurrentHashMap<>();
    
    /**
     * Salva uma dependência entre regras
     * 
     * @param dependency Dependência a ser salva
     * @return Dependência salva
     */
    public BusinessRuleDependency save(BusinessRuleDependency dependency) {
        storage.put(dependency.getId(), dependency);
        return dependency;
    }
    
    /**
     * Busca todas as dependências registradas
     * 
     * @return Lista de todas as dependências
     */
    public List<BusinessRuleDependency> findAll() {
        return storage.values().stream().toList();
    }
    
    /**
     * Busca dependências onde a regra é a ORIGEM (afeta outras)
     * 
     * Exemplo: Se BR-001 FEEDS BR-002, ao buscar sourceRuleId="BR-001",
     * retorna a dependência BR-001 -> BR-002
     * 
     * @param sourceRuleId ID da regra de origem
     * @return Lista de dependências onde esta regra afeta outras
     */
    public List<BusinessRuleDependency> findBySourceRuleId(String sourceRuleId) {
        return storage.values().stream()
            .filter(dep -> dep.getSourceRuleId().equals(sourceRuleId))
            .collect(Collectors.toList());
    }
    
    /**
     * Busca dependências onde a regra é o DESTINO (é afetada por outras)
     * 
     * Exemplo: Se BR-001 FEEDS BR-002, ao buscar targetRuleId="BR-002",
     * retorna a dependência BR-001 -> BR-002
     * 
     * @param targetRuleId ID da regra de destino
     * @return Lista de dependências onde esta regra é afetada por outras
     */
    public List<BusinessRuleDependency> findByTargetRuleId(String targetRuleId) {
        return storage.values().stream()
            .filter(dep -> dep.getTargetRuleId().equals(targetRuleId))
            .collect(Collectors.toList());
    }
    
    /**
     * Remove todas as dependências (útil para testes)
     */
    public void clear() {
        storage.clear();
    }
    
    /**
     * Conta total de dependências
     * 
     * @return Número de dependências registradas
     */
    public long count() {
        return storage.size();
    }
}
