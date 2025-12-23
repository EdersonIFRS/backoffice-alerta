package com.backoffice.alerta.alerts.notification;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.dto.RiskMetricAlertResponse;
import com.backoffice.alerta.alerts.preferences.service.AlertPreferenceService;
import com.backoffice.alerta.alerts.service.RiskMetricAlertService;
import com.backoffice.alerta.alerts.notification.dto.RiskAlertNotificationRequest;
import com.backoffice.alerta.alerts.notification.dto.RiskAlertNotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Serviço de orquestração de notificações de alertas
 * 
 * US#56 - Alertas Inteligentes via Slack / Microsoft Teams
 * US#57 - Preferências de Alertas (integração)
 * US#59 - Histórico e Rastreabilidade de Notificações (persistência)
 * 
 * PRINCÍPIOS:
 * - Filtro de severidade: ignora INFO
 * - Respeita preferências configuradas (US#57)
 * - Persiste histórico de TODAS as tentativas (US#59)
 * - Fallback seguro: sempre retorna response, nunca lança exceções
 * - Logs estruturados com emojis
 */
@Service
public class RiskAlertNotificationService {

    private static final Logger log = LoggerFactory.getLogger(RiskAlertNotificationService.class);

    private final RiskMetricAlertService alertService;
    private final List<AlertNotifier> notifiers;
    private final AlertPreferenceService preferenceService;
    private final RiskAlertNotificationHistoryRepository historyRepository;

    public RiskAlertNotificationService(
            RiskMetricAlertService alertService,
            List<AlertNotifier> notifiers,
            AlertPreferenceService preferenceService,
            RiskAlertNotificationHistoryRepository historyRepository) {
        this.alertService = alertService;
        this.notifiers = notifiers;
        this.preferenceService = preferenceService;
        this.historyRepository = historyRepository;
    }

    /**
     * Envia notificação para um alerta específico
     * 
     * @param alertId ID do alerta a ser notificado
     * @param request Dados da notificação (canal, webhook)
     * @return Response com status da notificação
     */
    public RiskAlertNotificationResponse notifyAlert(UUID alertId, RiskAlertNotificationRequest request) {
        try {
            log.info("📣 Iniciando notificação - alertId: {}, channel: {}", 
                     alertId, request.getChannel());

            // 1. Buscar alerta
            RiskMetricAlertResponse alert = findAlertById(alertId);
            
            if (alert == null) {
                log.warn("⚠️ Alerta não encontrado - alertId: {}", alertId);
                return RiskAlertNotificationResponse.error(
                    alertId,
                    request.getChannel(),
                    "Alerta não encontrado"
                );
            }

            // 2. Filtrar severidade (ignorar INFO)
            if (alert.getSeverity() == AlertSeverity.INFO) {
                log.info("ℹ️ Alerta ignorado (severidade INFO) - alertId: {}", alertId);
                
                // US#59 - Persistir histórico mesmo para alertas SKIPPED
                persistHistory(alert, request.getChannel(), NotificationStatus.SKIPPED, 
                              "BLOCKED_BY_SEVERITY_INFO", request.getWebhookUrl());
                
                return new RiskAlertNotificationResponse(
                    alertId,
                    request.getChannel(),
                    NotificationStatus.SKIPPED,
                    "Alerta com severidade INFO não é notificado"
                );
            }

            // 2.5. US#57 - Verificar preferências de alerta
            UUID projectId = alert.getProjectContext() != null ? alert.getProjectContext().getProjectId() : null;
            String businessRuleId = alert.getBusinessRuleId();
            
            boolean shouldSend = preferenceService.shouldSendAlert(
                projectId,
                businessRuleId,
                alert.getType(),
                alert.getSeverity(),
                request.getChannel()
            );
            
            if (!shouldSend) {
                log.info("🚫 Alerta ignorado por preferência - alertId: {}, project: {}, rule: {}", 
                         alertId, projectId, businessRuleId);
                
                // US#59 - Persistir histórico de alerta bloqueado por preferência
                persistHistory(alert, request.getChannel(), NotificationStatus.SKIPPED, 
                              "BLOCKED_BY_PREFERENCE", request.getWebhookUrl());
                
                return new RiskAlertNotificationResponse(
                    alertId,
                    request.getChannel(),
                    NotificationStatus.SKIPPED,
                    "Alerta suprimido por preferências configuradas"
                );
            }

            // 

            // 3. Selecionar notificador correto
            AlertNotifier notifier = selectNotifier(request.getChannel());
            
            if (notifier == null) {
                log.error("❌ Notificador não encontrado - channel: {}", request.getChannel());
                return RiskAlertNotificationResponse.error(
                    alertId,
                    request.getChannel(),
                    "Canal de notificação não suportado"
                );
            }

            // 4. Enviar notificação
            NotificationStatus status = notifier.send(alert, request.getWebhookUrl());

            // US#59 - Persistir histórico após tentativa de envio
            String deliveryReason = (status == NotificationStatus.SENT) 
                ? "PASSED_PREFERENCE" 
                : "SEND_FAILED";
            persistHistory(alert, request.getChannel(), status, deliveryReason, request.getWebhookUrl());

            // 5. Construir response
            if (status == NotificationStatus.SENT) {
                log.info("✅ Notificação enviada com sucesso - alertId: {}, channel: {}", 
                         alertId, request.getChannel());
                return RiskAlertNotificationResponse.success(alertId, request.getChannel());
            } else {
                log.warn("⚠️ Falha ao enviar notificação - alertId: {}, channel: {}, status: {}", 
                         alertId, request.getChannel(), status);
                return RiskAlertNotificationResponse.error(
                    alertId,
                    request.getChannel(),
                    "Falha ao enviar notificação via " + request.getChannel()
                );
            }

        } catch (Exception e) {
            log.error("❌ Erro inesperado ao notificar alerta - alertId: {}, error: {}", 
                      alertId, e.getMessage(), e);
            return RiskAlertNotificationResponse.error(
                alertId,
                request.getChannel(),
                "Erro interno: " + e.getMessage()
            );
        }
    }

    /**
     * Verifica saúde do serviço de notificações
     * 
     * @return Status de saúde
     */
    public NotificationHealthResponse checkHealth() {
        try {
            int availableChannels = notifiers.size();
            
            log.info("🏥 Health check - canais disponíveis: {}", availableChannels);
            
            return new NotificationHealthResponse(
                "UP",
                availableChannels,
                List.of(NotificationChannel.values()),
                Instant.now()
            );
            
        } catch (Exception e) {
            log.error("❌ Erro ao verificar saúde - error: {}", e.getMessage());
            return new NotificationHealthResponse(
                "DOWN",
                0,
                List.of(),
                Instant.now()
            );
        }
    }

    /**
     * Busca alerta por ID (in-memory)
     */
    private RiskMetricAlertResponse findAlertById(UUID alertId) {
        try {
            // Buscar em todos os alertas detectados
            return alertService.detectAlerts(null, null, null).stream()
                .filter(alert -> alert.getId().equals(alertId))
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            log.error("❌ Erro ao buscar alerta - alertId: {}, error: {}", 
                      alertId, e.getMessage());
            return null;
        }
    }

    /**
     * Seleciona notificador baseado no canal
     */
    private AlertNotifier selectNotifier(NotificationChannel channel) {
        return notifiers.stream()
            .filter(notifier -> notifier.getChannel() == channel)
            .findFirst()
            .orElse(null);
    }

    /**
     * US#59 - Persiste registro no histórico de notificações
     * 
     * Nunca lança exceções para não quebrar o fluxo principal
     */
    private void persistHistory(
            RiskMetricAlertResponse alert,
            NotificationChannel channel,
            NotificationStatus status,
            String deliveryReason,
            String recipient) {
        try {
            RiskAlertNotificationHistory history = new RiskAlertNotificationHistory();
            history.setAlertType(alert.getType());
            history.setSeverity(alert.getSeverity());
            history.setChannel(channel);
            history.setStatus(status);
            
            // Contexto de projeto
            if (alert.getProjectContext() != null) {
                history.setProjectId(alert.getProjectContext().getProjectId());
                history.setProjectName(alert.getProjectContext().getProjectName());
            }
            
            // Contexto de regra de negócio
            history.setBusinessRuleId(alert.getBusinessRuleId());
            history.setBusinessRuleName(alert.getBusinessRuleName());
            
            // Resumo da mensagem (truncar para 255 chars)
            String message = alert.getMessage();
            history.setMessageSummary(message != null && message.length() > 255 
                ? message.substring(0, 252) + "..." 
                : message);
            
            history.setDeliveryReason(deliveryReason);
            history.setRecipient(recipient);
            history.setCreatedBy(getCurrentUsername());
            
            historyRepository.save(history);
            
            log.debug("📜 Histórico persistido - alertId: {}, status: {}, reason: {}", 
                     alert.getId(), status, deliveryReason);
            
        } catch (Exception e) {
            // US#59 - Nunca quebrar o fluxo por erro no histórico
            log.error("❌ Erro ao persistir histórico - alertId: {}, error: {}", 
                     alert.getId(), e.getMessage());
        }
    }

    /**
     * Obtém username do contexto de segurança ou retorna SYSTEM
     */
    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    /**
     * DTO de resposta para health check
     */
    public record NotificationHealthResponse(
        String status,
        int availableChannels,
        List<NotificationChannel> supportedChannels,
        Instant timestamp
    ) {}
}
