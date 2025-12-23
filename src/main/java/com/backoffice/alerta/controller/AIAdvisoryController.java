package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.AIAdvisoryRequest;
import com.backoffice.alerta.dto.AIAdvisoryResponse;
import com.backoffice.alerta.dto.ImpactedBusinessRuleSummary;
import com.backoffice.alerta.rules.ChangeType;
import com.backoffice.alerta.rules.Environment;
import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskLevel;
import com.backoffice.alerta.service.AIAdvisoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para análise consultiva de IA
 * 
 * IMPORTANTE: Esta camada é APENAS CONSULTIVA
 * - NÃO modifica decisões de aprovação
 * - NÃO altera níveis de risco
 * - NÃO aprova ou bloqueia mudanças
 * - Apenas fornece insights baseados em dados já processados
 */
@RestController
@RequestMapping("/risk/ai-advisory")
@Tag(name = "AI Advisory", description = "Camada consultiva de IA para análise de risco (não altera decisões)")
public class AIAdvisoryController {

    private final AIAdvisoryService aiAdvisoryService;

    public AIAdvisoryController(AIAdvisoryService aiAdvisoryService) {
        this.aiAdvisoryService = aiAdvisoryService;
    }

    @PostMapping
    @Operation(
        summary = "Gera análise consultiva de IA sobre decisão de risco",
        description = "Fornece insights em linguagem natural sobre uma decisão já tomada pelo motor de decisão. " +
                     "Esta análise é PURAMENTE CONSULTIVA e não modifica a decisão original. " +
                     "Útil para comunicação com stakeholders e contexto estratégico.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Contexto da decisão já tomada pelo sistema",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AIAdvisoryRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Mudança de Produção com Alto Risco",
                        value = """
                        {
                          "pullRequestId": "PR-12345",
                          "environment": "PRODUCTION",
                          "changeType": "FEATURE",
                          "riskLevel": "ALTO",
                          "finalDecision": "APROVADO_COM_RESTRICOES",
                          "impactedBusinessRules": [
                            {
                              "ruleId": "PAY-001",
                              "name": "Processamento de Pagamentos",
                              "criticality": "CRITICA",
                              "impactType": "DIRECT",
                              "incidentCount": 2
                            }
                          ],
                          "mandatoryActions": [
                            "Aprovação do diretor de tecnologia obrigatória",
                            "Testes de carga devem ser executados",
                            "Rollback plan deve estar documentado"
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Hotfix Crítico",
                        value = """
                        {
                          "pullRequestId": "PR-54321",
                          "environment": "PRODUCTION",
                          "changeType": "HOTFIX",
                          "riskLevel": "CRITICO",
                          "finalDecision": "APROVADO_COM_RESTRICOES",
                          "impactedBusinessRules": [
                            {
                              "ruleId": "AUTH-001",
                              "name": "Autenticação de Usuários",
                              "criticality": "CRITICA",
                              "impactType": "DIRECT",
                              "incidentCount": 0
                            }
                          ],
                          "mandatoryActions": [
                            "CTO deve aprovar",
                            "Deploy deve ser realizado fora do horário comercial",
                            "Equipe de plantão deve estar disponível"
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Mudança de Baixo Risco",
                        value = """
                        {
                          "pullRequestId": "PR-99999",
                          "environment": "DEV",
                          "changeType": "REFACTOR",
                          "riskLevel": "BAIXO",
                          "finalDecision": "APROVADO",
                          "impactedBusinessRules": [],
                          "mandatoryActions": []
                        }
                        """
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Análise consultiva gerada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AIAdvisoryResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dados inválidos na requisição"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Erro ao gerar análise consultiva (fallback será retornado)"
            )
        }
    )
    public ResponseEntity<AIAdvisoryResponse> generateAdvisory(@RequestBody AIAdvisoryRequest request) {
        AIAdvisoryResponse response = aiAdvisoryService.generateAdvisory(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Verifica saúde da camada consultiva de IA",
        description = "Endpoint simples para verificar se o serviço consultivo está disponível"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Advisory Service is running (consultative layer)");
    }
}
