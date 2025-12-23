package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.*;
import com.backoffice.alerta.rules.ChangeType;
import com.backoffice.alerta.rules.Environment;
import com.backoffice.alerta.service.DecisionHistoricalComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Controller para comparação histórica de decisões de risco
 * 
 * US#41 - Comparação Histórica de Decisões de Risco
 * 
 * GOVERNANÇA:
 * ✅ READ-ONLY
 * ✅ Determinístico
 * ✅ Sem persistência
 * ✅ RBAC: ADMIN, RISK_MANAGER
 * 
 * @author Copilot
 */
@RestController
@RequestMapping("/risk/decision")
@Tag(name = "Comparação Histórica", description = "Análise de padrões em decisões de risco similares")
public class DecisionHistoricalComparisonController {
    
    private final DecisionHistoricalComparisonService comparisonService;
    
    public DecisionHistoricalComparisonController(DecisionHistoricalComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }
    
    @PostMapping("/historical-comparison")
    @Operation(
        summary = "Comparar decisão atual com histórico similar",
        description = """
            Compara uma mudança atual com decisões históricas semelhantes, identificando:
            - Padrões de similaridade (arquivos, regras, ambiente, tipo de mudança)
            - Resultados reais (incidentes, feedbacks, SLAs)
            - Insights executivos determinísticos
            
            **Pontuação de Similaridade (0-100):**
            - Arquivos em comum: até 40 pontos
            - Regras de negócio em comum: até 30 pontos
            - Mesmo ambiente: +10 pontos
            - Mesmo changeType: +10 pontos
            - Recência (mais recente = maior peso): até 10 pontos
            
            **Critério de Inclusão:** similarityScore >= 60
            
            **IMPORTANTE:**
            - Endpoint READ-ONLY (não persiste dados)
            - Análise determinística (sem IA)
            - Reutiliza dados de US#16, #17, #20, #21, #28
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comparação histórica gerada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DecisionHistoricalComparisonResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Exemplo 1: Histórico de incidentes em produção",
                        description = "Mudança em pagamento com padrão recorrente de incidentes",
                        value = """
                            {
                              "currentContextSummary": {
                                "riskLevel": "ALTO",
                                "finalDecision": "PENDING",
                                "businessDomains": ["PAYMENT"],
                                "criticalRules": 2
                              },
                              "historicalComparisons": [
                                {
                                  "pullRequestId": "PR-2024-123",
                                  "similarityScore": 87,
                                  "decision": "APPROVE",
                                  "riskLevel": "MEDIO",
                                  "environment": "PRODUCTION",
                                  "outcome": "FALSE_NEGATIVE_RISK",
                                  "incidentSeverity": "CRITICAL",
                                  "slaBreached": true,
                                  "summary": "Decisão: APPROVE | Risco: MEDIO | Resultado: FALSE_NEGATIVE_RISK | Incidente: CRITICAL | SLA VIOLADO"
                                },
                                {
                                  "pullRequestId": "PR-2024-098",
                                  "similarityScore": 75,
                                  "decision": "APPROVE",
                                  "riskLevel": "ALTO",
                                  "environment": "PRODUCTION",
                                  "outcome": "FALSE_NEGATIVE_RISK",
                                  "incidentSeverity": "HIGH",
                                  "slaBreached": false,
                                  "summary": "Decisão: APPROVE | Risco: ALTO | Resultado: FALSE_NEGATIVE_RISK | Incidente: HIGH"
                                }
                              ],
                              "executiveInsights": {
                                "patternDetected": true,
                                "patternDescription": "Alterações similares resultaram em 2 incidente(s) após aprovação. Sistema subestimou risco em 2 caso(s) similares. Domínios afetados: Pagamento. Padrão recorrente em ambiente de PRODUÇÃO.",
                                "recommendation": "Implementar feature flag para rollback rápido; Executar testes de regressão completos; Considerar deploy gradual (canary/blue-green); Atenção: 2 regra(s) crítica(s) impactada(s) - revisão sênior obrigatória; Validar em staging antes de produção."
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Exemplo 2: Histórico seguro",
                        description = "Mudança sem histórico negativo",
                        value = """
                            {
                              "currentContextSummary": {
                                "riskLevel": "BAIXO",
                                "finalDecision": "PENDING",
                                "businessDomains": ["USER"],
                                "criticalRules": 0
                              },
                              "historicalComparisons": [
                                {
                                  "pullRequestId": "PR-2024-200",
                                  "similarityScore": 82,
                                  "decision": "APPROVE",
                                  "riskLevel": "BAIXO",
                                  "environment": "STAGING",
                                  "outcome": "CORRECT_APPROVAL",
                                  "incidentSeverity": null,
                                  "slaBreached": false,
                                  "summary": "Decisão: APPROVE | Risco: BAIXO | Resultado: CORRECT_APPROVAL"
                                }
                              ],
                              "executiveInsights": {
                                "patternDetected": false,
                                "patternDescription": "Histórico similar mostra taxa aceitável de sucesso nas decisões anteriores.",
                                "recommendation": "Seguir processo padrão de revisão e deploy conforme nível de risco identificado."
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Exemplo 3: Falsos positivos históricos",
                        description = "Histórico de bloqueios desnecessários",
                        value = """
                            {
                              "currentContextSummary": {
                                "riskLevel": "MEDIO",
                                "finalDecision": "PENDING",
                                "businessDomains": ["ORDER"],
                                "criticalRules": 1
                              },
                              "historicalComparisons": [
                                {
                                  "pullRequestId": "PR-2024-150",
                                  "similarityScore": 78,
                                  "decision": "BLOCK",
                                  "riskLevel": "ALTO",
                                  "environment": "PRODUCTION",
                                  "outcome": "FALSE_POSITIVE_BLOCK",
                                  "incidentSeverity": null,
                                  "slaBreached": false,
                                  "summary": "Decisão: BLOCK | Risco: ALTO | Resultado: FALSE_POSITIVE_BLOCK"
                                },
                                {
                                  "pullRequestId": "PR-2024-142",
                                  "similarityScore": 71,
                                  "decision": "BLOCK",
                                  "riskLevel": "ALTO",
                                  "environment": "PRODUCTION",
                                  "outcome": "FALSE_POSITIVE_BLOCK",
                                  "incidentSeverity": null,
                                  "slaBreached": false,
                                  "summary": "Decisão: BLOCK | Risco: ALTO | Resultado: FALSE_POSITIVE_BLOCK"
                                }
                              ],
                              "executiveInsights": {
                                "patternDetected": false,
                                "patternDescription": "Histórico similar mostra taxa aceitável de sucesso nas decisões anteriores.",
                                "recommendation": "Seguir processo padrão de revisão e deploy conforme nível de risco identificado."
                              }
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request inválido (validação falhou)"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado (apenas ADMIN ou RISK_MANAGER)"
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Dados da mudança atual para comparação",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = DecisionHistoricalComparisonRequest.class),
            examples = {
                @ExampleObject(
                    name = "Request mínimo",
                    value = """
                        {
                          "currentPullRequestId": "PR-2024-001",
                          "changedFiles": [
                            "src/payment/PaymentService.java",
                            "src/payment/PaymentValidator.java"
                          ]
                        }
                        """
                ),
                @ExampleObject(
                    name = "Request completo",
                    value = """
                        {
                          "currentPullRequestId": "PR-2024-001",
                          "environment": "PRODUCTION",
                          "changeType": "FEATURE",
                          "changedFiles": [
                            "src/payment/PaymentService.java",
                            "src/payment/PaymentValidator.java",
                            "src/billing/BillingIntegration.java"
                          ],
                          "lookbackDays": 90,
                          "maxComparisons": 3
                        }
                        """
                )
            }
        )
    )
    public ResponseEntity<DecisionHistoricalComparisonResponse> compareWithHistorical(
            @Valid @RequestBody DecisionHistoricalComparisonRequest request) {
        
        DecisionHistoricalComparisonResponse response = 
                comparisonService.compareWithHistorical(request);
        
        return ResponseEntity.ok(response);
    }
}
