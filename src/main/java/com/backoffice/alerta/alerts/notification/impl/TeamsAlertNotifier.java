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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação de notificação via Microsoft Teams (Incoming Webhook)
 * 
 * US#56 - Alertas Inteligentes via Slack / Microsoft Teams
 * 
 * PRINCÍPIOS:
 * - READ-ONLY: não persiste dados
 * - Fallback seguro: retorna FAILED se webhook falhar
 * - Formato MessageCard (Office 365 Connector)
 * - Disclaimer obrigatório
 */
@Component
public class TeamsAlertNotifier implements AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(TeamsAlertNotifier.class);

    private final RestTemplate restTemplate;
    private final Gson gson;

    public TeamsAlertNotifier() {
        this.restTemplate = new RestTemplate();
        this.gson = new Gson();
    }

    @Override
    public NotificationStatus send(RiskMetricAlertResponse alert, String webhookUrl) {
        try {
            log.info("📣 Tentando enviar alerta via Teams - alertId: {}, type: {}", 
                     alert.getId(), alert.getType());

            String payload = buildTeamsPayload(alert);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                webhookUrl, 
                request, 
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Alerta enviado com sucesso via Teams - alertId: {}", alert.getId());
                return NotificationStatus.SENT;
            } else {
                log.warn("⚠️ Teams retornou status não-OK: {} - alertId: {}", 
                         response.getStatusCode(), alert.getId());
                return NotificationStatus.FAILED;
            }

        } catch (Exception e) {
            log.error("❌ Falha ao enviar alerta via Teams - alertId: {}, error: {}", 
                      alert.getId(), e.getMessage());
            return NotificationStatus.FAILED;
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.TEAMS;
    }

    /**
     * Constrói payload formatado para Teams (MessageCard)
     */
    private String buildTeamsPayload(RiskMetricAlertResponse alert) {
        Map<String, Object> card = new HashMap<>();
        
        // Tipo de card (MessageCard para Office 365 Connector)
        card.put("@type", "MessageCard");
        card.put("@context", "https://schema.org/extensions");
        
        // Cor baseada na severidade
        card.put("themeColor", getSeverityColor(alert.getSeverity()));
        
        // Título
        String emoji = getSeverityEmoji(alert.getSeverity());
        card.put("title", emoji + " Alerta de Risco Detectado");
        
        // Resumo
        card.put("summary", alert.getMessage());
        
        // Seções
        List<Map<String, Object>> sections = new ArrayList<>();
        
        // Seção principal
        Map<String, Object> mainSection = new HashMap<>();
        mainSection.put("activityTitle", alert.getType().toString());
        mainSection.put("activitySubtitle", "Severidade: " + alert.getSeverity());
        mainSection.put("text", alert.getMessage());
        
        // Facts (informações estruturadas)
        List<Map<String, String>> facts = new ArrayList<>();
        
        if (alert.getProjectContext() != null) {
            facts.add(createFact("Projeto", alert.getProjectContext().getProjectName()));
        }
        
        if (alert.getBusinessRuleId() != null) {
            facts.add(createFact("Regra de Negócio", alert.getBusinessRuleId()));
        }
        
        // Adicionar evidências
        if (alert.getEvidence() != null && !alert.getEvidence().isEmpty()) {
            alert.getEvidence().forEach((key, value) -> 
                facts.add(createFact(key, value.toString()))
            );
        }
        
        mainSection.put("facts", facts);
        sections.add(mainSection);
        
        // Seção de recomendações
        Map<String, Object> recommendationsSection = new HashMap<>();
        recommendationsSection.put("title", "Recomendações");
        recommendationsSection.put("text", getRecommendations(alert));
        sections.add(recommendationsSection);
        
        // Seção de disclaimer
        Map<String, Object> disclaimerSection = new HashMap<>();
        disclaimerSection.put("text", "⚠️ **Alerta consultivo** – nenhuma ação automática foi executada.");
        sections.add(disclaimerSection);
        
        card.put("sections", sections);
        
        return gson.toJson(card);
    }

    /**
     * Cria um fact para Teams
     */
    private Map<String, String> createFact(String name, String value) {
        Map<String, String> fact = new HashMap<>();
        fact.put("name", name);
        fact.put("value", value);
        return fact;
    }

    /**
     * Retorna cor hexadecimal baseada na severidade
     */
    private String getSeverityColor(AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "FF0000"; // Vermelho
            case WARNING -> "FFA500";  // Laranja
            case INFO -> "0078D4";     // Azul
        };
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
                "- Revisar regras de negócio do projeto\n" +
                "- Validar se bloqueios são verdadeiros positivos\n" +
                "- Considerar ajustar thresholds das regras";
            
            case RULE_OVERBLOCKING -> 
                "- Revisar criticidade da regra\n" +
                "- Analisar casos bloqueados\n" +
                "- Considerar criar exceções se necessário";
            
            case WARNING_SPIKE -> 
                "- Investigar mudanças recentes no código\n" +
                "- Verificar se houve deploy de novas regras\n" +
                "- Monitorar tendência nos próximos dias";
            
            case NEGATIVE_TREND -> 
                "- Identificar causa raiz da degradação\n" +
                "- Revisar commits recentes\n" +
                "- Considerar rollback se necessário";
            
            case SYSTEM_DEGRADATION -> 
                "- **URGENTE**: Sistema em estado crítico\n" +
                "- Escalar para time de arquitetura\n" +
                "- Avaliar desabilitar regras temporariamente";
            
            case POTENTIAL_FALSE_POSITIVE -> 
                "- Coletar feedback dos times impactados\n" +
                "- Ajustar thresholds da regra\n" +
                "- Considerar degradar severidade";
        };
    }
}
