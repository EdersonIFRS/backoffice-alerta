package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.RiskDecisionFeedbackRequest;
import com.backoffice.alerta.dto.RiskDecisionFeedbackResponse;
import com.backoffice.alerta.rules.RiskDecisionFeedback;
import com.backoffice.alerta.service.RiskDecisionFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller REST para feedback humano pós-deploy sobre decisões de risco
 * 
 * IMPORTANTE: Este endpoint NÃO modifica decisões, riscos ou auditorias
 * - Apenas registra feedback humano para aprendizado organizacional
 * - Feedback é opcional e pós-deploy
 * - Relação 1:1 com auditoria (um feedback por auditoria)
 */
@RestController
@RequestMapping("/risk/feedback")
@Tag(name = "Feedback", description = "Feedback humano pós-deploy sobre decisões de risco (não altera decisões)")
public class RiskDecisionFeedbackController {

    private final RiskDecisionFeedbackService feedbackService;

    public RiskDecisionFeedbackController(RiskDecisionFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @Operation(
        summary = "Registra feedback humano pós-deploy",
        description = "Permite que engenheiros, SREs ou gestores registrem o que realmente aconteceu " +
                     "após o deploy de uma mudança. Este feedback é usado para aprendizado organizacional " +
                     "e melhoria contínua das decisões de risco. " +
                     "IMPORTANTE: Não modifica a decisão original nem a auditoria.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do feedback",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RiskDecisionFeedbackRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Deploy bem-sucedido",
                        value = """
                        {
                          "auditId": "550e8400-e29b-41d4-a716-446655440000",
                          "outcome": "SUCCESS",
                          "comments": "Deploy ocorreu sem problemas. Todas as métricas dentro do esperado. Nenhum alerta disparado.",
                          "author": "john.doe@company.com"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Incidente em produção",
                        value = """
                        {
                          "auditId": "550e8400-e29b-41d4-a716-446655440000",
                          "outcome": "INCIDENT",
                          "comments": "Incidente P1 - 30 minutos de indisponibilidade parcial. Taxa de erro aumentou para 15%. Problema identificado no módulo de pagamentos.",
                          "author": "sre-team@company.com"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Risco subestimado",
                        value = """
                        {
                          "auditId": "550e8400-e29b-41d4-a716-446655440000",
                          "outcome": "FALSE_NEGATIVE_RISK",
                          "comments": "Mudança foi aprovada como baixo risco, mas causou degradação de performance em produção. Sistema subestimou impacto na regra de fraude.",
                          "author": "tech-lead@company.com"
                        }
                        """
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Feedback criado com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RiskDecisionFeedbackResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dados inválidos (campos obrigatórios ausentes)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Auditoria não encontrada"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Já existe feedback para esta auditoria"
            )
        }
    )
    public ResponseEntity<RiskDecisionFeedbackResponse> createFeedback(
            @RequestBody RiskDecisionFeedbackRequest request) {
        
        try {
            RiskDecisionFeedback feedback = feedbackService.createFeedback(request);
            RiskDecisionFeedbackResponse response = new RiskDecisionFeedbackResponse(feedback);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            // 404 ou 400 dependendo da mensagem
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
            
        } catch (IllegalStateException e) {
            // 409 - Conflito (feedback duplicado)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping
    @Operation(
        summary = "Lista todos os feedbacks",
        description = "Retorna todos os feedbacks humanos registrados, ordenados por data " +
                     "(mais recentes primeiro). Útil para análise de padrões e aprendizado organizacional.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de feedbacks recuperada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RiskDecisionFeedbackResponse.class))
                )
            )
        }
    )
    public ResponseEntity<List<RiskDecisionFeedbackResponse>> getAllFeedbacks() {
        List<RiskDecisionFeedback> feedbacks = feedbackService.findAll();
        
        List<RiskDecisionFeedbackResponse> responses = feedbacks.stream()
            .map(RiskDecisionFeedbackResponse::new)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/audit/{auditId}")
    @Operation(
        summary = "Busca feedback por ID da auditoria",
        description = "Retorna o feedback associado a uma auditoria específica, se existir. " +
                     "Cada auditoria pode ter no máximo um feedback (relação 1:1).",
        parameters = {
            @Parameter(
                name = "auditId",
                description = "ID da auditoria",
                example = "550e8400-e29b-41d4-a716-446655440000",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Feedback encontrado",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RiskDecisionFeedbackResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Feedback não encontrado para esta auditoria"
            )
        }
    )
    public ResponseEntity<RiskDecisionFeedbackResponse> getFeedbackByAuditId(
            @PathVariable UUID auditId) {
        
        Optional<RiskDecisionFeedback> feedback = feedbackService.findByAuditId(auditId);
        
        return feedback
            .map(RiskDecisionFeedbackResponse::new)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    @Operation(
        summary = "Verifica saúde do serviço de feedback",
        description = "Endpoint simples para verificar se o serviço de feedback está disponível"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Feedback Service is running");
    }
}
