package com.backoffice.alerta.dashboard.dto;

import java.time.LocalDate;

/**
 * Ponto de tendência de alertas por data
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
public class AlertTrendPoint {
    
    private LocalDate date;
    private long sent;
    private long skipped;
    private long failed;
    
    public AlertTrendPoint() {
    }
    
    public AlertTrendPoint(LocalDate date, long sent, long skipped, long failed) {
        this.date = date;
        this.sent = sent;
        this.skipped = skipped;
        this.failed = failed;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public long getSent() {
        return sent;
    }
    
    public void setSent(long sent) {
        this.sent = sent;
    }
    
    public long getSkipped() {
        return skipped;
    }
    
    public void setSkipped(long skipped) {
        this.skipped = skipped;
    }
    
    public long getFailed() {
        return failed;
    }
    
    public void setFailed(long failed) {
        this.failed = failed;
    }
}
