package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.RiskDecisionRequest;
import com.backoffice.alerta.dto.RiskDecisionResponse;
import com.backoffice.alerta.service.RiskDecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para motor de decisão de aprovação de mudanças
 */
@RestController
@RequestMapping("/risk/decision")
@Tag(name = "Risk Decision Engine", description = "Motor inteligente de decisão de aprovação de mudanças")
public class RiskDecisionController {

    private final RiskDecisionService service;

    public RiskDecisionController(RiskDecisionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Decidir sobre aprovação de mudança",
        description = "Analisa todos os riscos de um Pull Request e decide automaticamente se deve ser " +
                     "APROVADO, APROVADO_COM_RESTRIÇÕES ou BLOQUEADO. " +
                     "Considera risco técnico, impacto de negócio, histórico de incidentes, ambiente e tipo de mudança."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Decisão tomada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RiskDecisionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos na requisição",
            content = @Content(mediaType = "application/json")
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Dados do Pull Request para decisão",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = RiskDecisionRequest.class),
            examples = {
                @ExampleObject(
                    name = "Exemplo 1: Feature em Produção",
                    value = """
                        {
                          "pullRequestId": "PR-123",
                          "ruleVersion": "v2",
                          "environment": "PRODUCTION",
                          "changeType": "FEATURE",
                          "policy": {
                            "maxAllowedRisk": "MEDIO",
                            "allowConditionalApproval": true
                          },
                          "changedFiles": [
                            "src/main/java/com/app/payment/PaymentService.java",
                            "src/main/java/com/app/order/OrderController.java"
                          ]
                        }
                        """
                ),
                @ExampleObject(
                    name = "Exemplo 2: Hotfix Urgente",
                    value = """
                        {
                          "pullRequestId": "PR-456",
                          "ruleVersion": "v2",
                          "environment": "PRODUCTION",
                          "changeType": "HOTFIX",
                          "policy": {
                            "maxAllowedRisk": "ALTO",
                            "allowConditionalApproval": true
                          },
                          "changedFiles": [
                            "src/main/java/com/app/billing/BillingService.java"
                          ]
                        }
                        """
                ),
                @ExampleObject(
                    name = "Exemplo 3: Mudança em DEV",
                    value = """
                        {
                          "pullRequestId": "PR-789",
                          "environment": "DEV",
                          "changeType": "REFACTOR",
                          "changedFiles": [
                            "src/main/java/com/app/utils/StringHelper.java"
                          ]
                        }
                        """
                )
            }
        )
    )
    public ResponseEntity<RiskDecisionResponse> makeDecision(@RequestBody RiskDecisionRequest request) {
        try {
            RiskDecisionResponse response = service.decide(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
