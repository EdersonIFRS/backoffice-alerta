package com.backoffice.alerta.timeline;

import com.backoffice.alerta.notification.RiskNotification;
import com.backoffice.alerta.repository.RiskDecisionAuditRepository;
import com.backoffice.alerta.repository.RiskDecisionFeedbackRepository;
import com.backoffice.alerta.repository.RiskNotificationRepository;
import com.backoffice.alerta.repository.RiskSlaTrackingRepository;
import com.backoffice.alerta.rules.RiskDecisionAudit;
import com.backoffice.alerta.rules.RiskDecisionFeedback;
import com.backoffice.alerta.sla.RiskSlaTracking;
import com.backoffice.alerta.sla.SlaStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * US#40 - Serviço de orquestração da linha do tempo de decisão
 * 
 * Responsabilidade: Agregar eventos existentes de múltiplas fontes
 * em uma timeline cronológica unificada.
 * 
 * GOVERNANÇA:
 * - Read-only: Não modifica nenhuma entidade
 * - Determinístico: Não chama IA ou APIs externas
 * - Auditável: Não cria novos registros de auditoria
 * - Não persiste dados
 */
@Service
public class ChangeTimelineService {

    private final RiskDecisionAuditRepository auditRepository;
    private final RiskDecisionFeedbackRepository feedbackRepository;
    private final RiskNotificationRepository notificationRepository;
    private final RiskSlaTrackingRepository slaRepository;

    public ChangeTimelineService(RiskDecisionAuditRepository auditRepository,
                                  RiskDecisionFeedbackRepository feedbackRepository,
                                  RiskNotificationRepository notificationRepository,
                                  RiskSlaTrackingRepository slaRepository) {
        this.auditRepository = auditRepository;
        this.feedbackRepository = feedbackRepository;
        this.notificationRepository = notificationRepository;
        this.slaRepository = slaRepository;
    }

    /**
     * Gera timeline completa para um Pull Request
     * 
     * @param pullRequestId ID do Pull Request
     * @return Timeline com todos os eventos ordenados cronologicamente
     */
    public ChangeTimelineResponse generateTimeline(String pullRequestId) {
        // Buscar dados existentes (READ-ONLY)
        List<RiskDecisionAudit> audits = auditRepository.findByPullRequestIdOrderByCreatedAtDesc(pullRequestId);
        List<RiskDecisionFeedback> feedbacks = feedbackRepository.findByPullRequestIdOrderByCreatedAtDesc(pullRequestId);
        
        // Buscar notificações e SLAs relacionadas aos audits
        List<RiskNotification> notifications = new ArrayList<>();
        List<RiskSlaTracking> slas = new ArrayList<>();
        
        for (RiskDecisionAudit audit : audits) {
            notifications.addAll(notificationRepository.findByAuditIdOrderByCreatedAtDesc(audit.getId()));
            slas.addAll(slaRepository.findByAuditIdOrderByCreatedAtDesc(audit.getId()));
        }

        // Converter entidades em eventos da timeline
        List<TimelineEventResponse> events = new ArrayList<>();
        events.addAll(convertAuditsToEvents(audits));
        events.addAll(convertFeedbacksToEvents(feedbacks));
        events.addAll(convertNotificationsToEvents(notifications));
        events.addAll(convertSlasToEvents(slas));

        // Ordenar por data (ASC - mais antigo primeiro)
        events.sort(Comparator.comparing(TimelineEventResponse::createdAt));

        // Determinar decisão final e risco geral
        String finalDecision = audits.isEmpty() ? "PENDING" : audits.get(0).getFinalDecision().name();
        String overallRiskLevel = audits.isEmpty() ? "UNKNOWN" : audits.get(0).getRiskLevel().name();
        String environment = audits.isEmpty() ? "UNKNOWN" : audits.get(0).getEnvironment().name();
        
        // Requer atenção executiva se: CRITICAL ou REJECTED ou SLA_BREACHED
        boolean requiresExecutiveAttention = audits.stream()
            .anyMatch(a -> "CRITICAL".equals(a.getRiskLevel().name()) || 
                          "REJECTED".equals(a.getFinalDecision().name())) ||
            slas.stream().anyMatch(s -> s.getStatus() == SlaStatus.BREACHED);

        return new ChangeTimelineResponse(
            pullRequestId,
            environment,
            finalDecision,
            overallRiskLevel,
            requiresExecutiveAttention,
            events
        );
    }

    /**
     * Converte auditorias em eventos de timeline
     */
    private List<TimelineEventResponse> convertAuditsToEvents(List<RiskDecisionAudit> audits) {
        return audits.stream()
            .flatMap(audit -> Stream.of(
                // Evento de DECISÃO
                new TimelineEventResponse(
                    audit.getId().toString(),
                    TimelineEventType.DECISION,
                    "Decisão: " + formatDecision(audit.getFinalDecision().name()),
                    String.format("Decisão %s para mudança em %s. Risco: %s. Política aplicada.",
                        formatDecision(audit.getFinalDecision().name()),
                        audit.getEnvironment().name(),
                        audit.getRiskLevel().name()),
                    audit.getCreatedAt(),
                    "USER",
                    mapRiskToSeverity(audit.getRiskLevel().name()),
                    audit.getId().toString(),
                    Map.of(
                        "riskLevel", audit.getRiskLevel().name(),
                        "environment", audit.getEnvironment().name(),
                        "decision", audit.getFinalDecision().name()
                    )
                ),
                // Evento de AUDITORIA
                new TimelineEventResponse(
                    audit.getId().toString() + "_audit",
                    TimelineEventType.AUDIT,
                    "Auditoria Registrada",
                    String.format("Registro de auditoria criado. Timestamp: %s",
                        audit.getCreatedAt()),
                    audit.getCreatedAt(),
                    "SYSTEM",
                    "INFO",
                    audit.getId().toString(),
                    Map.of(
                        "pullRequestId", audit.getPullRequestId()
                    )
                )
            ))
            .collect(Collectors.toList());
    }

    /**
     * Converte feedbacks em eventos de timeline
     */
    private List<TimelineEventResponse> convertFeedbacksToEvents(List<RiskDecisionFeedback> feedbacks) {
        return feedbacks.stream()
            .map(feedback -> new TimelineEventResponse(
                feedback.getId().toString(),
                TimelineEventType.FEEDBACK,
                "Feedback Pós-Deploy: " + (feedback.getOutcome().name().contains("SUCCESS") ? "✓ Sucesso" : "✗ Problema"),
                String.format("Feedback recebido de %s. Resultado: %s. Comentário: %s",
                    feedback.getAuthor(),
                    feedback.getOutcome().name(),
                    feedback.getComments() != null ? feedback.getComments() : "Sem comentários"),
                feedback.getCreatedAt(),
                "USER",
                feedback.getOutcome().name().contains("SUCCESS") ? "INFO" : "WARNING",
                feedback.getAuditId().toString(),
                Map.of(
                    "outcome", feedback.getOutcome().name(),
                    "author", feedback.getAuthor()
                )
            ))
            .collect(Collectors.toList());
    }

    /**
     * Converte notificações em eventos de timeline
     */
    private List<TimelineEventResponse> convertNotificationsToEvents(List<RiskNotification> notifications) {
        return notifications.stream()
            .map(notification -> new TimelineEventResponse(
                notification.getId().toString(),
                TimelineEventType.NOTIFICATION,
                "Notificação: " + notification.getNotificationTrigger().name(),
                notification.getMessage(),
                notification.getCreatedAt(),
                "SYSTEM",
                notification.getSeverity().name(),
                notification.getAuditId().toString(),
                Map.of(
                    "severity", notification.getSeverity().name(),
                    "teamName", notification.getTeamName(),
                    "channel", notification.getChannel().name()
                )
            ))
            .collect(Collectors.toList());
    }

    /**
     * Converte SLAs em eventos de timeline
     */
    private List<TimelineEventResponse> convertSlasToEvents(List<RiskSlaTracking> slas) {
        return slas.stream()
            .flatMap(sla -> {
                List<TimelineEventResponse> events = new ArrayList<>();
                
                // Evento de criação do SLA
                events.add(new TimelineEventResponse(
                    sla.getId().toString(),
                    TimelineEventType.SLA_CREATED,
                    "SLA Criado",
                    String.format("SLA definido com deadline: %s. Nível de escalação: %s",
                        sla.getSlaDeadline(),
                        sla.getCurrentLevel().name()),
                    sla.getCreatedAt(),
                    "SYSTEM",
                    "INFO",
                    sla.getAuditId().toString(),
                    Map.of(
                        "deadline", sla.getSlaDeadline().toString(),
                        "escalationLevel", sla.getCurrentLevel().name(),
                        "status", sla.getStatus().name()
                    )
                ));
                
                // Evento de escalonamento (se SLA vencido)
                if (sla.getStatus() == SlaStatus.BREACHED) {
                    events.add(new TimelineEventResponse(
                        sla.getId().toString() + "_escalated",
                        TimelineEventType.SLA_ESCALATED,
                        "⚠️ SLA Vencido - Escalonamento",
                        String.format("SLA ultrapassou o deadline. Nível: %s",
                            sla.getCurrentLevel().name()),
                        sla.getSlaDeadline(), // Usa deadline como timestamp do escalonamento
                        "SYSTEM",
                        "CRITICAL",
                        sla.getId().toString(),
                        Map.of(
                            "deadline", sla.getSlaDeadline().toString(),
                            "escalationLevel", sla.getCurrentLevel().name(),
                            "status", "BREACHED"
                        )
                    ));
                }
                
                return events.stream();
            })
            .collect(Collectors.toList());
    }

    /**
     * Mapeia nível de risco para severidade
     */
    private String mapRiskToSeverity(String riskLevel) {
        return switch (riskLevel) {
            case "CRITICAL", "HIGH" -> "CRITICAL";
            case "MEDIUM" -> "WARNING";
            default -> "INFO";
        };
    }

    /**
     * Formata decisão para exibição
     */
    private String formatDecision(String decision) {
        return switch (decision) {
            case "APPROVED" -> "Aprovada";
            case "REJECTED" -> "Rejeitada";
            case "APPROVED_WITH_CONDITIONS" -> "Aprovada com Condições";
            default -> decision;
        };
    }
}
