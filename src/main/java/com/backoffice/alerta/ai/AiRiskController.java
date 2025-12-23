package com.backoffice.alerta.ai;

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
@RequestMapping("/risk/analyze")
@Tag(name = "Análise Inteligente", description = "Análise inteligente simulada baseada em heurísticas (não utiliza IA generativa)")
public class AiRiskController {

    private final AiChangeAnalysisService aiChangeAnalysisService;

    public AiRiskController(AiChangeAnalysisService aiChangeAnalysisService) {
        this.aiChangeAnalysisService = aiChangeAnalysisService;
    }

    @PostMapping("/ai")
    @Operation(
        summary = "Análise inteligente de mudança",
        description = "Executa análise inteligente simulada baseada em heurísticas para identificar sinais qualitativos " +
                      "sobre o impacto da mudança. A análise infere automaticamente: tipo de mudança (lógica, estrutural, cosmética), " +
                      "impacto de negócio (alto, médio, baixo) e nível de atenção recomendado. " +
                      "IMPORTANTE: Esta análise NÃO utiliza IA generativa real, apenas heurísticas determinísticas. " +
                      "NÃO substitui as regras de risco e NÃO altera scores automaticamente.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do Pull Request para análise inteligente",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AiAnalysisRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Mudança em módulo crítico com muitas linhas",
                        description = "Alteração em billing com mais de 100 linhas",
                        value = """
                        {
                          "pullRequestId": "PR-12345",
                          "files": [
                            {
                              "filePath": "src/main/java/com/app/billing/PaymentService.java",
                              "linesChanged": 120
                            }
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Mudança cosmética em módulo de suporte",
                        description = "Pequena alteração em arquivo de utilidade",
                        value = """
                        {
                          "pullRequestId": "PR-67890",
                          "files": [
                            {
                              "filePath": "src/main/java/com/app/util/StringHelper.java",
                              "linesChanged": 15
                            }
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Mudança estrutural em API",
                        description = "Alteração moderada em controller",
                        value = """
                        {
                          "pullRequestId": "PR-11111",
                          "files": [
                            {
                              "filePath": "src/main/java/com/app/api/OrderController.java",
                              "linesChanged": 75
                            },
                            {
                              "filePath": "src/main/java/com/app/order/OrderService.java",
                              "linesChanged": 45
                            }
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Mudança em múltiplos módulos críticos",
                        description = "Alterações em billing e payment",
                        value = """
                        {
                          "pullRequestId": "PR-22222",
                          "files": [
                            {
                              "filePath": "src/main/java/com/app/billing/BillingService.java",
                              "linesChanged": 85
                            },
                            {
                              "filePath": "src/main/java/com/app/payment/PaymentProcessor.java",
                              "linesChanged": 60
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
                description = "Análise realizada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AiAnalysisResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "Alta atenção - Lógica crítica",
                            value = """
                            {
                              "pullRequestId": "PR-12345",
                              "aiAssessment": {
                                "summary": "Mudança altera lógica sensível de billing",
                                "confidence": 0.85,
                                "recommendedAttention": "ALTA",
                                "signals": [
                                  "HIGH_BUSINESS_IMPACT",
                                  "LOGIC_CHANGE"
                                ]
                              }
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Baixa atenção - Mudança cosmética",
                            value = """
                            {
                              "pullRequestId": "PR-67890",
                              "aiAssessment": {
                                "summary": "Mudança de baixo impacto com alterações cosméticas",
                                "confidence": 0.6,
                                "recommendedAttention": "BAIXA",
                                "signals": [
                                  "LOW_BUSINESS_IMPACT",
                                  "COSMETIC_CHANGE"
                                ]
                              }
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Média atenção - Mudança estrutural",
                            value = """
                            {
                              "pullRequestId": "PR-11111",
                              "aiAssessment": {
                                "summary": "Mudança altera lógica sensível de order",
                                "confidence": 0.85,
                                "recommendedAttention": "ALTA",
                                "signals": [
                                  "HIGH_BUSINESS_IMPACT",
                                  "LOGIC_CHANGE"
                                ]
                              }
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
    public ResponseEntity<AiAnalysisResponse> analyzeWithAi(
        @Valid @RequestBody AiAnalysisRequest request
    ) {
        AiAnalysisResponse response = aiChangeAnalysisService.analyzeChange(request);
        return ResponseEntity.ok(response);
    }
}
