package com.backoffice.alerta.alerts.dto;

import java.time.LocalDate;

/**
 * DTO para timeline de auditoria da US#61
 * Agregação diária de alertas enviados/bloqueados/falhados
 * 
 * READ-ONLY - Usado apenas para análise temporal
 */
public class AlertAuditTimelineResponse {
    
    private LocalDate date;
    private long totalSent;
    private long totalSkipped;
    private long totalFailed;
    
    public AlertAuditTimelineResponse() {
    }
    
    public AlertAuditTimelineResponse(LocalDate date, long totalSent, 
                                     long totalSkipped, long totalFailed) {
        this.date = date;
        this.totalSent = totalSent;
        this.totalSkipped = totalSkipped;
        this.totalFailed = totalFailed;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public long getTotalSent() {
        return totalSent;
    }
    
    public void setTotalSent(long totalSent) {
        this.totalSent = totalSent;
    }
    
    public long getTotalSkipped() {
        return totalSkipped;
    }
    
    public void setTotalSkipped(long totalSkipped) {
        this.totalSkipped = totalSkipped;
    }
    
    public long getTotalFailed() {
        return totalFailed;
    }
    
    public void setTotalFailed(long totalFailed) {
        this.totalFailed = totalFailed;
    }
}
