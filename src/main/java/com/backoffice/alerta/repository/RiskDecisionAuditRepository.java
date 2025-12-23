package com.backoffice.alerta.repository;

import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskDecisionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para auditoria de decisões de risco
 * 
 * US#30 - Persistência com PostgreSQL/JPA
 */
@Repository
public interface RiskDecisionAuditRepository extends JpaRepository<RiskDecisionAudit, UUID> {

    List<RiskDecisionAudit> findByPullRequestIdOrderByCreatedAtDesc(String pullRequestId);
    
    List<RiskDecisionAudit> findByFinalDecisionOrderByCreatedAtDesc(FinalDecision decision);
    
    List<RiskDecisionAudit> findAllByOrderByCreatedAtDesc();
}
