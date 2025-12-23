package com.backoffice.alerta.rules;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade imutável de auditoria de decisões de risco
 * Registra trilha completa para governança e compliance
 * 
 * ⚠️ IMUTÁVEL - não pode ser alterada ou excluída após criação
 * 
 * US#30 - Persistência com PostgreSQL/JPA
 */
@Entity
@Table(name = "risk_decision_audit")
public class RiskDecisionAudit {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "pull_request_id", nullable = false)
    private String pullRequestId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false)
    private Environment environment;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;
    
    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "final_decision", nullable = false)
    private FinalDecision finalDecision;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "risk_audit_impacted_rules", joinColumns = @JoinColumn(name = "audit_id"))
    @Column(name = "business_rule_id")
    private List<String> impactedBusinessRules = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "risk_audit_incident_summary", joinColumns = @JoinColumn(name = "audit_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "severity")
    @Column(name = "count")
    private Map<IncidentSeverity, Integer> incidentSummary = new HashMap<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "risk_audit_restrictions", joinColumns = @JoinColumn(name = "audit_id"))
    @Column(name = "restriction")
    private List<String> restrictions = new ArrayList<>();
    
    @Column(name = "ai_consulted", nullable = false)
    private Boolean aiConsulted;
    
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;
    
    @Column(name = "policy_snapshot", columnDefinition = "TEXT")
    private String policySnapshot;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Construtor protegido para JPA
     */
    protected RiskDecisionAudit() {
    }

    /**
     * Construtor completo - todos os campos são obrigatórios no momento da criação
     */
    public RiskDecisionAudit(String pullRequestId,
                            Environment environment,
                            RiskLevel riskLevel,
                            Integer riskScore,
                            FinalDecision finalDecision,
                            List<String> impactedBusinessRules,
                            Map<IncidentSeverity, Integer> incidentSummary,
                            List<String> restrictions,
                            Boolean aiConsulted,
                            String aiSummary,
                            String policySnapshot) {
        this.id = UUID.randomUUID();
        this.pullRequestId = pullRequestId;
        this.environment = environment;
        this.riskLevel = riskLevel;
        this.riskScore = riskScore;
        this.finalDecision = finalDecision;
        this.impactedBusinessRules = new ArrayList<>(impactedBusinessRules);
        this.incidentSummary = new HashMap<>(incidentSummary);
        this.restrictions = new ArrayList<>(restrictions);
        this.aiConsulted = aiConsulted;
        this.aiSummary = aiSummary;
        this.policySnapshot = policySnapshot;
        this.createdAt = Instant.now();
    }

    // Apenas getters - sem setters para garantir imutabilidade

    public UUID getId() {
        return id;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public FinalDecision getFinalDecision() {
        return finalDecision;
    }

    public List<String> getImpactedBusinessRules() {
        return new ArrayList<>(impactedBusinessRules);
    }

    public Map<IncidentSeverity, Integer> getIncidentSummary() {
        return new HashMap<>(incidentSummary);
    }

    public List<String> getRestrictions() {
        return new ArrayList<>(restrictions);
    }

    public Boolean getAiConsulted() {
        return aiConsulted;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public String getPolicySnapshot() {
        return policySnapshot;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskDecisionAudit that = (RiskDecisionAudit) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
