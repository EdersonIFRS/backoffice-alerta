package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO de resposta para registro de auditoria de decisão de risco
 * Somente leitura - sem setters
 */
@Schema(description = "Registro de auditoria de decisão de risco (imutável)")
public class RiskDecisionAuditResponse {

    @Schema(description = "ID único do registro de auditoria", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID do Pull Request", example = "PR-458")
    private String pullRequestId;

    @Schema(description = "Ambiente de deploy", example = "PRODUCTION")
    private Environment environment;

    @Schema(description = "Nível de risco calculado", example = "CRITICO")
    private RiskLevel riskLevel;

    @Schema(description = "Pontuação numérica de risco", example = "95")
    private Integer riskScore;

    @Schema(description = "Decisão final do sistema", example = "BLOQUEADO")
    private FinalDecision finalDecision;

    @Schema(description = "Regras de negócio impactadas")
    private List<String> impactedBusinessRules;

    @Schema(description = "Resumo de incidentes por severidade")
    private Map<IncidentSeverity, Integer> incidentSummary;

    @Schema(description = "Restrições e ações obrigatórias")
    private List<String> restrictions;

    @Schema(description = "Indica se IA foi consultada", example = "true")
    private Boolean aiConsulted;

    @Schema(description = "Resumo da análise consultiva da IA", 
            example = "Mudanças semelhantes causaram incidentes críticos em produção.")
    private String aiSummary;

    @Schema(description = "Snapshot da política de risco aplicada", example = "RiskPolicy v2.1")
    private String policySnapshot;

    @Schema(description = "Data e hora da criação do registro", example = "2025-12-14T18:45:12Z")
    private Instant createdAt;

    public RiskDecisionAuditResponse() {
    }

    /**
     * Construtor a partir da entidade RiskDecisionAudit
     */
    public RiskDecisionAuditResponse(RiskDecisionAudit audit) {
        this.id = audit.getId();
        this.pullRequestId = audit.getPullRequestId();
        this.environment = audit.getEnvironment();
        this.riskLevel = audit.getRiskLevel();
        this.riskScore = audit.getRiskScore();
        this.finalDecision = audit.getFinalDecision();
        this.impactedBusinessRules = audit.getImpactedBusinessRules();
        this.incidentSummary = audit.getIncidentSummary();
        this.restrictions = audit.getRestrictions();
        this.aiConsulted = audit.getAiConsulted();
        this.aiSummary = audit.getAiSummary();
        this.policySnapshot = audit.getPolicySnapshot();
        this.createdAt = audit.getCreatedAt();
    }

    // Apenas getters - sem setters (read-only)

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
        return impactedBusinessRules;
    }

    public Map<IncidentSeverity, Integer> getIncidentSummary() {
        return incidentSummary;
    }

    public List<String> getRestrictions() {
        return restrictions;
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
}
