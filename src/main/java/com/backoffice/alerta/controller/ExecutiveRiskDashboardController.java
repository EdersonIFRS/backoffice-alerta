package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.ExecutiveRiskDashboardResponse;
import com.backoffice.alerta.service.ExecutiveRiskDashboardService;
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

import java.time.Instant;

/**
 * Controller REST para Dashboard Executivo de Risco
 * 
 * IMPORTANTE: Endpoint 100% READ-ONLY
 * - NÃO modifica decisões de risco
 * - NÃO recalcula scores passados
 * - NÃO altera auditorias, feedbacks ou métricas
 * - Apenas consolida dados existentes de múltiplos serviços
 * - Lógica 100% determinística e auditável
 * - Não chama IA
 */
@RestController
@RequestMapping("/risk/dashboard")
@Tag(name = "Executive Dashboard", description = "Dashboard executivo de risco e confiabilidade (Read-Only)")
public class ExecutiveRiskDashboardController {

    private final ExecutiveRiskDashboardService dashboardService;

    public ExecutiveRiskDashboardController(ExecutiveRiskDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/executive")
    @Operation(
        summary = "Gera dashboard executivo consolidado de risco e confiabilidade",
        description = "Consolida indicadores de múltiplos serviços (Auditorias - US#20, Feedbacks - US#21, Métricas - US#23) " +
                     "para fornecer visão executiva de acurácia e confiabilidade do sistema de decisões de risco. " +
                     "\n\n**Indicadores consolidados:**" +
                     "\n- systemConfidenceScore (0-100) com status classificado (EXCELLENT/HEALTHY/ATTENTION/CRITICAL)" +
                     "\n- Sumário de decisões (aprovações, bloqueios, incidentes)" +
                     "\n- Indicadores-chave (acurácia, falsos positivos, falsos negativos)" +
                     "\n- Top risk drivers (regras de negócio problemáticas com >= 3 evidências)" +
                     "\n- Alertas automáticos baseados em regras determinísticas" +
                     "\n- Recomendações contextuais baseadas no status de confiança" +
                     "\n\n**Alertas gerados automaticamente se:**" +
                     "\n- Taxa de incidentes após aprovação > 3%" +
                     "\n- Falsos negativos > falsos positivos (sistema permissivo)" +
                     "\n- Confiança do sistema < 65% (crítico)" +
                     "\n- Regras específicas com >= 5 incidentes recorrentes" +
                     "\n\n**IMPORTANTE:** Endpoint 100% read-only. Não modifica dados existentes.",
        parameters = {
            @Parameter(
                name = "from",
                description = "Timestamp inicial do período (ISO-8601: yyyy-MM-dd'T'HH:mm:ss'Z'). Se omitido, considera todos os dados históricos.",
                example = "2025-11-01T00:00:00Z"
            ),
            @Parameter(
                name = "to",
                description = "Timestamp final do período (ISO-8601: yyyy-MM-dd'T'HH:mm:ss'Z'). Se omitido, considera até agora.",
                example = "2025-12-17T23:59:59Z"
            ),
            @Parameter(
                name = "environment",
                description = "Filtrar por ambiente específico (DEV, STAGING, PRODUCTION). Se omitido, considera todos.",
                example = "PRODUCTION"
            ),
            @Parameter(
                name = "focus",
                description = "Foco da análise: GLOBAL (todos ambientes) ou PRODUCTION_ONLY (somente produção). Default: GLOBAL.",
                example = "PRODUCTION_ONLY"
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Dashboard executivo gerado com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExecutiveRiskDashboardResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "Dashboard global saudável",
                            value = """
                            {
                              "period": {
                                "from": "2025-10-01T00:00:00Z",
                                "to": "2025-12-17T23:59:59Z",
                                "description": "Análise de 77 dias",
                                "focus": "GLOBAL",
                                "environment": null
                              },
                              "systemConfidenceScore": 87.5,
                              "confidenceStatus": "HEALTHY",
                              "summary": {
                                "totalDecisions": 150,
                                "approved": 90,
                                "approvedWithRestrictions": 35,
                                "blocked": 25,
                                "incidentsAfterApproval": 6,
                                "falsePositives": 10,
                                "falseNegatives": 8
                              },
                              "keyIndicators": {
                                "accuracyRate": 87.5,
                                "falsePositiveRate": 6.7,
                                "falseNegativeRate": 5.3,
                                "safeChangeBlockedRate": 8.7,
                                "incidentAfterApprovalRate": 4.0
                              },
                              "topRiskDrivers": [],
                              "alerts": [
                                {
                                  "type": "HIGH_INCIDENT_RATE",
                                  "severity": "WARNING",
                                  "message": "Taxa de incidentes após aprovação está em 4.0% (limite: 3%)",
                                  "affectedEntity": "SYSTEM",
                                  "impactRate": 4.0
                                }
                              ],
                              "recommendation": "Sistema operando dentro de parâmetros saudáveis. Continuar monitoramento e revisar alertas periodicamente."
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Produção com alertas críticos",
                            value = """
                            {
                              "period": {
                                "from": "2025-11-01T00:00:00Z",
                                "to": "2025-12-17T23:59:59Z",
                                "description": "Análise de 46 dias",
                                "focus": "PRODUCTION_ONLY",
                                "environment": "PRODUCTION"
                              },
                              "systemConfidenceScore": 62.3,
                              "confidenceStatus": "CRITICAL",
                              "summary": {
                                "totalDecisions": 85,
                                "approved": 55,
                                "approvedWithRestrictions": 15,
                                "blocked": 15,
                                "incidentsAfterApproval": 12,
                                "falsePositives": 8,
                                "falseNegatives": 18
                              },
                              "keyIndicators": {
                                "accuracyRate": 68.2,
                                "falsePositiveRate": 9.4,
                                "falseNegativeRate": 21.2,
                                "safeChangeBlockedRate": 12.5,
                                "incidentAfterApprovalRate": 14.1
                              },
                              "topRiskDrivers": [
                                {
                                  "businessRuleId": "123e4567-e89b-12d3-a456-426614174000",
                                  "ruleName": "Regra ID: 123e4567-e89b-12d3-a456-426614174000",
                                  "incidentCount": 8,
                                  "falseNegativeCount": 0,
                                  "falsePositiveCount": 0,
                                  "impactRate": 32.0,
                                  "primaryIssue": "Regra com 8 incidentes recorrentes (32.0% do total)"
                                }
                              ],
                              "alerts": [
                                {
                                  "type": "HIGH_INCIDENT_RATE",
                                  "severity": "CRITICAL",
                                  "message": "Taxa de incidentes após aprovação está em 14.1% (limite: 3%)",
                                  "affectedEntity": "SYSTEM",
                                  "impactRate": 14.1
                                },
                                {
                                  "type": "FALSE_NEGATIVE_DOMINANCE",
                                  "severity": "WARNING",
                                  "message": "Falsos negativos (21.2%) superam falsos positivos (9.4%). Sistema pode estar muito permissivo.",
                                  "affectedEntity": "SYSTEM",
                                  "impactRate": 21.2
                                },
                                {
                                  "type": "CRITICAL_CONFIDENCE",
                                  "severity": "CRITICAL",
                                  "message": "Confiança do sistema está em nível crítico: 62.3% (limite saudável: 80%)",
                                  "affectedEntity": "SYSTEM",
                                  "impactRate": 62.3
                                },
                                {
                                  "type": "PROBLEMATIC_RULE",
                                  "severity": "CRITICAL",
                                  "message": "Regra com 8 incidentes recorrentes (32.0% do total)",
                                  "affectedEntity": "123e4567-e89b-12d3-a456-426614174000",
                                  "impactRate": 32.0
                                }
                              ],
                              "recommendation": "AÇÃO IMEDIATA NECESSÁRIA. Sistema em estado crítico com 3 alerta(s) crítico(s). Recomenda-se: (1) Revisar decisões recentes, (2) Ajustar regras problemáticas, (3) Considerar aumento temporário de restrições até estabilização."
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Período curto com baixa confiança",
                            value = """
                            {
                              "period": {
                                "from": "2025-12-10T00:00:00Z",
                                "to": "2025-12-17T23:59:59Z",
                                "description": "Análise de 7 dias",
                                "focus": "GLOBAL",
                                "environment": null
                              },
                              "systemConfidenceScore": 72.8,
                              "confidenceStatus": "ATTENTION",
                              "summary": {
                                "totalDecisions": 35,
                                "approved": 22,
                                "approvedWithRestrictions": 8,
                                "blocked": 5,
                                "incidentsAfterApproval": 2,
                                "falsePositives": 3,
                                "falseNegatives": 4
                              },
                              "keyIndicators": {
                                "accuracyRate": 74.3,
                                "falsePositiveRate": 8.6,
                                "falseNegativeRate": 11.4,
                                "safeChangeBlockedRate": 10.0,
                                "incidentAfterApprovalRate": 5.7
                              },
                              "topRiskDrivers": [],
                              "alerts": [
                                {
                                  "type": "HIGH_INCIDENT_RATE",
                                  "severity": "WARNING",
                                  "message": "Taxa de incidentes após aprovação está em 5.7% (limite: 3%)",
                                  "affectedEntity": "SYSTEM",
                                  "impactRate": 5.7
                                },
                                {
                                  "type": "FALSE_NEGATIVE_DOMINANCE",
                                  "severity": "WARNING",
                                  "message": "Falsos negativos (11.4%) superam falsos positivos (8.6%). Sistema pode estar muito permissivo.",
                                  "affectedEntity": "SYSTEM",
                                  "impactRate": 11.4
                                }
                              ],
                              "recommendation": "Sistema requer atenção. 2 alerta(s) identificado(s). Recomenda-se revisar regras problemáticas e ajustar pesos de risco conforme US #22."
                            }
                            """
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<ExecutiveRiskDashboardResponse> getExecutiveDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,
            
            @RequestParam(required = false)
            String environment,
            
            @RequestParam(required = false)
            String focus) {
        
        ExecutiveRiskDashboardResponse dashboard = dashboardService.generateDashboard(
                from, to, environment, focus);
        
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/executive/health")
    @Operation(
        summary = "Verifica saúde do serviço de dashboard executivo",
        description = "Endpoint simples para verificar se o serviço de dashboard está disponível"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Executive Dashboard Service is running (read-only mode)");
    }
}
