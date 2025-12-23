package com.backoffice.alerta.llm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * US#71 - Controller de comparação de impacto PRE vs POST
 * 
 * Endpoint READ-ONLY para comparar impacto antes e depois de mudança
 */
@RestController
@RequestMapping("/risk/llm/impact")
@Tag(name = "LLM Impact Comparison", description = "US#71 - Comparação de impacto PRE vs POST (determinístico, READ-ONLY)")
public class LLMImpactComparisonController {

    private static final Logger log = LoggerFactory.getLogger(LLMImpactComparisonController.class);

    private final LLMImpactComparisonService comparisonService;

    public LLMImpactComparisonController(LLMImpactComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @PostMapping("/compare")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER', 'ENGINEER')")
    @Operation(
        summary = "Compara impacto PRE vs POST de uma mudança",
        description = """
            **US#71 - Comparação de Impacto**
            
            Analisa 4 dimensões de comparação:
            - **AST**: Complexidade ciclomática, profundidade de chamadas
            - **RAG**: Scores semânticos, fallback rates
            - **BUSINESS**: Regras críticas impactadas
            - **TESTS**: Cobertura de testes em arquivos críticos
            
            **Veredito**:
            - DEGRADED: Qualquer dimensão degradada com delta relevante
            - IMPROVED: Duas ou mais dimensões melhoradas
            - UNCHANGED: Caso contrário
            
            **CI/CD Exit Codes**:
            - IMPROVED → 0
            - UNCHANGED → 1
            - DEGRADED → 2
            
            **Características**:
            - ✅ READ-ONLY (não persiste dados)
            - ✅ Determinístico (sem IA/ML)
            - ✅ Fail-safe (erro → UNCHANGED)
            - ✅ Auditável (logs detalhados)
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comparação realizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LLMImpactComparisonResponse.class),
                examples = {
                    @ExampleObject(
                        name = "IMPROVED - Melhoria detectada",
                        description = "Duas ou mais dimensões melhoradas",
                        value = """
                            {
                              "finalScoreDelta": 25,
                              "finalVerdict": "IMPROVED",
                              "deltas": [
                                {
                                  "dimension": "AST",
                                  "metric": "cyclomaticComplexity",
                                  "beforeValue": 3.4,
                                  "afterValue": 2.1,
                                  "delta": -1.3,
                                  "interpretation": "IMPROVED"
                                },
                                {
                                  "dimension": "TESTS",
                                  "metric": "criticalFilesWithoutTests",
                                  "beforeValue": 5.0,
                                  "afterValue": 2.0,
                                  "delta": -3.0,
                                  "interpretation": "IMPROVED"
                                }
                              ],
                              "executiveSummary": "✅ **Melhoria Detectada**\\n\\nA mudança reduziu complexidade e/ou melhorou qualidade em múltiplas dimensões.",
                              "baseRef": "main",
                              "compareRef": "123"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "DEGRADED - Degradação detectada",
                        description = "Pelo menos uma dimensão degradada",
                        value = """
                            {
                              "finalScoreDelta": -35,
                              "finalVerdict": "DEGRADED",
                              "deltas": [
                                {
                                  "dimension": "AST",
                                  "metric": "cyclomaticComplexity",
                                  "beforeValue": 2.1,
                                  "afterValue": 5.8,
                                  "delta": 3.7,
                                  "interpretation": "DEGRADED"
                                },
                                {
                                  "dimension": "RAG",
                                  "metric": "fallbackRate",
                                  "beforeValue": 15.0,
                                  "afterValue": 42.0,
                                  "delta": 27.0,
                                  "interpretation": "DEGRADED"
                                }
                              ],
                              "executiveSummary": "🚨 **Degradação Detectada**\\n\\nA mudança aumentou a complexidade técnica e/ou reduziu a qualidade em uma ou mais dimensões. Isto pode indicar código gerado automaticamente sem revisão adequada.",
                              "baseRef": "main",
                              "compareRef": "456"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "UNCHANGED - Sem mudanças significativas",
                        description = "Variações não relevantes ou métricas neutras",
                        value = """
                            {
                              "finalScoreDelta": 0,
                              "finalVerdict": "UNCHANGED",
                              "deltas": [
                                {
                                  "dimension": "AST",
                                  "metric": "cyclomaticComplexity",
                                  "beforeValue": 2.1,
                                  "afterValue": 2.3,
                                  "delta": 0.2,
                                  "interpretation": "NEUTRAL"
                                }
                              ],
                              "executiveSummary": "➡️ **Sem Mudança Significativa**\\n\\nA mudança não apresentou variações relevantes nas métricas analisadas.",
                              "baseRef": "main",
                              "compareRef": "789"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(responseCode = "400", description = "Requisição inválida"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN, RISK_MANAGER ou ENGINEER)"),
        @ApiResponse(responseCode = "500", description = "Erro interno (fail-safe retorna UNCHANGED)")
    })
    public ResponseEntity<LLMImpactComparisonResponse> compareImpact(
            @Valid @RequestBody LLMImpactComparisonRequest request) {

        log.info("📊 [US#71] Request de comparação recebido | base={} | compare={}",
                 request.getBaseRef(), request.getCompareRef());

        try {
            LLMImpactComparisonResponse response = comparisonService.compareImpact(request);

            log.info("✅ [US#71] Comparação concluída | verdict={} | deltas={}",
                     response.getFinalVerdict(), response.getDeltas().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ [US#71] Erro ao comparar impacto: {}", e.getMessage(), e);

            // Fail-safe: retornar UNCHANGED em caso de erro
            LLMImpactComparisonResponse fallback = new LLMImpactComparisonResponse();
            fallback.setFinalScoreDelta(0);
            fallback.setFinalVerdict("UNCHANGED");
            fallback.setExecutiveSummary("⚠️ Erro ao comparar impacto. Assumindo UNCHANGED e recomendando revisão manual.");
            fallback.setBaseRef(request.getBaseRef());
            fallback.setCompareRef(request.getCompareRef());

            return ResponseEntity.status(HttpStatus.OK).body(fallback);
        }
    }
}
