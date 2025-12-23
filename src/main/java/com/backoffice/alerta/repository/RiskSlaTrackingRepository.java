package com.backoffice.alerta.repository;

import com.backoffice.alerta.sla.RiskSlaTracking;
import com.backoffice.alerta.sla.SlaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para rastreamento de SLA de risco
 * 
 * US#30 - Persistência com PostgreSQL/JPA
 */
@Repository
public interface RiskSlaTrackingRepository extends JpaRepository<RiskSlaTracking, UUID> {

    List<RiskSlaTracking> findByStatusOrderByCreatedAtDesc(SlaStatus status);
    
    List<RiskSlaTracking> findByAuditIdOrderByCreatedAtDesc(UUID auditId);
    
    default List<RiskSlaTracking> findBreached() {
        return findByStatusOrderByCreatedAtDesc(SlaStatus.BREACHED);
    }
    
    List<RiskSlaTracking> findAllByOrderByCreatedAtDesc();
}
