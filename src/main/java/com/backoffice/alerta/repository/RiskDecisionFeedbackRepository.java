package com.backoffice.alerta.repository;

import com.backoffice.alerta.rules.RiskDecisionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA para feedback de decisões de risco
 * 
 * US#30 - Persistência com PostgreSQL/JPA
 */
@Repository
public interface RiskDecisionFeedbackRepository extends JpaRepository<RiskDecisionFeedback, UUID> {

    Optional<RiskDecisionFeedback> findByAuditId(UUID auditId);
    
    List<RiskDecisionFeedback> findByPullRequestIdOrderByCreatedAtDesc(String pullRequestId);
    
    boolean existsByAuditId(UUID auditId);
    
    List<RiskDecisionFeedback> findAllByOrderByCreatedAtDesc();
}
