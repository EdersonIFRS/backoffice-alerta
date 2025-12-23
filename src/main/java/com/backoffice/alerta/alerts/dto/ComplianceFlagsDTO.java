package com.backoffice.alerta.alerts.dto;

/**
 * DTO para flags de compliance da US#61 - Auditoria de Alertas
 * Indica se o alerta respeitou todas as regras de preferências
 * 
 * READ-ONLY - Usado apenas para auditoria
 */
public class ComplianceFlagsDTO {
    
    private boolean respectedSeverity;
    private boolean respectedChannel;
    private boolean respectedWindow;
    private boolean respectedHierarchy;
    
    public ComplianceFlagsDTO() {
    }
    
    public ComplianceFlagsDTO(boolean respectedSeverity, boolean respectedChannel, 
                              boolean respectedWindow, boolean respectedHierarchy) {
        this.respectedSeverity = respectedSeverity;
        this.respectedChannel = respectedChannel;
        this.respectedWindow = respectedWindow;
        this.respectedHierarchy = respectedHierarchy;
    }
    
    public boolean isRespectedSeverity() {
        return respectedSeverity;
    }
    
    public void setRespectedSeverity(boolean respectedSeverity) {
        this.respectedSeverity = respectedSeverity;
    }
    
    public boolean isRespectedChannel() {
        return respectedChannel;
    }
    
    public void setRespectedChannel(boolean respectedChannel) {
        this.respectedChannel = respectedChannel;
    }
    
    public boolean isRespectedWindow() {
        return respectedWindow;
    }
    
    public void setRespectedWindow(boolean respectedWindow) {
        this.respectedWindow = respectedWindow;
    }
    
    public boolean isRespectedHierarchy() {
        return respectedHierarchy;
    }
    
    public void setRespectedHierarchy(boolean respectedHierarchy) {
        this.respectedHierarchy = respectedHierarchy;
    }
}
