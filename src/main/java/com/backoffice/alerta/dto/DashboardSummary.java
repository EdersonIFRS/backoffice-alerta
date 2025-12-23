package com.backoffice.alerta.dto;

/**
 * Sumário quantitativo do dashboard executivo
 * 
 * Contadores de decisões, aprovações, bloqueios e problemas
 */
public class DashboardSummary {
    
    private final int totalDecisions;
    private final int approved;
    private final int approvedWithRestrictions;
    private final int blocked;
    private final int incidentsAfterApproval;
    private final int falsePositives;
    private final int falseNegatives;

    public DashboardSummary(int totalDecisions,
                           int approved,
                           int approvedWithRestrictions,
                           int blocked,
                           int incidentsAfterApproval,
                           int falsePositives,
                           int falseNegatives) {
        this.totalDecisions = totalDecisions;
        this.approved = approved;
        this.approvedWithRestrictions = approvedWithRestrictions;
        this.blocked = blocked;
        this.incidentsAfterApproval = incidentsAfterApproval;
        this.falsePositives = falsePositives;
        this.falseNegatives = falseNegatives;
    }

    public int getTotalDecisions() {
        return totalDecisions;
    }

    public int getApproved() {
        return approved;
    }

    public int getApprovedWithRestrictions() {
        return approvedWithRestrictions;
    }

    public int getBlocked() {
        return blocked;
    }

    public int getIncidentsAfterApproval() {
        return incidentsAfterApproval;
    }

    public int getFalsePositives() {
        return falsePositives;
    }

    public int getFalseNegatives() {
        return falseNegatives;
    }
}
