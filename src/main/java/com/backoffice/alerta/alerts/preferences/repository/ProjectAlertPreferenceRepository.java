package com.backoffice.alerta.alerts.preferences.repository;

import com.backoffice.alerta.alerts.preferences.domain.ProjectAlertPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para preferências de alertas por projeto
 * 
 * US#57 - Preferências de Alertas por Projeto e Regra de Negócio
 */
@Repository
public interface ProjectAlertPreferenceRepository extends JpaRepository<ProjectAlertPreference, UUID> {
    
    Optional<ProjectAlertPreference> findByProjectId(UUID projectId);
    
    boolean existsByProjectId(UUID projectId);
    
    void deleteByProjectId(UUID projectId);
}
