package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.PullRequestRequest;
import com.backoffice.alerta.dto.RiskAnalysisResponse;
import com.backoffice.alerta.service.RiskAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
@Tag(name = "Análise de Risco", description = "Endpoints para análise de risco de Pull Requests")
public class RiskController {

    private final RiskAnalysisService riskAnalysisService;

    public RiskController(RiskAnalysisService riskAnalysisService) {
        this.riskAnalysisService = riskAnalysisService;
    }

    @PostMapping("/analyze")
    @Operation(
        summary = "Analisar risco de Pull Request",
        description = "Calcula o score de risco de um Pull Request. A engine infere automaticamente: " +
                      "(1) criticidade baseada no caminho do arquivo (billing, payment, pricing, order); " +
                      "(2) histórico de incidentes por módulo; " +
                      "(3) pontuação baseada em linhas alteradas e presença de testes por arquivo."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Análise realizada com sucesso",
        content = @Content(schema = @Schema(implementation = RiskAnalysisResponse.class))
    )
    public ResponseEntity<RiskAnalysisResponse> analyzeRisk(@Valid @RequestBody PullRequestRequest request) {
        RiskAnalysisResponse response = riskAnalysisService.analyzeRisk(request);
        return ResponseEntity.ok(response);
    }
}
