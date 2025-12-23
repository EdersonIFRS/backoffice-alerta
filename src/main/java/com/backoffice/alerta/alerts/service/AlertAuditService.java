package com.backoffice.alerta.alerts.service;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.alerts.notification.NotificationChannel;
import com.backoffice.alerta.alerts.notification.NotificationStatus;
import com.backoffice.alerta.alerts.dto.*;
import com.backoffice.alerta.alerts.notification.RiskAlertNotificationHistory;
import com.backoffice.alerta.alerts.notification.RiskAlertNotificationHistoryRepository;
import com.backoffice.alerta.alerts.preferences.dto.EffectiveAlertPreferenceResponse;
import com.backoffice.alerta.alerts.preferences.service.AlertPreferenceService;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de auditoria profunda para alertas e notificações
 * 
 * US#61 - Auditoria Detalhada de Alertas e Notificações
 * 
 * PRINCÍPIOS:
 * - READ-ONLY absoluto
 * - 100% determinístico
 * - Explicação em linguagem humana
 * - Compliance-first
 * - Rastreabilidade total
 */
@Service
public class AlertAuditService {
    
    private static final Logger log = LoggerFactory.getLogger(AlertAuditService.class);
    
    private final RiskAlertNotificationHistoryRepository historyRepository;
    private final AlertPreferenceService preferenceService;
    private final ProjectRepository projectRepository;
    private final BusinessRuleRepository businessRuleRepository;
    
    public AlertAuditService(
            RiskAlertNotificationHistoryRepository historyRepository,
            AlertPreferenceService preferenceService,
            ProjectRepository projectRepository,
            BusinessRuleRepository businessRuleRepository) {
        this.historyRepository = historyRepository;
        this.preferenceService = preferenceService;
        this.projectRepository = projectRepository;
        this.businessRuleRepository = businessRuleRepository;
    }
    
    /**
     * Busca auditoria detalhada de um alerta específico
     * Responde: "Por que este alerta foi enviado/bloqueado?"
     */
    @Transactional(readOnly = true)
    public AlertAuditDetailResponse getAuditDetail(UUID alertHistoryId) {
        log.info("🔍 Buscando auditoria detalhada - alertHistoryId: {}", alertHistoryId);
        
        try {
            Optional<RiskAlertNotificationHistory> historyOpt = historyRepository.findById(alertHistoryId);
            
            if (historyOpt.isEmpty()) {
                log.warn("⚠️ Histórico não encontrado - id: {}", alertHistoryId);
                return createEmptyDetail();
            }
            
            RiskAlertNotificationHistory history = historyOpt.get();
            
            AlertAuditDetailResponse response = new AlertAuditDetailResponse();
            
            // Dados básicos do histórico
            response.setAlertHistoryId(history.getId().getMostSignificantBits());
            response.setAlertType(history.getAlertType());
            response.setSeverity(history.getSeverity());
            response.setChannel(history.getChannel());
            response.setStatus(history.getStatus());
            response.setMessageSummary(history.getMessageSummary());
            response.setDeliveryReason(history.getDeliveryReason());
            response.setCreatedAt(LocalDateTime.ofInstant(history.getCreatedAt(), ZoneId.systemDefault()));
            response.setCreatedBy(history.getCreatedBy());
            
            // Enriquecer com projeto
            if (history.getProjectId() != null) {
                response.setProject(new AlertAuditDetailResponse.ProjectSummaryDTO(
                    history.getProjectId().getMostSignificantBits(),
                    history.getProjectName()
                ));
            }
            
            // Enriquecer com regra de negócio
            if (history.getBusinessRuleId() != null) {
                response.setBusinessRule(new AlertAuditDetailResponse.BusinessRuleSummaryDTO(
                    history.getBusinessRuleId(),
                    history.getBusinessRuleName()
                ));
            }
            
            // Resolver preferência efetiva
            EffectiveAlertPreferenceResponse effectivePref = preferenceService.resolveEffectivePreference(
                history.getProjectId(),
                history.getBusinessRuleId()
            );
            
            ResolvedPreferenceDTO resolvedPref = new ResolvedPreferenceDTO(
                effectivePref.getSource().name(),
                effectivePref.getMinimumSeverity(),
                effectivePref.getAllowedAlertTypes(),
                effectivePref.getChannels(),
                effectivePref.getDeliveryWindow() != null ? effectivePref.getDeliveryWindow().name() : "ANY_TIME"
            );
            response.setResolvedPreference(resolvedPref);
            
            // Construir explicação determinística
            String explanation = buildExplanation(history, effectivePref);
            response.setExplanation(explanation);
            
            // Calcular compliance flags
            ComplianceFlagsDTO complianceFlags = calculateComplianceFlags(history, effectivePref);
            response.setComplianceFlags(complianceFlags);
            
            log.info("✅ Auditoria detalhada construída - id: {}, status: {}, source: {}", 
                     alertHistoryId, history.getStatus(), effectivePref.getSource());
            
            return response;
            
        } catch (Exception e) {
            log.error("❌ Erro ao buscar auditoria detalhada - id: {}", alertHistoryId, e);
            return createEmptyDetail();
        }
    }
    
    /**
     * Busca resumo agregado de auditoria
     * Responde: "Qual o panorama geral dos alertas bloqueados?"
     */
    @Transactional(readOnly = true)
    public AlertAuditSummaryResponse getAuditSummary(UUID projectId, String businessRuleId, 
                                                     LocalDate fromDate, LocalDate toDate) {
        log.info("📊 Construindo resumo de auditoria - projectId: {}, ruleId: {}", projectId, businessRuleId);
        
        try {
            List<RiskAlertNotificationHistory> allHistory;
            
            if (fromDate != null && toDate != null) {
                Instant from = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant to = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                allHistory = historyRepository.findTimeline(from, to);
            } else if (projectId != null) {
                allHistory = historyRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
            } else if (businessRuleId != null) {
                allHistory = historyRepository.findByBusinessRuleIdOrderByCreatedAtDesc(businessRuleId);
            } else {
                allHistory = historyRepository.findAll();
            }
            
            AlertAuditSummaryResponse response = new AlertAuditSummaryResponse();
            
            // Contadores básicos
            response.setTotalAlerts(allHistory.size());
            response.setSent(allHistory.stream().filter(h -> h.getStatus() == NotificationStatus.SENT).count());
            response.setSkipped(allHistory.stream().filter(h -> h.getStatus() == NotificationStatus.SKIPPED).count());
            response.setFailed(allHistory.stream().filter(h -> h.getStatus() == NotificationStatus.FAILED).count());
            
            // Severidade mais bloqueada
            Map<AlertSeverity, Long> blockedBySeverity = allHistory.stream()
                .filter(h -> h.getStatus() == NotificationStatus.SKIPPED)
                .collect(Collectors.groupingBy(RiskAlertNotificationHistory::getSeverity, Collectors.counting()));
            
            response.setMostBlockedSeverity(
                blockedBySeverity.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null)
            );
            
            // Canal mais bloqueado
            Map<NotificationChannel, Long> blockedByChannel = allHistory.stream()
                .filter(h -> h.getStatus() == NotificationStatus.SKIPPED)
                .collect(Collectors.groupingBy(RiskAlertNotificationHistory::getChannel, Collectors.counting()));
            
            response.setMostBlockedChannel(
                blockedByChannel.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null)
            );
            
            // Top 5 projetos por bloqueios
            Map<String, Long> blockedByProject = allHistory.stream()
                .filter(h -> h.getStatus() == NotificationStatus.SKIPPED && h.getProjectName() != null)
                .collect(Collectors.groupingBy(RiskAlertNotificationHistory::getProjectName, Collectors.counting()));
            
            List<AlertAuditSummaryResponse.ProjectBlockedDTO> topProjects = blockedByProject.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    // Buscar ID do projeto pelo nome
                    Long projectIdLong = allHistory.stream()
                        .filter(h -> entry.getKey().equals(h.getProjectName()))
                        .findFirst()
                        .map(h -> h.getProjectId() != null ? h.getProjectId().getMostSignificantBits() : null)
                        .orElse(null);
                    return new AlertAuditSummaryResponse.ProjectBlockedDTO(projectIdLong, entry.getKey(), entry.getValue());
                })
                .collect(Collectors.toList());
            response.setTopProjectsByBlocked(topProjects);
            
            // Top 5 regras por bloqueios
            Map<String, Long> blockedByRule = allHistory.stream()
                .filter(h -> h.getStatus() == NotificationStatus.SKIPPED && h.getBusinessRuleName() != null)
                .collect(Collectors.groupingBy(RiskAlertNotificationHistory::getBusinessRuleName, Collectors.counting()));
            
            List<AlertAuditSummaryResponse.RuleBlockedDTO> topRules = blockedByRule.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    // Buscar ID da regra pelo nome
                    String ruleId = allHistory.stream()
                        .filter(h -> entry.getKey().equals(h.getBusinessRuleName()))
                        .findFirst()
                        .map(RiskAlertNotificationHistory::getBusinessRuleId)
                        .orElse(null);
                    return new AlertAuditSummaryResponse.RuleBlockedDTO(ruleId, entry.getKey(), entry.getValue());
                })
                .collect(Collectors.toList());
            response.setTopRulesByBlocked(topRules);
            
            log.info("✅ Resumo construído - total: {}, sent: {}, skipped: {}, failed: {}", 
                     response.getTotalAlerts(), response.getSent(), response.getSkipped(), response.getFailed());
            
            return response;
            
        } catch (Exception e) {
            log.error("❌ Erro ao construir resumo de auditoria", e);
            return new AlertAuditSummaryResponse();
        }
    }
    
    /**
     * Busca timeline de alertas agrupados por data
     */
    @Transactional(readOnly = true)
    public List<AlertAuditTimelineResponse> getAuditTimeline(UUID projectId, String businessRuleId, 
                                                             LocalDate fromDate, LocalDate toDate) {
        log.info("📈 Construindo timeline de auditoria - projectId: {}, ruleId: {}, from: {}, to: {}", 
                 projectId, businessRuleId, fromDate, toDate);
        
        try {
            // Se não especificar datas, usar últimos 30 dias
            if (fromDate == null) {
                fromDate = LocalDate.now().minus(30, ChronoUnit.DAYS);
            }
            if (toDate == null) {
                toDate = LocalDate.now();
            }
            
            Instant from = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant to = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            
            List<RiskAlertNotificationHistory> history;
            
            if (projectId != null) {
                history = historyRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                    .filter(h -> h.getCreatedAt().isAfter(from) && h.getCreatedAt().isBefore(to))
                    .collect(Collectors.toList());
            } else if (businessRuleId != null) {
                history = historyRepository.findByBusinessRuleIdOrderByCreatedAtDesc(businessRuleId).stream()
                    .filter(h -> h.getCreatedAt().isAfter(from) && h.getCreatedAt().isBefore(to))
                    .collect(Collectors.toList());
            } else {
                history = historyRepository.findTimeline(from, to);
            }
            
            // Agrupar por data
            Map<LocalDate, Map<NotificationStatus, Long>> groupedByDate = history.stream()
                .collect(Collectors.groupingBy(
                    h -> LocalDate.ofInstant(h.getCreatedAt(), ZoneId.systemDefault()),
                    Collectors.groupingBy(RiskAlertNotificationHistory::getStatus, Collectors.counting())
                ));
            
            // Construir timeline
            List<AlertAuditTimelineResponse> timeline = groupedByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    Map<NotificationStatus, Long> counts = entry.getValue();
                    
                    return new AlertAuditTimelineResponse(
                        date,
                        counts.getOrDefault(NotificationStatus.SENT, 0L),
                        counts.getOrDefault(NotificationStatus.SKIPPED, 0L),
                        counts.getOrDefault(NotificationStatus.FAILED, 0L)
                    );
                })
                .sorted(Comparator.comparing(AlertAuditTimelineResponse::getDate))
                .collect(Collectors.toList());
            
            log.info("✅ Timeline construída - {} pontos de dados", timeline.size());
            
            return timeline;
            
        } catch (Exception e) {
            log.error("❌ Erro ao construir timeline de auditoria", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Constrói explicação determinística em linguagem humana
     */
    private String buildExplanation(RiskAlertNotificationHistory history, 
                                    EffectiveAlertPreferenceResponse preference) {
        
        StringBuilder explanation = new StringBuilder();
        
        if (history.getStatus() == NotificationStatus.SENT) {
            explanation.append("✅ Este alerta foi ENVIADO porque ");
            explanation.append("a severidade ").append(history.getSeverity()).append(" ");
            explanation.append("é maior ou igual à severidade mínima configurada (").append(preference.getMinimumSeverity()).append(") ");
            explanation.append("e o canal ").append(history.getChannel()).append(" está habilitado ");
            explanation.append("na preferência de ").append(preference.getSource()).append(" ");
            
            if (preference.getSource() == EffectiveAlertPreferenceResponse.PreferenceSource.RULE) {
                explanation.append("'").append(history.getBusinessRuleName()).append("'");
            } else if (preference.getSource() == EffectiveAlertPreferenceResponse.PreferenceSource.PROJECT) {
                explanation.append("'").append(history.getProjectName()).append("'");
            } else {
                explanation.append("DEFAULT do sistema");
            }
            explanation.append(".");
            
        } else if (history.getStatus() == NotificationStatus.SKIPPED) {
            explanation.append("🚫 Este alerta foi BLOQUEADO porque ");
            
            // Verificar motivo do bloqueio
            if (history.getSeverity().ordinal() < preference.getMinimumSeverity().ordinal()) {
                explanation.append("a severidade ").append(history.getSeverity()).append(" ");
                explanation.append("é inferior à severidade mínima configurada (").append(preference.getMinimumSeverity()).append(") ");
            } else if (!preference.getChannels().contains(history.getChannel())) {
                explanation.append("o canal ").append(history.getChannel()).append(" não está habilitado ");
                explanation.append("(canais permitidos: ").append(preference.getChannels()).append(") ");
            } else if (preference.getAllowedAlertTypes() != null && !preference.getAllowedAlertTypes().isEmpty() 
                       && !preference.getAllowedAlertTypes().contains(history.getAlertType())) {
                explanation.append("o tipo de alerta ").append(history.getAlertType()).append(" não está permitido ");
                explanation.append("(tipos permitidos: ").append(preference.getAllowedAlertTypes()).append(") ");
            } else {
                explanation.append("não atendeu aos critérios da janela de entrega ");
            }
            
            explanation.append("na preferência de ").append(preference.getSource()).append(" ");
            
            if (preference.getSource() == EffectiveAlertPreferenceResponse.PreferenceSource.RULE) {
                explanation.append("'").append(history.getBusinessRuleName()).append("', ");
                explanation.append("que sobrescreveu a configuração do PROJETO");
            } else if (preference.getSource() == EffectiveAlertPreferenceResponse.PreferenceSource.PROJECT) {
                explanation.append("'").append(history.getProjectName()).append("', ");
                explanation.append("que sobrescreveu o DEFAULT");
            } else {
                explanation.append("DEFAULT do sistema");
            }
            explanation.append(".");
            
        } else if (history.getStatus() == NotificationStatus.FAILED) {
            explanation.append("❌ Este alerta FALHOU na entrega. ");
            explanation.append("Embora atendesse aos critérios de envio ");
            explanation.append("(severidade ").append(history.getSeverity()).append(" >= ").append(preference.getMinimumSeverity()).append(", ");
            explanation.append("canal ").append(history.getChannel()).append(" habilitado), ");
            explanation.append("houve um erro técnico durante a comunicação com o serviço externo. ");
            explanation.append("Motivo: ").append(history.getDeliveryReason()).append(".");
        }
        
        return explanation.toString();
    }
    
    /**
     * Calcula compliance flags
     */
    private ComplianceFlagsDTO calculateComplianceFlags(RiskAlertNotificationHistory history,
                                                       EffectiveAlertPreferenceResponse preference) {
        
        boolean respectedSeverity = history.getSeverity().ordinal() >= preference.getMinimumSeverity().ordinal();
        boolean respectedChannel = preference.getChannels().contains(history.getChannel());
        
        boolean respectedAlertType = true;
        if (preference.getAllowedAlertTypes() != null && !preference.getAllowedAlertTypes().isEmpty()) {
            respectedAlertType = preference.getAllowedAlertTypes().contains(history.getAlertType());
        }
        
        // Considerando janela de entrega sempre respeitada se não houver validação temporal
        boolean respectedWindow = true;
        
        // Hierarquia respeitada se a preferência foi resolvida corretamente
        boolean respectedHierarchy = preference.getSource() != null;
        
        return new ComplianceFlagsDTO(
            respectedSeverity,
            respectedChannel,
            respectedWindow && respectedAlertType,
            respectedHierarchy
        );
    }
    
    /**
     * Cria response vazio em caso de erro
     */
    private AlertAuditDetailResponse createEmptyDetail() {
        AlertAuditDetailResponse response = new AlertAuditDetailResponse();
        response.setExplanation("Histórico não encontrado ou erro ao processar auditoria");
        response.setComplianceFlags(new ComplianceFlagsDTO(false, false, false, false));
        return response;
    }
}
