package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.RiskMetricsResponse;
import com.backoffice.alerta.service.RiskMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Controller REST para métricas de acurácia e confiabilidade do sistema de risco
 * 
 * IMPORTANTE: Endpoint SOMENTE LEITURA (Read-Only)
 * - NÃO modifica decisões de risco
 * - NÃO recalcula scores passados
 * - NÃO altera auditorias, feedbacks ou incidentes
 * - Apenas correlaciona dados existentes para análise de performance
 * - Lógica 100% determinística e auditável
 */
@RestController
@RequestMapping("/risk/metrics")
@Tag(name = "Metrics", description = "Métricas de acurácia e confiabilidade do sistema de risco (Read-Only)")
public class RiskMetricsController {

    private final RiskMetricsService metricsService;

    public RiskMetricsController(RiskMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping
    @Operation(
        summary = "Calcula métricas de acurácia e confiabilidade do sistema",
        description = "Analisa auditorias (US#20), feedbacks pós-deploy (US#21) e incidentes históricos (US#17) " +
                     "para calcular métricas de performance das decisões de risco. " +
                     "\n\n**Métricas calculadas:**" +
                     "\n- Acurácia geral do sistema" +
                     "\n- Taxa de falsos positivos (mudanças seguras bloqueadas)" +
                     "\n- Taxa de falsos negativos (incidentes após aprovação)" +
                     "\n- Métricas agrupadas por nível de risco (BAIXA, MEDIA, ALTA, CRITICA)" +
                     "\n- Tendências detectadas (padrões problemáticos)" +
                     "\n- Score de confiança do sistema (0-100)" +
                     "\n\n**Classificação determinística:**" +
                     "\n- APROVADO + INCIDENT → Falso Negativo" +
                     "\n- BLOQUEADO + SUCCESS → Falso Positivo" +
                     "\n- APROVADO + SUCCESS → Decisão Correta" +
                     "\n- BLOQUEADO + INCIDENT → Decisão Correta" +
                     "\n\n**IMPORTANTE:** Endpoint read-only. Não modifica dados existentes.",
        parameters = {
            @Parameter(
                name = "from",
                description = "Data inicial do período de análise (formato: yyyy-MM-dd). Se omitido, considera todos os dados históricos.",
                example = "2025-11-01"
            ),
            @Parameter(
                name = "to",
                description = "Data final do período de análise (formato: yyyy-MM-dd). Se omitido, considera até hoje.",
                example = "2025-12-17"
            ),
            @Parameter(
                name = "environment",
                description = "Filtrar por ambiente específico (DEV, STAGING, PRODUCTION). Se omitido, considera todos.",
                example = "PRODUCTION"
            ),
            @Parameter(
                name = "businessRuleId",
                description = "Filtrar por regra de negócio específica (UUID). Se omitido, considera todas.",
                example = "123e4567-e89b-12d3-a456-426614174000"
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Métricas calculadas com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RiskMetricsResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "Métricas gerais - todos os dados",
                            value = """
                            {
                              "totalDecisions": 150,
                              "totalDeploys": 120,
                              "totalIncidents": 8,
                              "approvedCount": 90,
                              "approvedWithRestrictionsCount": 35,
                              "blockedCount": 25,
                              "accuracyRate": 87.5,
                              "falsePositiveRate": 6.7,
                              "falseNegativeRate": 5.3,
                              "incidentAfterApprovalRate": 4.0,
                              "safeChangeBlockedRate": 8.7,
                              "systemConfidenceScore": 82.3,
                              "periodStart": "2025-10-01",
                              "periodEnd": "2025-12-17",
                              "appliedFilters": "Sem filtros (todos os dados)",
                              "metricsByRiskLevel": {
                                "BAIXA": {
                                  "riskLevel": "BAIXA",
                                  "totalDecisions": 60,
                                  "approvedCount": 58,
                                  "blockedCount": 2,
                                  "deploysWithIncidents": 1,
                                  "deploysWithSuccess": 55,
                                  "accuracyRate": 95.0,
                                  "falsePositiveRate": 3.3,
                                  "falseNegativeRate": 1.7
                                }
                              },
                              "trendIndicators": [
                                {
                                  "type": "FALSE_POSITIVE_INCREASE",
                                  "severity": "MEDIUM",
                                  "description": "Detectados 10 casos de falsos positivos (8.3% dos feedbacks)",
                                  "affectedEntity": "SYSTEM",
                                  "evidenceCount": 10,
                                  "impactRate": 8.3
                                }
                              ]
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Métricas por ambiente - PRODUCTION",
                            value = """
                            {
                              "totalDecisions": 45,
                              "totalDeploys": 40,
                              "totalIncidents": 5,
                              "approvedCount": 30,
                              "approvedWithRestrictionsCount": 10,
                              "blockedCount": 5,
                              "accuracyRate": 91.1,
                              "falsePositiveRate": 4.4,
                              "falseNegativeRate": 4.4,
                              "incidentAfterApprovalRate": 8.9,
                              "safeChangeBlockedRate": 6.7,
                              "systemConfidenceScore": 85.2,
                              "periodStart": "2025-11-01",
                              "periodEnd": "2025-12-17",
                              "appliedFilters": "environment: PRODUCTION",
                              "metricsByRiskLevel": {},
                              "trendIndicators": []
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Métricas com tendências críticas",
                            value = """
                            {
                              "totalDecisions": 200,
                              "totalDeploys": 180,
                              "totalIncidents": 25,
                              "approvedCount": 120,
                              "approvedWithRestrictionsCount": 50,
                              "blockedCount": 30,
                              "accuracyRate": 78.5,
                              "falsePositiveRate": 12.0,
                              "falseNegativeRate": 9.5,
                              "incidentAfterApprovalRate": 10.0,
                              "safeChangeBlockedRate": 15.0,
                              "systemConfidenceScore": 68.7,
                              "periodStart": "2025-09-01",
                              "periodEnd": "2025-12-17",
                              "appliedFilters": "Sem filtros (todos os dados)",
                              "metricsByRiskLevel": {},
                              "trendIndicators": [
                                {
                                  "type": "FALSE_NEGATIVE_INCREASE",
                                  "severity": "CRITICAL",
                                  "description": "Detectados 19 casos de falsos negativos (10.6% dos feedbacks)",
                                  "affectedEntity": "SYSTEM",
                                  "evidenceCount": 19,
                                  "impactRate": 10.6
                                },
                                {
                                  "type": "HIGH_INCIDENT_RATE",
                                  "severity": "HIGH",
                                  "description": "Alta taxa de incidentes: 25 casos (12.5% das decisões)",
                                  "affectedEntity": "SYSTEM",
                                  "evidenceCount": 25,
                                  "impactRate": 12.5
                                },
                                {
                                  "type": "PROBLEMATIC_BUSINESS_RULE",
                                  "severity": "HIGH",
                                  "description": "Regra com 8 incidentes recorrentes (32.0% do total)",
                                  "affectedEntity": "123e4567-e89b-12d3-a456-426614174000",
                                  "evidenceCount": 8,
                                  "impactRate": 32.0
                                }
                              ]
                            }
                            """
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<RiskMetricsResponse> getMetrics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            
            @RequestParam(required = false)
            String environment,
            
            @RequestParam(required = false)
            UUID businessRuleId) {
        
        RiskMetricsResponse metrics = metricsService.calculateMetrics(
                from, to, environment, businessRuleId);
        
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Verifica saúde do serviço de métricas",
        description = "Endpoint simples para verificar se o serviço de métricas está disponível"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Metrics Service is running (read-only mode)");
    }
}
