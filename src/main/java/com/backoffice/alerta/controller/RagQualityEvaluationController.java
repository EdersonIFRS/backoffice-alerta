package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.*;
import com.backoffice.alerta.service.RagQualityEvaluationService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * US#67 - Controller de Avaliação de Qualidade do RAG.
 * 
 * Endpoints para monitoramento e análise de qualidade do sistema RAG.
 * 
 * Princípios:
 * - READ-ONLY
 * - Determinístico
 * - Sem side-effects
 * - RBAC: ADMIN e RISK_MANAGER
 */
@RestController
@RequestMapping("/risk/rag/quality")
@Tag(name = "RAG Quality Evaluation", description = "US#67 - Avaliação de qualidade do RAG (Enterprise)")
public class RagQualityEvaluationController {

    private final RagQualityEvaluationService qualityService;

    public RagQualityEvaluationController(RagQualityEvaluationService qualityService) {
        this.qualityService = qualityService;
    }

    @GetMapping
    @Operation(
        summary = "Obter métricas de qualidade do RAG",
        description = "Retorna métricas agregadas de qualidade (GLOBAL ou SCOPED por projeto). " +
                      "Inclui fallbackRate, avgSemanticScore, hybridMatchRate, etc.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Métricas calculadas com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RagQualityMetricsResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "Métricas GLOBAL",
                            value = """
                                {
                                  "fallbackRate": 0.15,
                                  "avgSemanticScore": 0.67,
                                  "avgKeywordScore": 2.3,
                                  "hybridMatchRate": 0.45,
                                  "semanticOnlyRate": 0.35,
                                  "keywordOnlyRate": 0.20,
                                  "fallbackInclusionRate": 0.10,
                                  "confidenceMismatchRate": 0.05,
                                  "projectContext": null,
                                  "totalQueriesEvaluated": 120,
                                  "totalRulesEvaluated": 25
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Métricas SCOPED",
                            value = """
                                {
                                  "fallbackRate": 0.08,
                                  "avgSemanticScore": 0.75,
                                  "avgKeywordScore": 3.1,
                                  "hybridMatchRate": 0.62,
                                  "semanticOnlyRate": 0.28,
                                  "keywordOnlyRate": 0.10,
                                  "fallbackInclusionRate": 0.05,
                                  "confidenceMismatchRate": 0.02,
                                  "projectContext": "550e8400-e29b-41d4-a716-446655440000",
                                  "totalQueriesEvaluated": 85,
                                  "totalRulesEvaluated": 12
                                }
                                """
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<RagQualityMetricsResponse> getQualityMetrics(
            @Parameter(description = "ID do projeto (opcional, null = GLOBAL)")
            @RequestParam(required = false) UUID projectId) {
        
        RagQualityMetricsResponse metrics = qualityService.getQualityMetrics(projectId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/projects")
    @Operation(
        summary = "Obter métricas de qualidade por projeto",
        description = "Retorna métricas de qualidade para cada projeto, permitindo comparação.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Métricas por projeto calculadas com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RagProjectQualityResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "Métricas de múltiplos projetos",
                            value = """
                                [
                                  {
                                    "projectId": "550e8400-e29b-41d4-a716-446655440000",
                                    "projectName": "Backoffice Pagamentos",
                                    "metrics": {
                                      "fallbackRate": 0.08,
                                      "avgSemanticScore": 0.75,
                                      "avgKeywordScore": 3.1,
                                      "hybridMatchRate": 0.62,
                                      "semanticOnlyRate": 0.28,
                                      "keywordOnlyRate": 0.10,
                                      "fallbackInclusionRate": 0.05,
                                      "confidenceMismatchRate": 0.02,
                                      "projectContext": "550e8400-e29b-41d4-a716-446655440000",
                                      "totalQueriesEvaluated": 85,
                                      "totalRulesEvaluated": 12
                                    }
                                  },
                                  {
                                    "projectId": "550e8400-e29b-41d4-a716-446655440002",
                                    "projectName": "Portal do Cliente",
                                    "metrics": {
                                      "fallbackRate": 0.12,
                                      "avgSemanticScore": 0.68,
                                      "avgKeywordScore": 2.4,
                                      "hybridMatchRate": 0.52,
                                      "semanticOnlyRate": 0.33,
                                      "keywordOnlyRate": 0.15,
                                      "fallbackInclusionRate": 0.08,
                                      "confidenceMismatchRate": 0.04,
                                      "projectContext": "550e8400-e29b-41d4-a716-446655440002",
                                      "totalQueriesEvaluated": 65,
                                      "totalRulesEvaluated": 8
                                    }
                                  }
                                ]
                                """
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<List<RagProjectQualityResponse>> getQualityByProject() {
        List<RagProjectQualityResponse> projectMetrics = qualityService.getQualityByProject();
        return ResponseEntity.ok(projectMetrics);
    }

    @GetMapping("/rules")
    @Operation(
        summary = "Obter qualidade por regra de negócio",
        description = "Retorna métricas de qualidade para cada regra de negócio, " +
                      "identificando regras com baixa performance ou alta dependência de fallback.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Qualidade por regra calculada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RagRuleQualityResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "Qualidade de múltiplas regras",
                            value = """
                                [
                                  {
                                    "businessRuleId": "550e8400-e29b-41d4-a716-446655440001",
                                    "businessRuleName": "REGRA_CALCULO_HORAS_PJ",
                                    "avgSemanticScore": 0.78,
                                    "keywordDependencyRate": 0.15,
                                    "fallbackInclusionRate": 0.05,
                                    "occurrences": 45,
                                    "observations": "Excelente performance semântica."
                                  },
                                  {
                                    "businessRuleId": "550e8400-e29b-41d4-a716-446655440003",
                                    "businessRuleName": "REGRA_CALCULO_TRIBUTOS",
                                    "avgSemanticScore": 0.42,
                                    "keywordDependencyRate": 0.55,
                                    "fallbackInclusionRate": 0.18,
                                    "occurrences": 38,
                                    "observations": "Performance semântica baixa, considerar ajuste de embeddings. Alta dependência de keywords. Inclusão frequente via fallback."
                                  },
                                  {
                                    "businessRuleId": "550e8400-e29b-41d4-a716-446655440004",
                                    "businessRuleName": "REGRA_VALIDACAO_CADASTRO_USUARIO",
                                    "avgSemanticScore": 0.65,
                                    "keywordDependencyRate": 0.22,
                                    "fallbackInclusionRate": 0.08,
                                    "occurrences": 52,
                                    "observations": "Performance semântica adequada."
                                  }
                                ]
                                """
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<List<RagRuleQualityResponse>> getQualityByRule(
            @Parameter(description = "ID do projeto (opcional, null = GLOBAL)")
            @RequestParam(required = false) UUID projectId) {
        
        List<RagRuleQualityResponse> ruleMetrics = qualityService.getQualityByRule(projectId);
        return ResponseEntity.ok(ruleMetrics);
    }

    @GetMapping("/trends")
    @Operation(
        summary = "Obter tendências de qualidade ao longo do tempo",
        description = "Retorna série temporal de métricas de qualidade (fallbackRate e avgSemanticScore).",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tendências calculadas com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RagQualityTrendPoint.class),
                    examples = {
                        @ExampleObject(
                            name = "Tendências de 7 dias",
                            value = """
                                [
                                  {
                                    "date": "2025-12-15",
                                    "fallbackRate": 0.18,
                                    "avgSemanticScore": 0.62,
                                    "queryCount": 42
                                  },
                                  {
                                    "date": "2025-12-16",
                                    "fallbackRate": 0.15,
                                    "avgSemanticScore": 0.65,
                                    "queryCount": 38
                                  },
                                  {
                                    "date": "2025-12-17",
                                    "fallbackRate": 0.12,
                                    "avgSemanticScore": 0.68,
                                    "queryCount": 45
                                  },
                                  {
                                    "date": "2025-12-18",
                                    "fallbackRate": 0.10,
                                    "avgSemanticScore": 0.71,
                                    "queryCount": 50
                                  },
                                  {
                                    "date": "2025-12-19",
                                    "fallbackRate": 0.11,
                                    "avgSemanticScore": 0.69,
                                    "queryCount": 47
                                  },
                                  {
                                    "date": "2025-12-20",
                                    "fallbackRate": 0.09,
                                    "avgSemanticScore": 0.73,
                                    "queryCount": 52
                                  },
                                  {
                                    "date": "2025-12-21",
                                    "fallbackRate": 0.08,
                                    "avgSemanticScore": 0.75,
                                    "queryCount": 48
                                  }
                                ]
                                """
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<List<RagQualityTrendPoint>> getQualityTrends(
            @Parameter(description = "ID do projeto (opcional, null = GLOBAL)")
            @RequestParam(required = false) UUID projectId,
            
            @Parameter(description = "Data inicial (default: 7 dias atrás)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @Parameter(description = "Data final (default: hoje)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        List<RagQualityTrendPoint> trends = qualityService.getQualityTrends(projectId, fromDate, toDate);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Health check do serviço de qualidade",
        description = "Verifica se o serviço de avaliação de qualidade está operacional.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Serviço operacional",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "Health UP",
                            value = """
                                {
                                  "status": "UP",
                                  "service": "RagQualityEvaluationService",
                                  "queriesAvailable": 120,
                                  "timestamp": "2025-12-21"
                                }
                                """
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> health = qualityService.getHealthStatus();
        return ResponseEntity.ok(health);
    }
}
