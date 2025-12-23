package com.backoffice.alerta.alerts.notification;

import com.backoffice.alerta.alerts.AlertSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository para histórico de notificações de alertas
 * 
 * US#59 - Histórico e Rastreabilidade de Notificações de Alerta
 */
@Repository
public interface RiskAlertNotificationHistoryRepository extends JpaRepository<RiskAlertNotificationHistory, UUID> {
    
    /**
     * Busca histórico por projeto
     */
    List<RiskAlertNotificationHistory> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
    
    /**
     * Busca histórico por regra de negócio
     */
    List<RiskAlertNotificationHistory> findByBusinessRuleIdOrderByCreatedAtDesc(String businessRuleId);
    
    /**
     * Timeline de alertas (ordenado por data descendente)
     */
    @Query("SELECT h FROM RiskAlertNotificationHistory h WHERE h.createdAt BETWEEN :fromDate AND :toDate ORDER BY h.createdAt DESC")
    List<RiskAlertNotificationHistory> findTimeline(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
    
    /**
     * Contagem por status
     */
    @Query("SELECT COUNT(h) FROM RiskAlertNotificationHistory h WHERE h.status = :status")
    long countByStatus(@Param("status") NotificationStatus status);
    
    /**
     * Contagem por severidade
     */
    @Query("SELECT COUNT(h) FROM RiskAlertNotificationHistory h WHERE h.severity = :severity")
    long countBySeverity(@Param("severity") AlertSeverity severity);
    
    /**
     * Top projetos com mais alertas
     */
    @Query(value = "SELECT h.project_name, COUNT(*) as total FROM risk_alert_notification_history h " +
           "WHERE h.project_name IS NOT NULL " +
           "GROUP BY h.project_name " +
           "ORDER BY total DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopProjectsByAlertCount(@Param("limit") int limit);
    
    /**
     * Top regras com mais alertas
     */
    @Query(value = "SELECT h.business_rule_name, COUNT(*) as total FROM risk_alert_notification_history h " +
           "WHERE h.business_rule_name IS NOT NULL " +
           "GROUP BY h.business_rule_name " +
           "ORDER BY total DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopRulesByAlertCount(@Param("limit") int limit);
}
