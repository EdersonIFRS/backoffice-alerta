package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.RiskExecutiveRequest;
import com.backoffice.alerta.dto.RiskExecutiveResponse;
import com.backoffice.alerta.service.RiskExecutiveExplanationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para geração de explicações executivas de risco.
 * Traduz análises técnicas em linguagem de negócio para gestores e stakeholders.
 */
@RestController
@RequestMapping("/risk")
@Tag(name = "Explicação Executiva", description = "Gera explicações de risco em linguagem executiva/negócio")
public class RiskExecutiveController {

    private final RiskExecutiveExplanationService executiveService;

    public RiskExecutiveController(RiskExecutiveExplanationService executiveService) {
        this.executiveService = executiveService;
    }

    @PostMapping("/explain")
    @Operation(
        summary = "Gera explicação executiva de risco",
        description = "Orquestra análise técnica, política e IA para gerar explicação de risco " +
                     "em linguagem não-técnica voltada para gestores e áreas de negócio. " +
                     "Não expõe scores técnicos, focando em impacto operacional e ações recomendadas."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Explicação executiva gerada com sucesso"
    )
    public ResponseEntity<RiskExecutiveResponse> explainRisk(@RequestBody RiskExecutiveRequest request) {
        RiskExecutiveResponse response = executiveService.generateExecutiveExplanation(request);
        return ResponseEntity.ok(response);
    }
}
