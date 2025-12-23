package com.backoffice.alerta.alerts.service;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.alerts.RiskMetricAlert;
import com.backoffice.alerta.alerts.dto.RiskMetricAlertResponse;
import com.backoffice.alerta.alerts.dto.RiskMetricAlertSummaryResponse;
import com.backoffice.alerta.ci.dto.CIGateMetricsResponse;
import com.backoffice.alerta.ci.dto.CIGateProjectMetrics;
import com.backoffice.alerta.ci.dto.CIGateRuleMetrics;
import com.backoffice.alerta.ci.dto.CIGateTimelinePoint;
import com.backoffice.alerta.ci.service.CIGateMetricsService;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.rag.OwnershipSummary;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleOwnership;
import com.backoffice.alerta.rules.BusinessRuleOwnershipRepository;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para geração de alertas inteligentes baseados em métricas do Gate de Risco
 * 
 * US#55 - Alertas Inteligentes Baseados em Métricas
 * 
 * PRINCÍPIOS:
 * - READ-ONLY absoluto: apenas lê métricas existentes
 * - Determinístico: mesma métrica → mesmo alerta
 * - SEM IA/ML: regras baseadas em thresholds
 * - SEM side-effects: não persiste, não notifica
 * - Reutiliza dados da US#54 (CIGateMetricsService)
 * - Enriquece com ownership (US#26)
 */
@Service
public class RiskMetricAlertService {

    private static final Logger log = LoggerFactory.getLogger(RiskMetricAlertService.class);

    // Thresholds para detecção
    private static final double HIGH_BLOCK_RATE_THRESHOLD = 30.0;
    private static final int RULE_OVERBLOCKING_THRESHOLD = 5;
    private static final double WARNING_SPIKE_THRESHOLD = 15.0;
    private static final int NEGATIVE_TREND_DAYS = 3;
    private static final double SYSTEM_DEGRADATION_THRESHOLD = 25.0;

    private final CIGateMetricsService metricsService;
    private final ProjectRepository projectRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final BusinessRuleOwnershipRepository ownershipRepository;
    private final BusinessRuleIncidentRepository incidentRepository;

    public RiskMetricAlertService(
            CIGateMetricsService metricsService,
            ProjectRepository projectRepository,
            BusinessRuleRepository businessRuleRepository,
            BusinessRuleOwnershipRepository ownershipRepository,
            BusinessRuleIncidentRepository incidentRepository) {
        this.metricsService = metricsService;
        this.projectRepository = projectRepository;
        this.businessRuleRepository = businessRuleRepository;
        this.ownershipRepository = ownershipRepository;
        this.incidentRepository = incidentRepository;
    }

    /**
     * Detecta todos os alertas ativos no sistema
     * 
     * @param projectId ID do projeto (opcional, null = global)
     * @param from Data inicial (opcional)
     * @param to Data final (opcional)
     * @return Lista de alertas detectados (nunca null)
     */
    public List<RiskMetricAlertResponse> detectAlerts(UUID projectId, LocalDate from, LocalDate to) {
        log.info("🚨 Iniciando detecção de alertas - projectId: {}, from: {}, to: {}", 
                 projectId, from, to);

        List<RiskMetricAlert> alerts = new ArrayList<>();

        // 1. Detectar alertas por projeto
        alerts.addAll(detectHighBlockRateProjects(from, to));

        // 2. Detectar alertas por regra de negócio
        alerts.addAll(detectRuleOverblocking());

        // 3. Detectar spike de warnings
        alerts.addAll(detectWarningSpike(from, to));

        // 4. Detectar tendências negativas
        alerts.addAll(detectNegativeTrend(from, to));

        // 5. Detectar degradação sistêmica
        alerts.addAll(detectSystemDegradation(from, to));

        // 6. Detectar possíveis falsos positivos
        alerts.addAll(detectPotentialFalsePositives());

        // Filtrar por projeto se especificado
        if (projectId != null) {
            alerts = alerts.stream()
                .filter(alert -> projectId.equals(alert.getProjectId()))
                .collect(Collectors.toList());
            log.info("📊 Alertas filtrados por projeto {}: {} alertas", projectId, alerts.size());
        }

        log.info("✅ Detecção concluída - Total de alertas: {}", alerts.size());

        // Enriquecer alertas com informações adicionais
        return alerts.stream()
            .map(this::enrichAlert)
            .collect(Collectors.toList());
    }

    /**
     * Retorna resumo executivo de todos os alertas
     */
    public RiskMetricAlertSummaryResponse getAlertSummary(LocalDate from, LocalDate to) {
        log.info("📊 Gerando resumo de alertas - from: {}, to: {}", from, to);

        List<RiskMetricAlertResponse> alerts = detectAlerts(null, from, to);

        int critical = (int) alerts.stream()
            .filter(a -> a.getSeverity() == AlertSeverity.CRITICAL)
            .count();
        
        int warning = (int) alerts.stream()
            .filter(a -> a.getSeverity() == AlertSeverity.WARNING)
            .count();
        
        int info = (int) alerts.stream()
            .filter(a -> a.getSeverity() == AlertSeverity.INFO)
            .count();

        RiskMetricAlertSummaryResponse summary = new RiskMetricAlertSummaryResponse(
            alerts.size(), critical, warning, info
        );

        // Agrupar por tipo
        Map<AlertType, Integer> byType = alerts.stream()
            .collect(Collectors.groupingBy(
                RiskMetricAlertResponse::getType,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        summary.setAlertsByType(byType);

        // Agrupar por projeto
        Map<String, Integer> byProject = alerts.stream()
            .filter(a -> a.getProjectContext() != null && a.getProjectContext().isScoped())
            .collect(Collectors.groupingBy(
                a -> a.getProjectContext().getProjectName(),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        summary.setAlertsByProject(byProject);

        // Definir health status
        String healthStatus = critical > 0 ? "CRITICAL" 
                            : warning > 5 ? "DEGRADED"
                            : warning > 0 ? "WARNING"
                            : "HEALTHY";
        summary.setHealthStatus(healthStatus);

        log.info("✅ Resumo gerado - total: {}, critical: {}, warning: {}, info: {}, health: {}", 
                 alerts.size(), critical, warning, info, healthStatus);

        return summary;
    }

    /**
     * Detecta alertas para um projeto específico
     */
    public List<RiskMetricAlertResponse> detectAlertsForProject(UUID projectId, LocalDate from, LocalDate to) {
        log.info("🔍 Detectando alertas para projeto: {}", projectId);
        return detectAlerts(projectId, from, to);
    }

    /**
     * Detecta alertas para uma regra específica
     */
    public List<RiskMetricAlertResponse> detectAlertsForRule(String ruleId) {
        log.info("🔍 Detectando alertas para regra: {}", ruleId);
        
        List<RiskMetricAlert> alerts = new ArrayList<>();
        
        // Buscar métricas da regra
        List<CIGateRuleMetrics> ruleMetrics = metricsService.getRuleMetrics();
        
        ruleMetrics.stream()
            .filter(rm -> rm.getBusinessRuleId().toString().equals(ruleId))
            .forEach(rm -> {
                // Verificar overblocking
                if (rm.getBlockCount() >= RULE_OVERBLOCKING_THRESHOLD) {
                    alerts.add(RiskMetricAlert.builder()
                        .type(AlertType.RULE_OVERBLOCKING)
                        .severity(AlertSeverity.CRITICAL)
                        .message(String.format(
                            "Regra '%s' bloqueou %d PRs (threshold: %d)",
                            rm.getRuleName(), rm.getBlockCount(), RULE_OVERBLOCKING_THRESHOLD
                        ))
                        .businessRuleId(ruleId)
                        .evidence("blockCount", rm.getBlockCount())
                        .evidence("threshold", RULE_OVERBLOCKING_THRESHOLD)
                        .evidence("warningCount", rm.getWarningCount())
                        .build());
                }
            });

        return alerts.stream()
            .map(this::enrichAlert)
            .collect(Collectors.toList());
    }

    /**
     * Retorna status de saúde do sistema baseado em alertas
     */
    public Map<String, Object> getHealthStatus() {
        log.info("🏥 Verificando health status do sistema");

        RiskMetricAlertSummaryResponse summary = getAlertSummary(
            LocalDate.now().minusDays(7), 
            LocalDate.now()
        );

        Map<String, Object> health = new HashMap<>();
        health.put("status", summary.getHealthStatus());
        health.put("totalAlerts", summary.getTotalAlerts());
        health.put("criticalCount", summary.getCriticalCount());
        health.put("warningCount", summary.getWarningCount());
        health.put("timestamp", new Date());

        return health;
    }

    // ============ REGRAS DE DETECÇÃO ============

    /**
     * REGRA 1: Detecta projetos com blockRate > 30%
     */
    private List<RiskMetricAlert> detectHighBlockRateProjects(LocalDate from, LocalDate to) {
        log.info("📊 Detectando projetos com high block rate...");
        
        List<RiskMetricAlert> alerts = new ArrayList<>();
        List<CIGateProjectMetrics> projectMetrics = metricsService.getProjectMetrics();

        for (CIGateProjectMetrics pm : projectMetrics) {
            if (pm.getBlockRate() > HIGH_BLOCK_RATE_THRESHOLD) {
                alerts.add(RiskMetricAlert.builder()
                    .type(AlertType.HIGH_BLOCK_RATE_PROJECT)
                    .severity(AlertSeverity.CRITICAL)
                    .message(String.format(
                        "Projeto '%s' apresenta taxa de bloqueio crítica (%.1f%%, threshold: %.1f%%)",
                        pm.getProjectName(), pm.getBlockRate(), HIGH_BLOCK_RATE_THRESHOLD
                    ))
                    .projectId(pm.getProjectId())
                    .evidence("blockRate", pm.getBlockRate())
                    .evidence("threshold", HIGH_BLOCK_RATE_THRESHOLD)
                    .evidence("blockedCount", pm.getBlockedCount())
                    .evidence("totalExecutions", pm.getTotalExecutions())
                    .build());
                
                log.warn("⚠️ HIGH_BLOCK_RATE: {} - {:.1f}%", pm.getProjectName(), pm.getBlockRate());
            }
        }

        log.info("✅ High block rate detection: {} alertas", alerts.size());
        return alerts;
    }

    /**
     * REGRA 2: Detecta regras bloqueando ≥5 PRs
     */
    private List<RiskMetricAlert> detectRuleOverblocking() {
        log.info("📊 Detectando regras com overblocking...");
        
        List<RiskMetricAlert> alerts = new ArrayList<>();
        List<CIGateRuleMetrics> ruleMetrics = metricsService.getRuleMetrics();

        for (CIGateRuleMetrics rm : ruleMetrics) {
            if (rm.getBlockCount() >= RULE_OVERBLOCKING_THRESHOLD) {
                alerts.add(RiskMetricAlert.builder()
                    .type(AlertType.RULE_OVERBLOCKING)
                    .severity(AlertSeverity.CRITICAL)
                    .message(String.format(
                        "Regra '%s' bloqueou %d PRs (threshold: %d) - revisar criticidade",
                        rm.getRuleName(), rm.getBlockCount(), RULE_OVERBLOCKING_THRESHOLD
                    ))
                    .businessRuleId(rm.getBusinessRuleId().toString())
                    .evidence("blockCount", rm.getBlockCount())
                    .evidence("threshold", RULE_OVERBLOCKING_THRESHOLD)
                    .evidence("warningCount", rm.getWarningCount())
                    .evidence("criticality", rm.getCriticality())
                    .build());
                
                log.warn("⚠️ RULE_OVERBLOCKING: {} - {} bloqueios", rm.getRuleName(), rm.getBlockCount());
            }
        }

        log.info("✅ Rule overblocking detection: {} alertas", alerts.size());
        return alerts;
    }

    /**
     * REGRA 3: Detecta spike de warnings (>15% acima da média)
     */
    private List<RiskMetricAlert> detectWarningSpike(LocalDate from, LocalDate to) {
        log.info("📊 Detectando spikes de warnings...");
        
        List<RiskMetricAlert> alerts = new ArrayList<>();
        
        try {
            CIGateMetricsResponse current = metricsService.getGeneralMetrics(
                null, 
                LocalDate.now().minusDays(7), 
                LocalDate.now()
            );
            
            CIGateMetricsResponse historical = metricsService.getGeneralMetrics(
                null,
                LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(7)
            );

            double currentWarningRate = current.getWarningRate();
            double historicalWarningRate = historical.getWarningRate();
            double delta = currentWarningRate - historicalWarningRate;

            if (delta > WARNING_SPIKE_THRESHOLD) {
                alerts.add(RiskMetricAlert.builder()
                    .type(AlertType.WARNING_SPIKE)
                    .severity(AlertSeverity.WARNING)
                    .message(String.format(
                        "Aumento súbito de warnings detectado (+%.1f%% vs média histórica)",
                        delta
                    ))
                    .evidence("currentWarningRate", currentWarningRate)
                    .evidence("historicalWarningRate", historicalWarningRate)
                    .evidence("delta", delta)
                    .evidence("threshold", WARNING_SPIKE_THRESHOLD)
                    .build());
                
                log.warn("⚠️ WARNING_SPIKE: +{:.1f}% (current: {:.1f}%, historical: {:.1f}%)", 
                         delta, currentWarningRate, historicalWarningRate);
            }
        } catch (Exception e) {
            log.warn("⚠️ Erro ao detectar warning spike: {}", e.getMessage());
        }

        log.info("✅ Warning spike detection: {} alertas", alerts.size());
        return alerts;
    }

    /**
     * REGRA 4: Detecta tendência negativa (≥3 dias consecutivos de piora)
     */
    private List<RiskMetricAlert> detectNegativeTrend(LocalDate from, LocalDate to) {
        log.info("📊 Detectando tendências negativas...");
        
        List<RiskMetricAlert> alerts = new ArrayList<>();
        
        try {
            List<CIGateTimelinePoint> timeline = metricsService.getTimeline(
                LocalDate.now().minusDays(7),
                LocalDate.now()
            );

            if (timeline.size() < NEGATIVE_TREND_DAYS) {
                log.info("⚠️ Dados insuficientes para análise de tendência ({} dias)", timeline.size());
                return alerts;
            }

            // Ordenar por data
            timeline.sort(Comparator.comparing(CIGateTimelinePoint::getDate));

            // Verificar 3 dias consecutivos de aumento no blockRate
            int consecutiveDegradation = 0;
            for (int i = 1; i < timeline.size(); i++) {
                CIGateTimelinePoint current = timeline.get(i);
                CIGateTimelinePoint previous = timeline.get(i - 1);

                double currentRate = current.getBlocked() * 100.0 / current.getExecutions();
                double previousRate = previous.getBlocked() * 100.0 / previous.getExecutions();

                if (currentRate > previousRate) {
                    consecutiveDegradation++;
                } else {
                    consecutiveDegradation = 0;
                }

                if (consecutiveDegradation >= NEGATIVE_TREND_DAYS - 1) {
                    alerts.add(RiskMetricAlert.builder()
                        .type(AlertType.NEGATIVE_TREND)
                        .severity(AlertSeverity.WARNING)
                        .message(String.format(
                            "Tendência negativa detectada: blockRate aumentando por %d dias consecutivos",
                            NEGATIVE_TREND_DAYS
                        ))
                        .evidence("consecutiveDays", NEGATIVE_TREND_DAYS)
                        .evidence("latestBlockRate", currentRate)
                        .evidence("previousBlockRate", previousRate)
                        .build());
                    
                    log.warn("⚠️ NEGATIVE_TREND: {} dias de degradação", NEGATIVE_TREND_DAYS);
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Erro ao detectar tendência negativa: {}", e.getMessage());
        }

        log.info("✅ Negative trend detection: {} alertas", alerts.size());
        return alerts;
    }

    /**
     * REGRA 5: Detecta degradação sistêmica (blockRate global > 25%)
     */
    private List<RiskMetricAlert> detectSystemDegradation(LocalDate from, LocalDate to) {
        log.info("📊 Detectando degradação sistêmica...");
        
        List<RiskMetricAlert> alerts = new ArrayList<>();
        
        try {
            CIGateMetricsResponse metrics = metricsService.getGeneralMetrics(null, from, to);

            if (metrics.getBlockRate() > SYSTEM_DEGRADATION_THRESHOLD) {
                alerts.add(RiskMetricAlert.builder()
                    .type(AlertType.SYSTEM_DEGRADATION)
                    .severity(AlertSeverity.CRITICAL)
                    .message(String.format(
                        "Sistema degradado: blockRate global de %.1f%% (threshold: %.1f%%)",
                        metrics.getBlockRate(), SYSTEM_DEGRADATION_THRESHOLD
                    ))
                    .evidence("blockRate", metrics.getBlockRate())
                    .evidence("threshold", SYSTEM_DEGRADATION_THRESHOLD)
                    .evidence("totalExecutions", metrics.getTotalExecutions())
                    .evidence("blockedCount", metrics.getBlockedCount())
                    .build());
                
                log.error("🚨 SYSTEM_DEGRADATION: blockRate global = {:.1f}%", metrics.getBlockRate());
            }
        } catch (Exception e) {
            log.warn("⚠️ Erro ao detectar degradação sistêmica: {}", e.getMessage());
        }

        log.info("✅ System degradation detection: {} alertas", alerts.size());
        return alerts;
    }

    /**
     * REGRA 6: Detecta possíveis falsos positivos (warnings altos + poucos incidentes)
     */
    private List<RiskMetricAlert> detectPotentialFalsePositives() {
        log.info("📊 Detectando possíveis falsos positivos...");
        
        List<RiskMetricAlert> alerts = new ArrayList<>();
        
        try {
            List<CIGateRuleMetrics> ruleMetrics = metricsService.getRuleMetrics();

            for (CIGateRuleMetrics rm : ruleMetrics) {
                // Regra com muitos warnings mas poucos incidentes históricos
                if (rm.getWarningCount() > 10) {
                    int incidentCount = incidentRepository
                        .findByBusinessRuleIdOrderByOccurredAtDesc(rm.getBusinessRuleId())
                        .size();

                    if (incidentCount < 2) {
                        alerts.add(RiskMetricAlert.builder()
                            .type(AlertType.POTENTIAL_FALSE_POSITIVE)
                            .severity(AlertSeverity.INFO)
                            .message(String.format(
                                "Regra '%s' gera muitos warnings (%d) mas tem poucos incidentes reais (%d) - revisar thresholds",
                                rm.getRuleName(), rm.getWarningCount(), incidentCount
                            ))
                            .businessRuleId(rm.getBusinessRuleId().toString())
                            .evidence("warningCount", rm.getWarningCount())
                            .evidence("incidentCount", incidentCount)
                            .evidence("blockCount", rm.getBlockCount())
                            .build());
                        
                        log.info("💡 POTENTIAL_FALSE_POSITIVE: {} - {} warnings, {} incidentes", 
                                rm.getRuleName(), rm.getWarningCount(), incidentCount);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Erro ao detectar falsos positivos: {}", e.getMessage());
        }

        log.info("✅ False positive detection: {} alertas", alerts.size());
        return alerts;
    }

    // ============ ENRIQUECIMENTO DE DADOS ============

    /**
     * Enriquece alerta com informações adicionais (projeto, regra, ownership)
     */
    private RiskMetricAlertResponse enrichAlert(RiskMetricAlert alert) {
        RiskMetricAlertResponse response = new RiskMetricAlertResponse(
            alert.getId(),
            alert.getType(),
            alert.getSeverity(),
            alert.getMessage(),
            alert.getDetectedAt(),
            alert.getEvidence()
        );

        // Enriquecer com contexto de projeto
        if (alert.getProjectId() != null) {
            projectRepository.findById(alert.getProjectId()).ifPresent(project -> {
                response.setProjectContext(ProjectContext.scoped(
                    project.getId(),
                    project.getName()
                ));
            });
        } else {
            response.setProjectContext(ProjectContext.global());
        }

        // Enriquecer com informações da regra
        if (alert.getBusinessRuleId() != null) {
            businessRuleRepository.findById(alert.getBusinessRuleId()).ifPresent(rule -> {
                response.setBusinessRuleId(rule.getId());
                response.setBusinessRuleName(rule.getName());

                // Buscar ownership
                List<BusinessRuleOwnership> ownerships = ownershipRepository
                    .findByBusinessRuleId(UUID.fromString(rule.getId()));
                
                response.setOwnerships(ownerships.stream()
                    .map(o -> {
                        OwnershipSummary summary = new OwnershipSummary();
                        summary.setRuleId(rule.getId());
                        summary.setRuleName(rule.getName());
                        summary.setTeam(o.getTeamName());
                        summary.setContact(o.getContactEmail());
                        return summary;
                    })
                    .collect(Collectors.toList()));
            });
        }

        return response;
    }
}
