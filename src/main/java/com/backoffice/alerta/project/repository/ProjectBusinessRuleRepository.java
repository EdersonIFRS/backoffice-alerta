package com.backoffice.alerta.project.repository;

import com.backoffice.alerta.project.domain.ProjectBusinessRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * US#49 - Repository para associações entre Projects e BusinessRules
 */
@Repository
public interface ProjectBusinessRuleRepository extends JpaRepository<ProjectBusinessRule, UUID> {

    /**
     * Verifica se já existe associação entre projeto e regra
     * 
     * @param projectId ID do projeto
     * @param businessRuleId ID da regra
     * @return true se associação já existe
     */
    boolean existsByProjectIdAndBusinessRuleId(UUID projectId, String businessRuleId);

    /**
     * Lista todas as associações de um projeto
     * 
     * @param projectId ID do projeto
     * @return Lista de associações
     */
    List<ProjectBusinessRule> findByProjectId(UUID projectId);

    /**
     * Lista todas as associações de uma regra de negócio
     * 
     * @param businessRuleId ID da regra
     * @return Lista de associações
     */
    List<ProjectBusinessRule> findByBusinessRuleId(String businessRuleId);

    /**
     * Conta quantas regras estão associadas a um projeto
     * 
     * @param projectId ID do projeto
     * @return Quantidade de regras
     */
    long countByProjectId(UUID projectId);
}
