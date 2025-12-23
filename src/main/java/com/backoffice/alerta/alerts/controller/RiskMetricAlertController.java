package com.backoffice.alerta.alerts.controller;

import com.backoffice.alerta.alerts.dto.RiskMetricAlertResponse;
import com.backoffice.alerta.alerts.dto.RiskMetricAlertSummaryResponse;
import com.backoffice.alerta.alerts.service.RiskMetricAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller REST para alertas inteligentes baseados em métricas do Gate de Risco
 * 
 * US#55 - Alertas Inteligentes Baseados em Métricas
 * 
 * IMPORTANTE:
 * - READ-ONLY: não persiste dados, não envia notificações
 * - CONSULTIVO: gera alertas sob demanda
 * - RBAC: ADMIN e RISK_MANAGER apenas
 * - Usa métricas da US#54 (CIGateMetricsService)
 */
@RestController
@RequestMapping("/risk/alerts")
@Tag(name = "Risk Metric Alerts", description = "Alertas inteligentes baseados em métricas do Gate de Risco (US#55)")
public class RiskMetricAlertController {

    private final RiskMetricAlertService alertService;

    public RiskMetricAlertController(RiskMetricAlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Listar todos os alertas detectados",
        description = """
            Detecta e retorna todos os alertas ativos baseados em métricas do Gate de Risco.
            
            **Tipos de Alerta:**
            - HIGH_BLOCK_RATE_PROJECT: Projeto com blockRate > 30%
            - RULE_OVERBLOCKING: Regra bloqueou ≥5 PRs
            - WARNING_SPIKE: Aumento de warnings >15% vs média
            - NEGATIVE_TREND: BlockRate aumentando por ≥3 dias
            - SYSTEM_DEGRADATION: BlockRate global >25%
            - POTENTIAL_FALSE_POSITIVE: Warnings altos + poucos incidentes
            
            **IMPORTANTE:**
            - Alertas são gerados dinamicamente (não persistidos)
            - Determinístico: mesma métrica → mesmo alerta
            - Não envia notificações (apenas consulta)
            - Enriquecido com projeto, regra e ownership
            
            **Casos de Uso:**
            - Dashboard executivo de alertas
            - Monitoramento de saúde do Gate
            - Identificação de regras problemáticas
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Alertas detectados com sucesso",
                content = @Content(schema = @Schema(implementation = RiskMetricAlertResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN/RISK_MANAGER")
        }
    )
    public ResponseEntity<List<RiskMetricAlertResponse>> getAllAlerts(
            @Parameter(description = "ID do projeto para filtrar (opcional)")
            @RequestParam(required = false) UUID projectId,
            
            @Parameter(description = "Data inicial do período de análise (formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            
            @Parameter(description = "Data final do período de análise (formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<RiskMetricAlertResponse> alerts = alertService.detectAlerts(projectId, from, to);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Obter resumo executivo de alertas",
        description = """
            Retorna estatísticas agregadas de todos os alertas detectados.
            
            **Informações Incluídas:**
            - Total de alertas
            - Distribuição por severidade (CRITICAL, WARNING, INFO)
            - Distribuição por tipo de alerta
            - Distribuição por projeto
            - Status de saúde do sistema (HEALTHY, WARNING, DEGRADED, CRITICAL)
            
            **Status de Saúde:**
            - CRITICAL: ≥1 alerta crítico
            - DEGRADED: >5 warnings
            - WARNING: ≥1 warning
            - HEALTHY: sem alertas
            
            **Casos de Uso:**
            - Dashboard executivo
            - KPIs de qualidade do Gate
            - Relatórios gerenciais
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Resumo gerado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = RiskMetricAlertSummaryResponse.class),
                    examples = @ExampleObject(value = """
                        {
                          "totalAlerts": 8,
                          "criticalCount": 2,
                          "warningCount": 5,
                          "infoCount": 1,
                          "alertsByType": {
                            "HIGH_BLOCK_RATE_PROJECT": 1,
                            "RULE_OVERBLOCKING": 2,
                            "WARNING_SPIKE": 1,
                            "NEGATIVE_TREND": 1,
                            "SYSTEM_DEGRADATION": 1,
                            "POTENTIAL_FALSE_POSITIVE": 2
                          },
                          "alertsByProject": {
                            "Backoffice Pagamentos": 4,
                            "Portal do Cliente": 2
                          },
                          "healthStatus": "DEGRADED"
                        }
                        """)
                )),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
        }
    )
    public ResponseEntity<RiskMetricAlertSummaryResponse> getAlertSummary(
            @Parameter(description = "Data inicial (opcional)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            
            @Parameter(description = "Data final (opcional)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        RiskMetricAlertSummaryResponse summary = alertService.getAlertSummary(from, to);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/projects/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Listar alertas de um projeto específico",
        description = """
            Retorna apenas alertas relacionados ao projeto especificado.
            
            **Alertas Possíveis por Projeto:**
            - HIGH_BLOCK_RATE_PROJECT: BlockRate do projeto >30%
            - RULE_OVERBLOCKING: Regras do projeto sobrebloqueando
            - NEGATIVE_TREND: Tendência de piora no projeto
            
            **Casos de Uso:**
            - Dashboard de projeto específico
            - Análise de problemas em um projeto
            - Relatório de saúde de projeto
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Alertas do projeto retornados",
                content = @Content(
                    schema = @Schema(implementation = RiskMetricAlertResponse.class),
                    examples = @ExampleObject(value = """
                        [
                          {
                            "id": "550e8400-e29b-41d4-a716-446655440099",
                            "type": "HIGH_BLOCK_RATE_PROJECT",
                            "severity": "CRITICAL",
                            "message": "Projeto 'Backoffice Pagamentos' apresenta taxa de bloqueio crítica (35.7%, threshold: 30.0%)",
                            "projectContext": {
                              "isScoped": true,
                              "projectId": "550e8400-e29b-41d4-a716-446655440011",
                              "projectName": "Backoffice Pagamentos"
                            },
                            "detectedAt": "2025-12-20T19:30:00Z",
                            "evidence": {
                              "blockRate": 35.7,
                              "threshold": 30.0,
                              "blockedCount": 15,
                              "totalExecutions": 42
                            }
                          }
                        ]
                        """)
                )),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
        }
    )
    public ResponseEntity<List<RiskMetricAlertResponse>> getAlertsForProject(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable UUID projectId,
            
            @Parameter(description = "Data inicial (opcional)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            
            @Parameter(description = "Data final (opcional)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<RiskMetricAlertResponse> alerts = alertService.detectAlertsForProject(projectId, from, to);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/rules/{ruleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Listar alertas de uma regra de negócio específica",
        description = """
            Retorna alertas relacionados a uma regra de negócio.
            
            **Alertas Possíveis por Regra:**
            - RULE_OVERBLOCKING: Regra bloqueou ≥5 PRs
            - POTENTIAL_FALSE_POSITIVE: Muitos warnings + poucos incidentes reais
            
            **Informações Enriquecidas:**
            - Nome da regra
            - Ownership (times/pessoas responsáveis)
            - Evidências numéricas (blockCount, warningCount, etc.)
            
            **Casos de Uso:**
            - Análise de regra específica
            - Identificar regras problemáticas
            - Ajustar thresholds de regras
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Alertas da regra retornados",
                content = @Content(
                    schema = @Schema(implementation = RiskMetricAlertResponse.class),
                    examples = @ExampleObject(value = """
                        [
                          {
                            "id": "550e8400-e29b-41d4-a716-446655440088",
                            "type": "RULE_OVERBLOCKING",
                            "severity": "CRITICAL",
                            "message": "Regra 'REGRA_CALCULO_HORAS_PJ' bloqueou 8 PRs (threshold: 5) - revisar criticidade",
                            "businessRuleId": "550e8400-e29b-41d4-a716-446655440001",
                            "businessRuleName": "REGRA_CALCULO_HORAS_PJ",
                            "ownerships": [
                              {
                                "team": "Backoffice",
                                "owner": "João Silva"
                              }
                            ],
                            "detectedAt": "2025-12-20T19:30:00Z",
                            "evidence": {
                              "blockCount": 8,
                              "threshold": 5,
                              "warningCount": 15,
                              "criticality": "ALTA"
                            }
                          }
                        ]
                        """)
                )),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
        }
    )
    public ResponseEntity<List<RiskMetricAlertResponse>> getAlertsForRule(
            @Parameter(description = "ID da regra de negócio", required = true)
            @PathVariable String ruleId
    ) {
        List<RiskMetricAlertResponse> alerts = alertService.detectAlertsForRule(ruleId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Verificar status de saúde do sistema",
        description = """
            Retorna status de saúde do Gate de Risco baseado em alertas detectados.
            
            **Status Possíveis:**
            - HEALTHY: Sistema operando normalmente
            - WARNING: Alertas de atenção detectados
            - DEGRADED: Múltiplos warnings detectados
            - CRITICAL: Alertas críticos detectados
            
            **Métricas Incluídas:**
            - Total de alertas
            - Quantidade de alertas críticos
            - Quantidade de warnings
            - Timestamp da análise
            
            **Casos de Uso:**
            - Health check em dashboards
            - Monitoramento automatizado
            - Integração com ferramentas de observabilidade
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Health status retornado",
                content = @Content(
                    examples = @ExampleObject(value = """
                        {
                          "status": "DEGRADED",
                          "totalAlerts": 8,
                          "criticalCount": 2,
                          "warningCount": 5,
                          "timestamp": "2025-12-20T19:30:15.123Z"
                        }
                        """)
                )),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
        }
    )
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> health = alertService.getHealthStatus();
        return ResponseEntity.ok(health);
    }
}
