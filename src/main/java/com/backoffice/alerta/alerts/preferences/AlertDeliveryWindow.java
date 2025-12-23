package com.backoffice.alerta.alerts.preferences;

/**
 * Janela de entrega de alertas
 * 
 * US#57 - Preferências de Alertas por Projeto e Regra de Negócio
 */
public enum AlertDeliveryWindow {
    
    /**
     * Apenas em horário comercial (8h-18h, seg-sex)
     */
    BUSINESS_HOURS,
    
    /**
     * A qualquer momento (24/7)
     */
    ANY_TIME
}
