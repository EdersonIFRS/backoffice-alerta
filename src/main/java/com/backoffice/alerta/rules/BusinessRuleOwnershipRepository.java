package com.backoffice.alerta.rules;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repositório in-memory para gerenciar ownerships organizacionais de regras de negócio
 * 
 * Armazena mappings entre regras de negócio e seus times responsáveis
 */
@Repository
public class BusinessRuleOwnershipRepository {

    private final ConcurrentHashMap<UUID, BusinessRuleOwnership> ownerships = new ConcurrentHashMap<>();

    /**
     * Salva um novo ownership (ou substitui se já existir com mesmo ID)
     */
    public BusinessRuleOwnership save(BusinessRuleOwnership ownership) {
        ownerships.put(ownership.getId(), ownership);
        return ownership;
    }

    /**
     * Retorna todos os ownerships cadastrados
     */
    public List<BusinessRuleOwnership> findAll() {
        return ownerships.values().stream()
                .collect(Collectors.toList());
    }

    /**
     * Busca ownerships por ID da regra de negócio
     * Uma regra pode ter múltiplos owners (PRIMARY, SECONDARY, BACKUP)
     */
    public List<BusinessRuleOwnership> findByBusinessRuleId(UUID businessRuleId) {
        return ownerships.values().stream()
                .filter(o -> o.getBusinessRuleId().equals(businessRuleId))
                .collect(Collectors.toList());
    }

    /**
     * Verifica se já existe ownership com role específico para uma regra
     * Usado para prevenir duplicação de PRIMARY_OWNER
     */
    public boolean existsByBusinessRuleIdAndRole(UUID businessRuleId, OwnershipRole role) {
        return ownerships.values().stream()
                .anyMatch(o -> o.getBusinessRuleId().equals(businessRuleId) 
                            && o.getRole() == role);
    }

    /**
     * Remove ownership pelo ID
     * Retorna true se removido, false se não encontrado
     */
    public boolean deleteById(UUID id) {
        return ownerships.remove(id) != null;
    }

    /**
     * Busca ownership pelo ID
     */
    public BusinessRuleOwnership findById(UUID id) {
        return ownerships.get(id);
    }
}
