package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Domain;
import com.backoffice.alerta.rules.ImpactType;
import com.backoffice.alerta.rules.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Resposta com informações de uma regra de negócio impactada
 */
@Schema(description = "Detalhes de uma regra de negócio impactada")
public class ImpactedBusinessRuleResponse {

    @Schema(description = "ID da regra de negócio", example = "BR-001")
    private String businessRuleId;

    @Schema(description = "Nome da regra de negócio", example = "Processamento de Pagamentos")
    private String name;

    @Schema(description = "Domínio da regra", example = "PAYMENT")
    private Domain domain;

    @Schema(description = "Criticidade da regra", example = "CRITICA")
    private Criticality criticality;

    @Schema(description = "Tipo de impacto", example = "DIRECT")
    private ImpactType impactType;

    @Schema(description = "Arquivos impactados", 
            example = "[\"src/main/java/com/app/payment/PaymentService.java\"]")
    private List<String> impactedFiles;

    @Schema(description = "Nível de risco calculado", example = "CRITICO")
    private RiskLevel riskLevel;

    @Schema(description = "Explicação do impacto em linguagem de negócio")
    private String explanation;

    @Schema(description = "Ownerships organizacionais da regra (times responsáveis)")
    private List<BusinessRuleOwnershipResponse> ownerships;

    public ImpactedBusinessRuleResponse() {
    }

    public ImpactedBusinessRuleResponse(String businessRuleId, String name, Domain domain, 
                                       Criticality criticality, ImpactType impactType, 
                                       List<String> impactedFiles, RiskLevel riskLevel, 
                                       String explanation,
                                       List<BusinessRuleOwnershipResponse> ownerships) {
        this.businessRuleId = businessRuleId;
        this.name = name;
        this.domain = domain;
        this.criticality = criticality;
        this.impactType = impactType;
        this.impactedFiles = impactedFiles;
        this.riskLevel = riskLevel;
        this.explanation = explanation;
        this.ownerships = ownerships;
    }

    public String getBusinessRuleId() {
        return businessRuleId;
    }

    public void setBusinessRuleId(String businessRuleId) {
        this.businessRuleId = businessRuleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public void setCriticality(Criticality criticality) {
        this.criticality = criticality;
    }

    public ImpactType getImpactType() {
        return impactType;
    }

    public void setImpactType(ImpactType impactType) {
        this.impactType = impactType;
    }

    public List<String> getImpactedFiles() {
        return impactedFiles;
    }

    public void setImpactedFiles(List<String> impactedFiles) {
        this.impactedFiles = impactedFiles;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<BusinessRuleOwnershipResponse> getOwnerships() {
        return ownerships;
    }

    public void setOwnerships(List<BusinessRuleOwnershipResponse> ownerships) {
        this.ownerships = ownerships;
    }
}
