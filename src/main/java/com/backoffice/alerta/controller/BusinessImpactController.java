package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.BusinessImpactRequest;
import com.backoffice.alerta.dto.BusinessImpactResponse;
import com.backoffice.alerta.service.BusinessImpactAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para análise de impacto de negócio em Pull Requests
 */
@RestController
@RequestMapping("/risk/business-impact")
@Tag(name = "Business Impact Analysis", description = "API para análise de impacto de negócio em Pull Requests")
public class BusinessImpactController {

    private final BusinessImpactAnalysisService service;

    public BusinessImpactController(BusinessImpactAnalysisService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Analisar impacto de negócio de Pull Request",
        description = "Identifica quais regras de negócio são impactadas pelos arquivos alterados em um Pull Request, " +
                     "calculando o nível de risco e fornecendo explicações claras em linguagem de negócio."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Análise concluída com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BusinessImpactResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos na requisição",
            content = @Content(mediaType = "application/json")
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Dados do Pull Request para análise de impacto",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BusinessImpactRequest.class),
            examples = @ExampleObject(
                name = "Exemplo de análise de impacto",
                value = """
                    {
                      "pullRequestId": "PR-123",
                      "changedFiles": [
                        "src/main/java/com/app/payment/PaymentService.java",
                        "src/main/java/com/app/order/OrderController.java"
                      ]
                    }
                    """
            )
        )
    )
    public ResponseEntity<BusinessImpactResponse> analyzeBusinessImpact(
            @RequestBody BusinessImpactRequest request) {
        try {
            BusinessImpactResponse response = service.analyze(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
