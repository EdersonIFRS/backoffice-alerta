package com.backoffice.alerta.dto;

import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Domain;

import java.util.List;

/**
 * Response com explicação detalhada de uma regra de negócio
 * 
 * US#35 - Consulta Inteligente e Explicação de Regras de Negócio
 */
public class BusinessRuleExplanationResponse {
    
    private String id;
    private String name;
    private Domain domain;
    private Criticality criticality;
    private String businessDescription;
    private String operationalImpact;
    private String whyItMatters;
    private List<String> knownRisks;
    private HistoricalIncidentSummary historicalIncidents;
    private List<String> implementedByFiles;
    private List<OwnershipInfo> ownedByTeams;
    private List<String> riskHints;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public String getOperationalImpact() {
        return operationalImpact;
    }

    public void setOperationalImpact(String operationalImpact) {
        this.operationalImpact = operationalImpact;
    }

    public String getWhyItMatters() {
        return whyItMatters;
    }

    public void setWhyItMatters(String whyItMatters) {
        this.whyItMatters = whyItMatters;
    }

    public List<String> getKnownRisks() {
        return knownRisks;
    }

    public void setKnownRisks(List<String> knownRisks) {
        this.knownRisks = knownRisks;
    }

    public HistoricalIncidentSummary getHistoricalIncidents() {
        return historicalIncidents;
    }

    public void setHistoricalIncidents(HistoricalIncidentSummary historicalIncidents) {
        this.historicalIncidents = historicalIncidents;
    }

    public List<String> getImplementedByFiles() {
        return implementedByFiles;
    }

    public void setImplementedByFiles(List<String> implementedByFiles) {
        this.implementedByFiles = implementedByFiles;
    }

    public List<OwnershipInfo> getOwnedByTeams() {
        return ownedByTeams;
    }

    public void setOwnedByTeams(List<OwnershipInfo> ownedByTeams) {
        this.ownedByTeams = ownedByTeams;
    }

    public List<String> getRiskHints() {
        return riskHints;
    }

    public void setRiskHints(List<String> riskHints) {
        this.riskHints = riskHints;
    }

    /**
     * Resumo de incidentes históricos
     */
    public static class HistoricalIncidentSummary {
        private int totalIncidents;
        private int criticalIncidents;
        private int highIncidents;
        private String lastIncidentDate;
        private String summary;

        public int getTotalIncidents() {
            return totalIncidents;
        }

        public void setTotalIncidents(int totalIncidents) {
            this.totalIncidents = totalIncidents;
        }

        public int getCriticalIncidents() {
            return criticalIncidents;
        }

        public void setCriticalIncidents(int criticalIncidents) {
            this.criticalIncidents = criticalIncidents;
        }

        public int getHighIncidents() {
            return highIncidents;
        }

        public void setHighIncidents(int highIncidents) {
            this.highIncidents = highIncidents;
        }

        public String getLastIncidentDate() {
            return lastIncidentDate;
        }

        public void setLastIncidentDate(String lastIncidentDate) {
            this.lastIncidentDate = lastIncidentDate;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }
    }

    /**
     * Informação de ownership (time + role)
     */
    public static class OwnershipInfo {
        private String teamName;
        private String role;

        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
