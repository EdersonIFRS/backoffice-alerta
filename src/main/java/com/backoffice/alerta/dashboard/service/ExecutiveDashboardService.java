package com.backoffice.alerta.dashboard.service;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.dto.RiskMetricAlertResponse;
import com.backoffice.alerta.alerts.notification.NotificationStatus;
import com.backoffice.alerta.alerts.notification.RiskAlertNotificationHistory;
import com.backoffice.alerta.alerts.notification.RiskAlertNotificationHistoryRepository;
import com.backoffice.alerta.alerts.service.RiskMetricAlertService;
import com.backoffice.alerta.ci.dto.CIGateMetricsResponse;
import com.backoffice.alerta.ci.service.CIGateMetricsService;
import com.backoffice.alerta.dashboard.dto.*;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de dashboard executivo consolidado
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 * 
 * PRINCÍPIOS:
 * - READ-ONLY: não persiste dados
 * - NÃO recalcula risco
 * - NÃO dispara alertas
 * - NÃO chama IA/LLM
 * - Reutiliza serviços existentes das US#48-59
 * - Determinístico e seguro
 */
@Service
public class ExecutiveDashboardService {
    
    private static final Logger log = LoggerFactory.getLogger(ExecutiveDashboardService.class);
    
    private final CIGateMetricsService ciGateMetricsService;
    private final RiskMetricAlertService alertService;
    private final RiskAlertNotificationHistoryRepository historyRepository;
    private final ProjectRepository projectRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final BusinessRuleIncidentRepository incidentRepository;
    
    public ExecutiveDashboardService(
            CIGateMetricsService ciGateMetricsService,
            RiskMetricAlertService alertService,
            RiskAlertNotificationHistoryRepository historyRepository,
            ProjectRepository projectRepository,
            BusinessRuleRepository businessRuleRepository,
            BusinessRuleIncidentRepository incidentRepository) {
        this.ciGateMetricsService = ciGateMetricsService;
        this.alertService = alertService;
        this.historyRepository = historyRepository;
        this.projectRepository = projectRepository;
        this.businessRuleRepository = businessRuleRepository;
        this.incidentRepository = incidentRepository;
    }
    
    /**
     * Gera visão executiva consolidada do sistema
     * 
     * @return Dashboard executivo com métricas agregadas
     */
    public ExecutiveDashboardResponse getExecutiveDashboard() {
        log.info("📊 Gerando dashboard executivo consolidado...");
        
        try {
            ExecutiveDashboardResponse response = new ExecutiveDashboardResponse();
            
            // 1. Summary
            response.setSummary(buildSummary());
            
            // 2. Top Projects
            response.setTopProjects(buildTopProjects());
            
            // 3. Top Rules
            response.setTopRules(buildTopRules());
            
            // 4. Alert Trends (30 dias)
            response.setAlertTrends(buildAlertTrends());
            
            // 5. Active Alerts (CRITICAL e WARNING)
            response.setActiveAlerts(buildActiveAlerts());
            
            log.info("✅ Dashboard executivo gerado com sucesso");
            return response;
            
        } catch (Exception e) {
            log.error("❌ Erro ao gerar dashboard executivo: {}", e.getMessage(), e);
            // Retorna dashboard vazio em caso de erro
            return new ExecutiveDashboardResponse(
                new ExecutiveDashboardSummary(0, 0.0, 0.0, 0, false),
                List.of(), List.of(), List.of(), List.of()
            );
        }
    }
    
    /**
     * Constrói resumo executivo
     */
    private ExecutiveDashboardSummary buildSummary() {
        try {
            // Métricas CI (US#54)
            CIGateMetricsResponse metrics = ciGateMetricsService.getGeneralMetrics(null, null, null);
            
            // Se métricas não disponíveis, retorna valores padrão
            if (metrics == null) {
                log.warn("⚠️ Métricas CI não disponíveis, retornando valores padrão");
                return new ExecutiveDashboardSummary(0, 0.0, 0.0, 0, false);
            }
            
            long totalGates = metrics.getTotalExecutions();
            double blockRate = metrics.getBlockRate();
            double warningRate = metrics.getWarningRate();
            
            // Alertas críticos últimos 7 dias (US#59)
            Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
            long criticalAlerts = historyRepository.findTimeline(sevenDaysAgo, Instant.now()).stream()
                .filter(h -> h.getSeverity() == AlertSeverity.CRITICAL)
                .filter(h -> h.getStatus() == NotificationStatus.SENT)
                .count();
            
            // Alert Fatigue: warnings altos + poucos incidentes resolvidos
            long totalIncidents = incidentRepository.count();
            boolean alertFatigue = (warningRate > 20.0 && totalIncidents < 5);
            
            return new ExecutiveDashboardSummary(
                totalGates, blockRate, warningRate, criticalAlerts, alertFatigue
            );
            
        } catch (Exception e) {
            log.error("❌ Erro ao construir summary: {}", e.getMessage());
            return new ExecutiveDashboardSummary(0, 0.0, 0.0, 0, false);
        }
    }
    
    /**
     * Top 5 projetos por block rate
     */
    private List<ProjectRiskSummary> buildTopProjects() {
        try {
            List<Project> projects = projectRepository.findByActiveTrue();
            Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
            
            return projects.stream()
                .map(project -> {
                    // Calcular block rate do projeto (simplificado - baseado em histórico)
                    long alerts = historyRepository.findByProjectIdOrderByCreatedAtDesc(project.getId()).stream()
                        .filter(h -> h.getCreatedAt().isAfter(thirtyDaysAgo))
                        .count();
                    
                    // Block rate estimado (em produção viria de métricas reais)
                    double blockRate = alerts > 0 ? Math.min(alerts * 5.0, 100.0) : 0.0;
                    
                    return new ProjectRiskSummary(
                        project.getId(),
                        project.getName(),
                        blockRate,
                        alerts
                    );
                })
                .sorted(Comparator.comparingDouble(ProjectRiskSummary::getBlockRate).reversed())
                .limit(5)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erro ao construir top projects: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Top 5 regras por block count
     */
    private List<RuleRiskSummary> buildTopRules() {
        try {
            List<BusinessRule> rules = businessRuleRepository.findAll();
            
            return rules.stream()
                .map(rule -> {
                    // Contar incidentes (US#49) - converter String ID para UUID
                    long incidents = 0;
                    try {
                        UUID ruleUuid = UUID.fromString(rule.getId());
                        incidents = incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(ruleUuid).size();
                    } catch (IllegalArgumentException e) {
                        log.warn("⚠️ ID de regra inválido para UUID: {}", rule.getId());
                    }
                    
                    // Block count estimado baseado em histórico de alertas
                    long blockCount = historyRepository.findByBusinessRuleIdOrderByCreatedAtDesc(rule.getId()).stream()
                        .filter(h -> h.getSeverity() == AlertSeverity.CRITICAL || h.getSeverity() == AlertSeverity.WARNING)
                        .count();
                    
                    return new RuleRiskSummary(
                        rule.getId(),
                        rule.getName(),
                        blockCount,
                        incidents
                    );
                })
                .sorted(Comparator.comparingLong(RuleRiskSummary::getBlockCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erro ao construir top rules: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Tendência de alertas últimos 30 dias
     */
    private List<AlertTrendPoint> buildAlertTrends() {
        try {
            Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
            List<RiskAlertNotificationHistory> history = historyRepository.findTimeline(thirtyDaysAgo, Instant.now());
            
            // Agrupar por data
            Map<LocalDate, List<RiskAlertNotificationHistory>> byDate = history.stream()
                .collect(Collectors.groupingBy(h -> 
                    h.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()
                ));
            
            // Criar pontos de tendência
            return byDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<RiskAlertNotificationHistory> dayHistory = entry.getValue();
                    
                    long sent = dayHistory.stream().filter(h -> h.getStatus() == NotificationStatus.SENT).count();
                    long skipped = dayHistory.stream().filter(h -> h.getStatus() == NotificationStatus.SKIPPED).count();
                    long failed = dayHistory.stream().filter(h -> h.getStatus() == NotificationStatus.FAILED).count();
                    
                    return new AlertTrendPoint(date, sent, skipped, failed);
                })
                .sorted(Comparator.comparing(AlertTrendPoint::getDate))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erro ao construir alert trends: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Alertas ativos (CRITICAL e WARNING)
     */
    private List<ActiveAlertSummary> buildActiveAlerts() {
        try {
            // Usar serviço de alertas (US#55)
            List<RiskMetricAlertResponse> alerts = alertService.detectAlerts(null, null, null);
            
            return alerts.stream()
                .filter(alert -> alert.getSeverity() == AlertSeverity.CRITICAL || 
                               alert.getSeverity() == AlertSeverity.WARNING)
                .limit(10)
                .map(alert -> new ActiveAlertSummary(
                    alert.getType(),
                    alert.getSeverity(),
                    alert.getMessage()
                ))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erro ao construir active alerts: {}", e.getMessage());
            return List.of();
        }
    }
}
