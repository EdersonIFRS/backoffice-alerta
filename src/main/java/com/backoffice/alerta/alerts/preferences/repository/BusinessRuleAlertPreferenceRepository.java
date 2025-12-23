package com.backoffice.alerta.alerts.preferences.repository;

import com.backoffice.alerta.alerts.preferences.domain.BusinessRuleAlertPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para preferências de alertas por regra de negócio
 * 
 * US#57 - Preferências de Alertas por Projeto e Regra de Negócio
 */
@Repository
public interface BusinessRuleAlertPreferenceRepository extends JpaRepository<BusinessRuleAlertPreference, UUID> {
    
    Optional<BusinessRuleAlertPreference> findByBusinessRuleId(String businessRuleId);
    
    boolean existsByBusinessRuleId(String businessRuleId);
    
    void deleteByBusinessRuleId(String businessRuleId);
}
