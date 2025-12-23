package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.RiskPolicyRequest;
import com.backoffice.alerta.dto.RiskPolicyResponse;
import com.backoffice.alerta.service.RiskPolicyService;
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
@Tag(name = "Política de Risco", description = "Endpoints para avaliar Pull Requests contra políticas de aceite de risco")
public class RiskPolicyController {

    private final RiskPolicyService riskPolicyService;

    public RiskPolicyController(RiskPolicyService riskPolicyService) {
        this.riskPolicyService = riskPolicyService;
    }

    @PostMapping("/evaluate")
    @Operation(
        summary = "Avaliar Pull Request contra política de risco",
        description = "Executa análise de risco e avalia se o Pull Request deve ser APROVADO, " +
                      "exigir REVISÃO OBRIGATÓRIA ou ser BLOQUEADO com base no nível de risco calculado. " +
                      "A política define o nível máximo aceitável (BAIXO, MÉDIO, ALTO ou CRÍTICO). " +
                      "Se o risco estiver dentro do limite, é APROVADO. " +
                      "Se estiver um nível acima, exige REVISÃO OBRIGATÓRIA. " +
                      "Se estiver dois ou mais níveis acima, é BLOQUEADO.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do Pull Request e política de aceite de risco",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RiskPolicyRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Política permissiva (máximo MÉDIO) - Arquivo crítico sem testes",
                        value = """
                        {
                          "pullRequestId": "PR-12345",
                          "ruleVersion": "v2",
                          "policy": {
                            "maxAllowedRisk": "MÉDIO"
                          },
                          "files": [
                            {
                              "filePath": "src/main/java/com/billing/BillingService.java",
                              "linesChanged": 120,
                              "hasTest": false
                            }
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Política restritiva (máximo BAIXO) - Arquivo com teste",
                        value = """
                        {
                          "pullRequestId": "PR-67890",
                          "ruleVersion": "v2",
                          "policy": {
                            "maxAllowedRisk": "BAIXO"
                          },
                          "files": [
                            {
                              "filePath": "src/main/java/com/util/StringHelper.java",
                              "linesChanged": 30,
                              "hasTest": true
                            }
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Política moderada (máximo ALTO) - Múltiplos arquivos",
                        value = """
                        {
                          "pullRequestId": "PR-11111",
                          "ruleVersion": "v2",
                          "policy": {
                            "maxAllowedRisk": "ALTO"
                          },
                          "files": [
                            {
                              "filePath": "src/main/java/com/payment/PaymentController.java",
                              "linesChanged": 80,
                              "hasTest": true
                            },
                            {
                              "filePath": "src/main/java/com/payment/PaymentService.java",
                              "linesChanged": 45,
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
                description = "Avaliação realizada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RiskPolicyResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "Exemplo de PR APROVADO",
                            value = """
                            {
                              "pullRequestId": "PR-12345",
                              "ruleVersion": "v2",
                              "riskScore": 45,
                              "riskLevel": "MÉDIO",
                              "policyDecision": "APROVADO",
                              "reason": "O nível de risco MÉDIO está dentro do limite permitido (MÉDIO)"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Exemplo de PR com REVISÃO OBRIGATÓRIA",
                            value = """
                            {
                              "pullRequestId": "PR-12345",
                              "ruleVersion": "v2",
                              "riskScore": 65,
                              "riskLevel": "ALTO",
                              "policyDecision": "REVISÃO OBRIGATÓRIA",
                              "reason": "O nível de risco ALTO está um nível acima do permitido (MÉDIO), exige revisão manual"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Exemplo de PR BLOQUEADO",
                            value = """
                            {
                              "pullRequestId": "PR-12345",
                              "ruleVersion": "v2",
                              "riskScore": 85,
                              "riskLevel": "CRÍTICO",
                              "policyDecision": "BLOQUEADO",
                              "reason": "O nível de risco CRÍTICO excede o máximo permitido (MÉDIO)"
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
    public ResponseEntity<RiskPolicyResponse> evaluatePolicy(
        @Valid @RequestBody RiskPolicyRequest request
    ) {
        RiskPolicyResponse response = riskPolicyService.evaluatePolicy(request);
        return ResponseEntity.ok(response);
    }
}
