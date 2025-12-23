package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de dashboard executivo de risco
 * 
 * Consolida indicadores de múltiplos serviços (US #20, #21, #23) para
 * fornecer visão executiva de acurácia e confiabilidade do sistema.
 * 
 * IMPORTANTE: Serviço 100% READ-ONLY
 * - NÃO modifica decisões de risco
 * - NÃO recalcula scores
 * - NÃO altera auditorias, feedbacks ou métricas
 * - NÃO chama IA
 * - Apenas consolida e resume dados existentes
 * - Lógica 100% determinística e auditável
 */
@Service
public class ExecutiveRiskDashboardService {

    private static final Logger log = LoggerFactory.getLogger(ExecutiveRiskDashboardService.class);

    private final RiskMetricsService metricsService;

    public ExecutiveRiskDashboardService(RiskMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Gera dashboard executivo consolidado
     * 
     * @param from Timestamp inicial (ISO-8601)
     * @param to Timestamp final (ISO-8601)
     * @param environment Filtro por ambiente (opcional)
     * @param focus GLOBAL ou PRODUCTION_ONLY
     */
    public ExecutiveRiskDashboardResponse generateDashboard(Instant from,
                                                           Instant to,
                                                           String environment,
                                                           String focus) {
        log.info("Gerando dashboard executivo - from: {}, to: {}, env: {}, focus: {}",
                from, to, environment, focus);

        // Aplica foco PRODUCTION_ONLY se solicitado
        String effectiveEnvironment = environment;
        if ("PRODUCTION_ONLY".equalsIgnoreCase(focus) && environment == null) {
            effectiveEnvironment = "PRODUCTION";
        }

        // Converte Instant para LocalDate para usar RiskMetricsService
        LocalDate fromDate = from != null ? from.atZone(ZoneId.systemDefault()).toLocalDate() : null;
        LocalDate toDate = to != null ? to.atZone(ZoneId.systemDefault()).toLocalDate() : null;

        // Busca métricas consolidadas (reutiliza US #23)
        RiskMetricsResponse metrics = metricsService.calculateMetrics(
                fromDate, toDate, effectiveEnvironment, null);

        // Monta período analisado
        PeriodSummary period = buildPeriodSummary(from, to, focus, effectiveEnvironment, metrics);

        // Calcula status de confiança baseado no score
        ExecutiveRiskDashboardResponse.ConfidenceStatus confidenceStatus = 
                calculateConfidenceStatus(metrics.getSystemConfidenceScore());

        // Monta sumário de decisões
        DashboardSummary summary = buildDashboardSummary(metrics);

        // Monta indicadores-chave
        KeyRiskIndicators keyIndicators = buildKeyIndicators(metrics);

        // Identifica top risk drivers (regras problemáticas)
        List<TopRiskDriverSummary> topRiskDrivers = buildTopRiskDrivers(metrics);

        // Gera alertas baseados em regras determinísticas
        List<DashboardAlert> alerts = generateAlerts(metrics, confidenceStatus);

        // Gera recomendação determinística
        String recommendation = generateRecommendation(confidenceStatus, alerts);

        log.info("Dashboard executivo gerado - confiança: {}% ({}), alertas: {}, drivers: {}",
                String.format("%.1f", metrics.getSystemConfidenceScore()),
                confidenceStatus,
                alerts.size(),
                topRiskDrivers.size());

        return new ExecutiveRiskDashboardResponse(
                period,
                metrics.getSystemConfidenceScore(),
                confidenceStatus,
                summary,
                keyIndicators,
                topRiskDrivers,
                alerts,
                recommendation
        );
    }

    /**
     * Constrói sumário do período analisado
     */
    private PeriodSummary buildPeriodSummary(Instant from,
                                            Instant to,
                                            String focus,
                                            String environment,
                                            RiskMetricsResponse metrics) {
        Instant effectiveFrom = from != null ? from : 
                metrics.getPeriodStart().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant effectiveTo = to != null ? to : 
                metrics.getPeriodEnd().atStartOfDay(ZoneId.systemDefault()).toInstant();

        long days = ChronoUnit.DAYS.between(effectiveFrom, effectiveTo);
        String description = String.format("Análise de %d dias", Math.abs(days));

        String effectiveFocus = focus != null ? focus : "GLOBAL";

        return new PeriodSummary(effectiveFrom, effectiveTo, description, effectiveFocus, environment);
    }

    /**
     * Calcula status de confiança baseado no score (0-100)
     * 
     * >= 90 → EXCELLENT
     * 80-89 → HEALTHY
     * 65-79 → ATTENTION
     * < 65 → CRITICAL
     */
    private ExecutiveRiskDashboardResponse.ConfidenceStatus calculateConfidenceStatus(double score) {
        if (score >= 90.0) {
            return ExecutiveRiskDashboardResponse.ConfidenceStatus.EXCELLENT;
        } else if (score >= 80.0) {
            return ExecutiveRiskDashboardResponse.ConfidenceStatus.HEALTHY;
        } else if (score >= 65.0) {
            return ExecutiveRiskDashboardResponse.ConfidenceStatus.ATTENTION;
        } else {
            return ExecutiveRiskDashboardResponse.ConfidenceStatus.CRITICAL;
        }
    }

    /**
     * Constrói sumário de decisões a partir das métricas
     */
    private DashboardSummary buildDashboardSummary(RiskMetricsResponse metrics) {
        // Calcula contadores de falsos positivos e negativos
        int falsePositives = (int) Math.round((metrics.getFalsePositiveRate() * metrics.getTotalDecisions()) / 100.0);
        int falseNegatives = (int) Math.round((metrics.getFalseNegativeRate() * metrics.getTotalDecisions()) / 100.0);
        int incidentsAfterApproval = (int) Math.round((metrics.getIncidentAfterApprovalRate() * metrics.getTotalDecisions()) / 100.0);

        return new DashboardSummary(
                metrics.getTotalDecisions(),
                metrics.getApprovedCount(),
                metrics.getApprovedWithRestrictionsCount(),
                metrics.getBlockedCount(),
                incidentsAfterApproval,
                falsePositives,
                falseNegatives
        );
    }

    /**
     * Constrói indicadores-chave a partir das métricas
     */
    private KeyRiskIndicators buildKeyIndicators(RiskMetricsResponse metrics) {
        return new KeyRiskIndicators(
                metrics.getAccuracyRate(),
                metrics.getFalsePositiveRate(),
                metrics.getFalseNegativeRate(),
                metrics.getSafeChangeBlockedRate(),
                metrics.getIncidentAfterApprovalRate()
        );
    }

    /**
     * Identifica top risk drivers baseado em tendências (US #23)
     * 
     * Filtra apenas regras com >= 3 evidências
     */
    private List<TopRiskDriverSummary> buildTopRiskDrivers(RiskMetricsResponse metrics) {
        return metrics.getTrendIndicators().stream()
                .filter(trend -> trend.getType() == TrendIndicator.TrendType.PROBLEMATIC_BUSINESS_RULE)
                .filter(trend -> trend.getEvidenceCount() >= 3)
                .map(trend -> new TopRiskDriverSummary(
                        trend.getAffectedEntity(),
                        "Regra ID: " + trend.getAffectedEntity(),
                        trend.getEvidenceCount(),
                        0,  // Não temos contadores separados na trend
                        0,  // Não temos contadores separados na trend
                        trend.getImpactRate(),
                        trend.getDescription()
                ))
                .limit(5)  // Top 5 drivers
                .collect(Collectors.toList());
    }

    /**
     * Gera alertas baseados em regras determinísticas
     * 
     * Alertas gerados se:
     * - incidentAfterApprovalRate > 3%
     * - falseNegativeRate > falsePositiveRate
     * - confidenceStatus = CRITICAL
     */
    private List<DashboardAlert> generateAlerts(RiskMetricsResponse metrics,
                                               ExecutiveRiskDashboardResponse.ConfidenceStatus confidenceStatus) {
        List<DashboardAlert> alerts = new ArrayList<>();

        // Alerta: Taxa de incidentes após aprovação > 3%
        if (metrics.getIncidentAfterApprovalRate() > 3.0) {
            DashboardAlert.AlertSeverity severity = metrics.getIncidentAfterApprovalRate() > 10.0 ?
                    DashboardAlert.AlertSeverity.CRITICAL : DashboardAlert.AlertSeverity.WARNING;
            
            alerts.add(new DashboardAlert(
                    DashboardAlert.AlertType.HIGH_INCIDENT_RATE,
                    severity,
                    String.format("Taxa de incidentes após aprovação está em %.1f%% (limite: 3%%)",
                            metrics.getIncidentAfterApprovalRate()),
                    "SYSTEM",
                    metrics.getIncidentAfterApprovalRate()
            ));
        }

        // Alerta: Falsos negativos > falsos positivos (sistema muito permissivo)
        if (metrics.getFalseNegativeRate() > metrics.getFalsePositiveRate()) {
            alerts.add(new DashboardAlert(
                    DashboardAlert.AlertType.FALSE_NEGATIVE_DOMINANCE,
                    DashboardAlert.AlertSeverity.WARNING,
                    String.format("Falsos negativos (%.1f%%) superam falsos positivos (%.1f%%). Sistema pode estar muito permissivo.",
                            metrics.getFalseNegativeRate(), metrics.getFalsePositiveRate()),
                    "SYSTEM",
                    metrics.getFalseNegativeRate()
            ));
        }

        // Alerta: Confiança crítica
        if (confidenceStatus == ExecutiveRiskDashboardResponse.ConfidenceStatus.CRITICAL) {
            alerts.add(new DashboardAlert(
                    DashboardAlert.AlertType.CRITICAL_CONFIDENCE,
                    DashboardAlert.AlertSeverity.CRITICAL,
                    String.format("Confiança do sistema está em nível crítico: %.1f%% (limite saudável: 80%%)",
                            metrics.getSystemConfidenceScore()),
                    "SYSTEM",
                    metrics.getSystemConfidenceScore()
            ));
        }

        // Alertas: Regras problemáticas
        metrics.getTrendIndicators().stream()
                .filter(trend -> trend.getType() == TrendIndicator.TrendType.PROBLEMATIC_BUSINESS_RULE)
                .filter(trend -> trend.getEvidenceCount() >= 5)  // Apenas regras com muitas evidências
                .forEach(trend -> alerts.add(new DashboardAlert(
                        DashboardAlert.AlertType.PROBLEMATIC_RULE,
                        trend.getSeverity() == TrendIndicator.Severity.HIGH ?
                                DashboardAlert.AlertSeverity.CRITICAL : DashboardAlert.AlertSeverity.WARNING,
                        trend.getDescription(),
                        trend.getAffectedEntity(),
                        trend.getImpactRate()
                )));

        // Alertas: Degradação de ambiente
        metrics.getTrendIndicators().stream()
                .filter(trend -> trend.getType() == TrendIndicator.TrendType.ENVIRONMENT_DEGRADATION)
                .forEach(trend -> alerts.add(new DashboardAlert(
                        DashboardAlert.AlertType.ENVIRONMENT_DEGRADATION,
                        DashboardAlert.AlertSeverity.WARNING,
                        trend.getDescription(),
                        trend.getAffectedEntity(),
                        trend.getImpactRate()
                )));

        return alerts;
    }

    /**
     * Gera recomendação determinística baseada no status de confiança
     */
    private String generateRecommendation(ExecutiveRiskDashboardResponse.ConfidenceStatus status,
                                         List<DashboardAlert> alerts) {
        switch (status) {
            case EXCELLENT:
                return "Sistema operando com excelência. Manter estratégia atual e monitoramento contínuo.";
            
            case HEALTHY:
                return "Sistema operando dentro de parâmetros saudáveis. " +
                       "Continuar monitoramento e revisar alertas periodicamente.";
            
            case ATTENTION:
                if (alerts.isEmpty()) {
                    return "Sistema requer atenção. Apesar de não haver alertas críticos, " +
                           "recomenda-se revisar regras de negócio e configurações de criticidade.";
                } else {
                    return String.format("Sistema requer atenção. %d alerta(s) identificado(s). " +
                                       "Recomenda-se revisar regras problemáticas e ajustar pesos de risco conforme US #22.",
                                       alerts.size());
                }
            
            case CRITICAL:
                long criticalAlerts = alerts.stream()
                        .filter(a -> a.getSeverity() == DashboardAlert.AlertSeverity.CRITICAL)
                        .count();
                
                return String.format("AÇÃO IMEDIATA NECESSÁRIA. Sistema em estado crítico com %d alerta(s) crítico(s). " +
                                   "Recomenda-se: (1) Revisar decisões recentes, (2) Ajustar regras problemáticas, " +
                                   "(3) Considerar aumento temporário de restrições até estabilização.",
                                   criticalAlerts);
            
            default:
                return "Status indeterminado. Revisar métricas manualmente.";
        }
    }
}

