package com.backoffice.alerta.ci.controller;

import com.backoffice.alerta.ci.dto.CIGateMetricsResponse;
import com.backoffice.alerta.ci.dto.CIGateProjectMetrics;
import com.backoffice.alerta.ci.dto.CIGateRuleMetrics;
import com.backoffice.alerta.ci.dto.CIGateTimelinePoint;
import com.backoffice.alerta.ci.service.CIGateMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST para métricas e observabilidade do Gate de Risco CI/CD
 * 
 * US#54 - Observabilidade e Métricas do Gate de Risco (CI/CD)
 * 
 * ENDPOINTS READ-ONLY:
 * - GET /risk/ci/metrics - Métricas gerais
 * - GET /risk/ci/metrics/projects - Métricas por projeto
 * - GET /risk/ci/metrics/rules - Regras que mais bloqueiam
 * - GET /risk/ci/metrics/timeline - Tendência temporal
 * 
 * RBAC: ADMIN + RISK_MANAGER
 * 
 * PRINCÍPIOS:
 * - Sem side-effects
 * - Apenas leitura de auditorias existentes
 * - Dados agregados para observabilidade e melhoria contínua
 */
@RestController
@RequestMapping("/risk/ci/metrics")
@Tag(name = "CI/CD Gate Metrics", description = "Métricas e observabilidade do Gate de Risco CI/CD (READ-ONLY)")
@SecurityRequirement(name = "Bearer Authentication")
public class CIGateMetricsController {

    private final CIGateMetricsService metricsService;

    public CIGateMetricsController(CIGateMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Retorna métricas gerais do Gate de Risco
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Métricas gerais do Gate de Risco",
        description = "Retorna estatísticas agregadas de execuções do gate: aprovações, bloqueios, warnings, taxas e nível de risco médio. " +
                     "**READ-ONLY**: Apenas lê dados existentes, sem side-effects.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Métricas calculadas com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CIGateMetricsResponse.class),
                    examples = @ExampleObject(
                        name = "Gate Saudável",
                        value = """
                            {
                              "totalExecutions": 150,
                              "approvedCount": 90,
                              "approvedWithRestrictionsCount": 35,
                              "blockedCount": 25,
                              "blockRate": 16.67,
                              "warningRate": 23.33,
                              "averageRiskLevel": "MEDIO",
                              "from": "2025-10-01",
                              "to": "2025-12-20"
                            }
                            """
                    )
                )
            )
        }
    )
    public ResponseEntity<CIGateMetricsResponse> getGeneralMetrics(
        @Parameter(description = "ID do projeto (opcional, null = GLOBAL)", example = "1")
        @RequestParam(required = false) Long projectId,
        
        @Parameter(description = "Data inicial (formato: yyyy-MM-dd, opcional, padrão: 90 dias atrás)", example = "2025-10-01")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        
        @Parameter(description = "Data final (formato: yyyy-MM-dd, opcional, padrão: hoje)", example = "2025-12-20")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        CIGateMetricsResponse metrics = metricsService.getGeneralMetrics(projectId, from, to);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Retorna métricas agrupadas por projeto
     */
    @GetMapping("/projects")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Métricas do Gate por Projeto",
        description = "Retorna lista de projetos ordenados por taxa de bloqueio (blockRate DESC). " +
                     "Útil para identificar projetos mais arriscados. " +
                     "**READ-ONLY**: Apenas lê dados existentes.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de métricas por projeto",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CIGateProjectMetrics.class),
                    examples = @ExampleObject(
                        name = "Projeto Crítico",
                        value = """
                            [
                              {
                                "projectId": 1,
                                "projectName": "Backoffice Pagamentos",
                                "totalExecutions": 45,
                                "blockedCount": 12,
                                "blockRate": 26.67,
                                "mostFrequentRiskLevel": "ALTO",
                                "lastExecutionAt": "2025-12-20T15:30:00Z"
                              },
                              {
                                "projectId": 2,
                                "projectName": "Portal do Cliente",
                                "totalExecutions": 80,
                                "blockedCount": 8,
                                "blockRate": 10.0,
                                "mostFrequentRiskLevel": "MEDIO",
                                "lastExecutionAt": "2025-12-19T10:15:00Z"
                              }
                            ]
                            """
                    )
                )
            )
        }
    )
    public ResponseEntity<List<CIGateProjectMetrics>> getProjectMetrics() {
        List<CIGateProjectMetrics> metrics = metricsService.getProjectMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Retorna regras que mais causam bloqueios
     */
    @GetMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Regras que Mais Bloqueiam",
        description = "Retorna lista de regras de negócio ordenadas por quantidade de bloqueios (blockCount DESC). " +
                     "Útil para identificar regras que precisam de ajustes ou treinamento. " +
                     "**READ-ONLY**: Apenas lê dados existentes.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de regras ordenadas por bloqueios",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CIGateRuleMetrics.class),
                    examples = @ExampleObject(
                        name = "Regra Problemática",
                        value = """
                            [
                              {
                                "businessRuleId": "550e8400-e29b-41d4-a716-446655440001",
                                "ruleName": "REGRA_CALCULO_HORAS_PJ",
                                "criticality": "ALTA",
                                "blockCount": 8,
                                "warningCount": 15,
                                "lastTriggeredAt": "2025-12-20T14:22:00Z"
                              },
                              {
                                "businessRuleId": "550e8400-e29b-41d4-a716-446655440003",
                                "ruleName": "REGRA_CALCULO_TRIBUTOS",
                                "criticality": "CRITICA",
                                "blockCount": 5,
                                "warningCount": 10,
                                "lastTriggeredAt": "2025-12-19T16:45:00Z"
                              }
                            ]
                            """
                    )
                )
            )
        }
    )
    public ResponseEntity<List<CIGateRuleMetrics>> getRuleMetrics() {
        List<CIGateRuleMetrics> metrics = metricsService.getRuleMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Retorna tendência temporal de execuções do gate
     */
    @GetMapping("/timeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Tendência Temporal do Gate",
        description = "Retorna evolução diária das execuções do gate no período especificado. " +
                     "Útil para identificar se o gate está melhorando ou piorando ao longo do tempo. " +
                     "**READ-ONLY**: Apenas lê dados existentes.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Timeline de execuções agrupadas por dia",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CIGateTimelinePoint.class),
                    examples = @ExampleObject(
                        name = "Timeline 7 dias",
                        value = """
                            [
                              {
                                "date": "2025-12-14",
                                "executions": 8,
                                "approved": 5,
                                "warnings": 2,
                                "blocked": 1
                              },
                              {
                                "date": "2025-12-15",
                                "executions": 12,
                                "approved": 7,
                                "warnings": 3,
                                "blocked": 2
                              },
                              {
                                "date": "2025-12-16",
                                "executions": 10,
                                "approved": 6,
                                "warnings": 3,
                                "blocked": 1
                              }
                            ]
                            """
                    )
                )
            )
        }
    )
    public ResponseEntity<List<CIGateTimelinePoint>> getTimeline(
        @Parameter(description = "Data inicial (formato: yyyy-MM-dd, opcional, padrão: 30 dias atrás)", example = "2025-11-20")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        
        @Parameter(description = "Data final (formato: yyyy-MM-dd, opcional, padrão: hoje)", example = "2025-12-20")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<CIGateTimelinePoint> timeline = metricsService.getTimeline(from, to);
        return ResponseEntity.ok(timeline);
    }
}
