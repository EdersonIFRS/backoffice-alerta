package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.*;
import com.backoffice.alerta.rules.RiskAdjustmentSuggestion;
import com.backoffice.alerta.service.RiskLearningAdjustmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para análise de aprendizado e sugestões de ajuste de risco
 * 
 * IMPORTANTE: Este endpoint é APENAS CONSULTIVO (Human-in-the-Loop)
 * - NÃO modifica regras de negócio automaticamente
 * - NÃO recalcula riscos passados
 * - NÃO altera auditorias ou decisões
 * - Apenas gera sugestões baseadas em aprendizado organizacional
 * - Humanos decidem se aplicam ou não as sugestões
 */
@RestController
@RequestMapping("/risk/learning")
@Tag(name = "Learning", description = "Aprendizado organizacional e sugestões de ajuste de risco (Human-in-the-Loop)")
public class RiskLearningAdjustmentController {

    private final RiskLearningAdjustmentService learningService;

    public RiskLearningAdjustmentController(RiskLearningAdjustmentService learningService) {
        this.learningService = learningService;
    }

    @PostMapping("/adjustments")
    @Operation(
        summary = "Analisa histórico e gera sugestões de ajuste de risco",
        description = "Realiza análise de aprendizado organizacional através de feedbacks pós-deploy (US#21), " +
                     "auditorias de decisões (US#20) e histórico de incidentes (US#17). " +
                     "Detecta padrões como falsos positivos, falsos negativos e incidentes recorrentes. " +
                     "Gera sugestões de ajuste com nível de confiança e evidências explicáveis. " +
                     "\n\n**IMPORTANTE:** As sugestões são apenas consultivas. " +
                     "Nenhuma mudança é aplicada automaticamente. " +
                     "Gestores e engenheiros devem revisar e decidir se aplicam os ajustes.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Parâmetros da análise de aprendizado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RiskAdjustmentAnalysisRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Análise padrão - últimos 30 dias",
                        value = """
                        {
                          "timeWindowDays": 30,
                          "minimumConfidence": 50
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Análise estendida - últimos 90 dias",
                        value = """
                        {
                          "timeWindowDays": 90,
                          "minimumConfidence": 70
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Análise conservadora - alta confiança",
                        value = """
                        {
                          "timeWindowDays": 60,
                          "minimumConfidence": 85
                        }
                        """
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Análise concluída com sucesso. Sugestões geradas.",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RiskAdjustmentAnalysisResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Parâmetros inválidos (timeWindowDays <= 0 ou minimumConfidence fora de 0-100)"
            )
        }
    )
    public ResponseEntity<RiskAdjustmentAnalysisResponse> analyzeLearning(
            @RequestBody RiskAdjustmentAnalysisRequest request) {
        
        try {
            // Gera sugestões baseadas em aprendizado
            List<RiskAdjustmentSuggestion> suggestions = learningService.analyzeLearning(request);

            // Converte para DTOs
            List<RiskAdjustmentSuggestionResponse> suggestionResponses = suggestions.stream()
                .map(RiskAdjustmentSuggestionResponse::new)
                .collect(Collectors.toList());

            // Monta resposta completa
            String summary = buildAnalysisSummary(suggestions, request.getTimeWindowDays());
            
            RiskAdjustmentAnalysisResponse response = new RiskAdjustmentAnalysisResponse(
                suggestionResponses,
                0, // Será preenchido com métricas reais
                0,
                0,
                request.getTimeWindowDays(),
                summary
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Verifica saúde do serviço de aprendizado",
        description = "Endpoint simples para verificar se o serviço de aprendizado está disponível"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Learning Service is running (consultative mode)");
    }

    /**
     * Constrói resumo da análise
     */
    private String buildAnalysisSummary(List<RiskAdjustmentSuggestion> suggestions, int timeWindowDays) {
        if (suggestions.isEmpty()) {
            return String.format(
                "Análise dos últimos %d dias não detectou padrões que justifiquem ajustes. " +
                "Sistema está operando dentro dos parâmetros esperados.",
                timeWindowDays
            );
        }

        long highConfidence = suggestions.stream()
            .filter(s -> s.getConfidenceLevel() >= 80)
            .count();

        return String.format(
            "Análise dos últimos %d dias detectou %d padrão(ões) que sugerem ajustes de risco. " +
            "%d sugestão(ões) com alta confiança (>=80%%). " +
            "Recomenda-se revisar e aplicar ajustes conforme governança organizacional.",
            timeWindowDays,
            suggestions.size(),
            highConfidence
        );
    }
}
