package com.backoffice.alerta.repository;

import com.backoffice.alerta.rules.BusinessRuleIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para incidentes de regras de negócio
 * 
 * US#30 - Persistência com PostgreSQL/JPA
 */
@Repository
public interface BusinessRuleIncidentRepository extends JpaRepository<BusinessRuleIncident, UUID> {

    List<BusinessRuleIncident> findByBusinessRuleIdOrderByOccurredAtDesc(UUID businessRuleId);
}
