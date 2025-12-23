package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.RiskSimulationRequest;
import com.backoffice.alerta.dto.RiskSimulationResponse;
import com.backoffice.alerta.service.RiskSimulationService;
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
@Tag(name = "Simulação de Risco", description = "Endpoints para simulação de impacto em Pull Requests")
public class RiskSimulationController {

    private final RiskSimulationService riskSimulationService;

    public RiskSimulationController(RiskSimulationService riskSimulationService) {
        this.riskSimulationService = riskSimulationService;
    }

    @PostMapping("/simulate")
    @Operation(
        summary = "Simular impacto de mudanças no risco",
        description = "Permite testar cenários hipotéticos como 'E se eu adicionar testes?' ou 'E se eu reduzir as linhas alteradas?'. " +
                      "O endpoint compara o risco atual vs. o risco simulado após aplicar as mudanças.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do Pull Request e parâmetros da simulação",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RiskSimulationRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Simular adição de testes",
                        value = """
                        {
                          "pullRequestId": "PR-12345",
                          "ruleVersion": "v2",
                          "baseFiles": [
                            {
                              "filePath": "src/main/java/com/billing/BillingService.java",
                              "linesChanged": 150,
                              "hasTest": false
                            }
                          ],
                          "simulation": {
                            "applyTests": true
                          }
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Simular redução de linhas",
                        value = """
                        {
                          "pullRequestId": "PR-12345",
                          "ruleVersion": "v2",
                          "baseFiles": [
                            {
                              "filePath": "src/main/java/com/billing/BillingService.java",
                              "linesChanged": 150,
                              "hasTest": false
                            }
                          ],
                          "simulation": {
                            "overrideLinesChanged": 40
                          }
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Simular testes + redução de linhas",
                        value = """
                        {
                          "pullRequestId": "PR-12345",
                          "ruleVersion": "v2",
                          "baseFiles": [
                            {
                              "filePath": "src/main/java/com/billing/BillingService.java",
                              "linesChanged": 150,
                              "hasTest": false
                            }
                          ],
                          "simulation": {
                            "applyTests": true,
                            "overrideLinesChanged": 40
                          }
                        }
                        """
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Simulação realizada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RiskSimulationResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dados inválidos na requisição"
            )
        }
    )
    public ResponseEntity<RiskSimulationResponse> simulateRisk(
        @Valid @RequestBody RiskSimulationRequest request
    ) {
        RiskSimulationResponse response = riskSimulationService.simulateRisk(request);
        return ResponseEntity.ok(response);
    }
}
