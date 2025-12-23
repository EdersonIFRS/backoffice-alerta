package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.RiskRecommendationRequest;
import com.backoffice.alerta.dto.RiskRecommendationResponse;
import com.backoffice.alerta.service.RiskRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/risk")
@Tag(name = "Recomendações de Risco", description = "Endpoints para sugerir ações de mitigação de risco")
public class RiskRecommendationController {

    private final RiskRecommendationService riskRecommendationService;

    public RiskRecommendationController(RiskRecommendationService riskRecommendationService) {
        this.riskRecommendationService = riskRecommendationService;
    }

    @PostMapping("/recommend")
    @Operation(
        summary = "Obter recomendações de mitigação de risco",
        description = "Analisa o Pull Request e sugere ações concretas para reduzir o risco até o nível desejado. " +
                      "As recomendações são baseadas nos fatores que mais impactam o score: " +
                      "ausência de testes, muitas linhas alteradas, arquivos críticos e histórico de incidentes. " +
                      "O endpoint calcula o impacto estimado de cada recomendação e projeta o score esperado " +
                      "após aplicar as ações sugeridas.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do Pull Request e nível de risco desejado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RiskRecommendationRequest.class),
                examples = {
                    @ExampleObject(
                        name = "PR CRÍTICO - Alvo MÉDIO",
                        description = "Pull Request com risco crítico buscando reduzir para médio",
                        value = """
                        {
                          "pullRequestId": "PR-12345",
                          "ruleVersion": "v2",
                          "targetRiskLevel": "MÉDIO",
                          "files": [
                            {
                              "filePath": "src/main/java/com/billing/BillingService.java",
                              "linesChanged": 150,
                              "hasTest": false
                            }
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "PR ALTO - Alvo BAIXO",
                        description = "Pull Request com múltiplos arquivos buscando risco baixo",
                        value = """
                        {
                          "pullRequestId": "PR-67890",
                          "ruleVersion": "v2",
                          "targetRiskLevel": "BAIXO",
                          "files": [
                            {
                              "filePath": "src/main/java/com/payment/PaymentController.java",
                              "linesChanged": 120,
                              "hasTest": false
                            },
                            {
                              "filePath": "src/main/java/com/order/OrderService.java",
                              "linesChanged": 85,
                              "hasTest": false
                            }
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "PR MÉDIO - Alvo BAIXO",
                        description = "Pull Request com arquivo não crítico",
                        value = """
                        {
                          "pullRequestId": "PR-11111",
                          "ruleVersion": "v1",
                          "targetRiskLevel": "BAIXO",
                          "files": [
                            {
                              "filePath": "src/main/java/com/util/StringHelper.java",
                              "linesChanged": 65,
                              "hasTest": false
                            }
                          ]
                        }
                        """
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Recomendações geradas com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RiskRecommendationResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "Exemplo de resposta com múltiplas recomendações",
                            value = """
                            {
                              "pullRequestId": "PR-12345",
                              "ruleVersion": "v2",
                              "currentRiskScore": 95,
                              "currentRiskLevel": "CRÍTICO",
                              "targetRiskLevel": "MÉDIO",
                              "recommendations": [
                                {
                                  "action": "ADICIONAR_TESTES",
                                  "description": "Adicionar testes automatizados para BillingService.java",
                                  "estimatedImpact": -25
                                },
                                {
                                  "action": "REDUZIR_LINHAS_ALTERADAS",
                                  "description": "Reduzir alterações para menos de 100 linhas por arquivo",
                                  "estimatedImpact": -20
                                },
                                {
                                  "action": "SEGMENTAR_MODULO",
                                  "description": "Considerar segmentar módulo crítico: BillingService.java",
                                  "estimatedImpact": -30
                                }
                              ],
                              "expectedRiskScore": 20,
                              "expectedRiskLevel": "BAIXO"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Exemplo com histórico de incidentes",
                            value = """
                            {
                              "pullRequestId": "PR-67890",
                              "ruleVersion": "v2",
                              "currentRiskScore": 85,
                              "currentRiskLevel": "CRÍTICO",
                              "targetRiskLevel": "MÉDIO",
                              "recommendations": [
                                {
                                  "action": "ADICIONAR_TESTES",
                                  "description": "Adicionar testes automatizados para 2 arquivo(s)",
                                  "estimatedImpact": -25
                                },
                                {
                                  "action": "REDUZIR_LINHAS_ALTERADAS",
                                  "description": "Reduzir alterações para menos de 100 linhas por arquivo",
                                  "estimatedImpact": -20
                                },
                                {
                                  "action": "REVISAO_MANUAL",
                                  "description": "Solicitar revisão manual de especialista para arquivos com histórico elevado",
                                  "estimatedImpact": -20
                                }
                              ],
                              "expectedRiskScore": 20,
                              "expectedRiskLevel": "BAIXO"
                            }
                            """
                        )
                    }
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dados inválidos na requisição"
            )
        }
    )
    public ResponseEntity<RiskRecommendationResponse> recommendActions(
        @Valid @RequestBody RiskRecommendationRequest request
    ) {
        RiskRecommendationResponse response = riskRecommendationService.recommendActions(request);
        return ResponseEntity.ok(response);
    }
}
