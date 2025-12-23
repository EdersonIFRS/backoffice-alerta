package com.backoffice.alerta.repository;

import com.backoffice.alerta.notification.NotificationSeverity;
import com.backoffice.alerta.notification.RiskNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para notificações de risco
 * 
 * US#30 - Persistência com PostgreSQL/JPA
 */
@Repository
public interface RiskNotificationRepository extends JpaRepository<RiskNotification, UUID> {

    List<RiskNotification> findByAuditIdOrderByCreatedAtDesc(UUID auditId);
    
    List<RiskNotification> findByTeamNameOrderByCreatedAtDesc(String teamName);
    
    List<RiskNotification> findBySeverityOrderByCreatedAtDesc(NotificationSeverity severity);
    
    List<RiskNotification> findAllByOrderByCreatedAtDesc();
}
