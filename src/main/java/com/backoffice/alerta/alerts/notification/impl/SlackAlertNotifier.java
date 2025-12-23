package com.backoffice.alerta.alerts.notification.impl;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.dto.RiskMetricAlertResponse;
import com.backoffice.alerta.alerts.notification.AlertNotifier;
import com.backoffice.alerta.alerts.notification.NotificationChannel;
import com.backoffice.alerta.alerts.notification.NotificationStatus;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementação de notificação via Slack (Incoming Webhook)
 * 
 * US#56 - Alertas Inteligentes via Slack / Microsoft Teams
 * 
 * PRINCÍPIOS:
 * - READ-ONLY: não persiste dados
 * - Fallback seguro: retorna FAILED se webhook falhar
 * - Mensagens formatadas com Markdown
 * - Disclaimer obrigatório
 */
@Component
public class SlackAlertNotifier implements AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(SlackAlertNotifier.class);

    private final RestTemplate restTemplate;
    private final Gson gson;

    public SlackAlertNotifier() {
        this.restTemplate = new RestTemplate();
        this.gson = new Gson();
    }

    @Override
    public NotificationStatus send(RiskMetricAlertResponse alert, String webhookUrl) {
        try {
            log.info("📣 Tentando enviar alerta via Slack - alertId: {}, type: {}", 
                     alert.getId(), alert.getType());

            String payload = buildSlackPayload(alert);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                webhookUrl, 
                request, 
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Alerta enviado com sucesso via Slack - alertId: {}", alert.getId());
                return NotificationStatus.SENT;
            } else {
                log.warn("⚠️ Slack retornou status não-OK: {} - alertId: {}", 
                         response.getStatusCode(), alert.getId());
                return NotificationStatus.FAILED;
            }

        } catch (Exception e) {
            log.error("❌ Falha ao enviar alerta via Slack - alertId: {}, error: {}", 
                      alert.getId(), e.getMessage());
            return NotificationStatus.FAILED;
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SLACK;
    }

    /**
     * Constrói payload formatado para Slack com Markdown
     */
    private String buildSlackPayload(RiskMetricAlertResponse alert) {
        StringBuilder text = new StringBuilder();

        // Emoji baseado na severidade
        String emoji = getSeverityEmoji(alert.getSeverity());
        
        // Título
        text.append(emoji).append(" *Alerta de Risco Detectado*\n\n");

        // Tipo do alerta
        text.append("*Tipo:* ").append(alert.getType()).append("\n");

        // Severidade
        text.append("*Severidade:* ").append(alert.getSeverity()).append("\n\n");

        // Mensagem
        text.append("*Descrição:*\n");
        text.append(alert.getMessage()).append("\n\n");

        // Projeto (se existir)
        if (alert.getProjectContext() != null) {
            text.append("*Projeto:* ").append(alert.getProjectContext().getProjectName()).append("\n");
        }

        // Regra (se existir)
        if (alert.getBusinessRuleId() != null) {
            text.append("*Regra de Negócio:* ").append(alert.getBusinessRuleId()).append("\n");
        }

        // Evidências
        if (alert.getEvidence() != null && !alert.getEvidence().isEmpty()) {
            text.append("\n*Métricas:*\n");
            alert.getEvidence().forEach((key, value) -> 
                text.append("• ").append(key).append(": ").append(value).append("\n")
            );
        }

        // Recomendações baseadas no tipo
        text.append("\n*Recomendações:*\n");
        text.append(getRecommendations(alert));

        // Disclaimer obrigatório
        text.append("\n\n⚠️ _Alerta consultivo – nenhuma ação automática foi executada._");

        Map<String, Object> payload = new HashMap<>();
        payload.put("text", text.toString());
        payload.put("mrkdwn", true);

        return gson.toJson(payload);
    }

    /**
     * Retorna emoji baseado na severidade
     */
    private String getSeverityEmoji(AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "🚨";
            case WARNING -> "⚠️";
            case INFO -> "ℹ️";
        };
    }

    /**
     * Retorna recomendações baseadas no tipo de alerta
     */
    private String getRecommendations(RiskMetricAlertResponse alert) {
        return switch (alert.getType()) {
            case HIGH_BLOCK_RATE_PROJECT -> 
                "• Revisar regras de negócio do projeto\n" +
                "• Validar se bloqueios são verdadeiros positivos\n" +
                "• Considerar ajustar thresholds das regras";
            
            case RULE_OVERBLOCKING -> 
                "• Revisar criticidade da regra\n" +
                "• Analisar casos bloqueados\n" +
                "• Considerar criar exceções se necessário";
            
            case WARNING_SPIKE -> 
                "• Investigar mudanças recentes no código\n" +
                "• Verificar se houve deploy de novas regras\n" +
                "• Monitorar tendência nos próximos dias";
            
            case NEGATIVE_TREND -> 
                "• Identificar causa raiz da degradação\n" +
                "• Revisar commits recentes\n" +
                "• Considerar rollback se necessário";
            
            case SYSTEM_DEGRADATION -> 
                "• URGENTE: Sistema em estado crítico\n" +
                "• Escalar para time de arquitetura\n" +
                "• Avaliar desabilitar regras temporariamente";
            
            case POTENTIAL_FALSE_POSITIVE -> 
                "• Coletar feedback dos times impactados\n" +
                "• Ajustar thresholds da regra\n" +
                "• Considerar degradar severidade";
        };
    }
}
