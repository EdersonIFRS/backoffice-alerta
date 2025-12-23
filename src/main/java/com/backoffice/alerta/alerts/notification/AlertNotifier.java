package com.backoffice.alerta.alerts.notification;

import com.backoffice.alerta.alerts.dto.RiskMetricAlertResponse;

/**
 * Interface para implementações de notificação de alertas
 * 
 * US#56 - Alertas Inteligentes via Slack / Microsoft Teams
 * 
 * Padrão Strategy: permite trocar implementação sem alterar código cliente
 */
public interface AlertNotifier {
    
    /**
     * Envia alerta para canal de notificação
     * 
     * @param alert Alerta a ser enviado
     * @param webhookUrl URL do webhook (Slack ou Teams)
     * @return Status do envio (SENT, FAILED, SKIPPED)
     */
    NotificationStatus send(RiskMetricAlertResponse alert, String webhookUrl);
    
    /**
     * Retorna o canal suportado por este notifier
     * 
     * @return Canal de notificação
     */
    NotificationChannel getChannel();
}
