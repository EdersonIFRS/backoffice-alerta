package com.backoffice.alerta.rules;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repositório em memória para regras de negócio
 */
@Repository
public class BusinessRuleRepository {

    private final Map<String, BusinessRule> storage = new ConcurrentHashMap<>();

    /**
     * Salva uma regra de negócio
     * @param rule Regra a ser salva
     * @return Regra salva
     */
    public BusinessRule save(BusinessRule rule) {
        storage.put(rule.getId(), rule);
        return rule;
    }

    /**
     * Busca todas as regras de negócio
     * @return Lista de todas as regras
     */
    public List<BusinessRule> findAll() {
        return storage.values().stream().toList();
    }

    /**
     * Busca uma regra de negócio por ID
     * @param id ID da regra
     * @return Optional com a regra, se encontrada
     */
    public Optional<BusinessRule> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Verifica se existe uma regra com o ID informado
     * @param id ID da regra
     * @return true se existe, false caso contrário
     */
    public boolean existsById(String id) {
        return storage.containsKey(id);
    }

    /**
     * Busca regras de negócio por ID do projeto
     * @param projectId ID do projeto
     * @return Lista de regras associadas ao projeto
     */
    public List<BusinessRule> findByProjectId(UUID projectId) {
        return storage.values().stream()
                .filter(rule -> projectId.equals(rule.getProjectId()))
                .toList();
    }
}
