package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.RiskComparisonRequest;
import com.backoffice.alerta.dto.RiskComparisonResponse;
import com.backoffice.alerta.service.RiskComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/risk")
@Tag(name = "Análise de Risco", description = "Endpoints para análise de risco de Pull Requests")
public class RiskComparisonController {

    private final RiskComparisonService riskComparisonService;

    public RiskComparisonController(RiskComparisonService riskComparisonService) {
        this.riskComparisonService = riskComparisonService;
    }

    @PostMapping("/compare")
    @Operation(
        summary = "Comparar risco entre versões de regras",
        description = "Compara o score de risco de um Pull Request entre duas versões de regras. " +
                      "Executa duas análises independentes e retorna a diferença (delta) entre os scores, " +
                      "além de uma explicação das principais diferenças entre as versões."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Comparação realizada com sucesso",
            content = @Content(schema = @Schema(implementation = RiskComparisonResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida (versão inexistente ou campos obrigatórios ausentes)"
        )
    })
    public ResponseEntity<RiskComparisonResponse> compareRisk(@Valid @RequestBody RiskComparisonRequest request) {
        RiskComparisonResponse response = riskComparisonService.compareRisk(request);
        return ResponseEntity.ok(response);
    }
}
