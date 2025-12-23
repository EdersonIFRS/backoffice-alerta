package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.RiskReportRequest;
import com.backoffice.alerta.dto.RiskReportResponse;
import com.backoffice.alerta.service.RiskReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para geração de relatórios consolidados de risco.
 * Agrega análise técnica, política, explicação executiva e recomendações em um único artefato.
 */
@RestController
@RequestMapping("/risk")
@Tag(name = "Relatório Consolidado", description = "Gera relatório de risco unificado para CI/CD, auditoria e comitês de mudança")
public class RiskReportController {

    private final RiskReportService reportService;

    public RiskReportController(RiskReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/report")
    @Operation(
        summary = "Gera relatório consolidado de risco",
        description = "Gera relatório consolidado de risco para uso em CI/CD, auditoria e comitês de mudança. " +
                     "Agrega em uma única resposta: análise técnica, decisão de política, explicação executiva " +
                     "em linguagem de negócio e recomendações de mitigação. Não expõe scores numéricos."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Relatório consolidado gerado com sucesso"
    )
    public ResponseEntity<RiskReportResponse> generateReport(@RequestBody RiskReportRequest request) {
        RiskReportResponse response = reportService.generateReport(request);
        return ResponseEntity.ok(response);
    }
}
