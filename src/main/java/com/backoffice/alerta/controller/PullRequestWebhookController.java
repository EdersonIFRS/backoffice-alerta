package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.PullRequestWebhookRequest;
import com.backoffice.alerta.dto.PullRequestWebhookResponse;
import com.backoffice.alerta.service.PullRequestWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para webhooks de Pull Request (CI/CD)
 * 
 * Permite integração automática com pipelines de GitHub Actions, GitLab CI, etc.
 * O pipeline pode decidir baseado em finalDecision:
 * - BLOQUEADO → falhar o build
 * - APROVADO_COM_RESTRICOES → gerar warning mas permitir
 * - APROVADO → seguir normalmente
 * 
 * IMPORTANTE: Apenas orquestra serviços existentes
 * - NÃO cria lógica de risco
 * - NÃO recalcula métricas
 * - NÃO chama IA
 */
@RestController
@RequestMapping("/webhooks")
@Tag(name = "Webhooks", description = "Integração CI/CD via webhooks de Pull Request")
public class PullRequestWebhookController {

    private final PullRequestWebhookService webhookService;

    public PullRequestWebhookController(PullRequestWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/pull-request")
    @Operation(
        summary = "Processa webhook de Pull Request e retorna decisão de risco",
        description = "Endpoint para integração com pipelines CI/CD (GitHub Actions, GitLab CI, etc.). " +
                     "Recebe dados do Pull Request, executa análise de impacto e decisão de risco, " +
                     "e retorna a decisão para o pipeline decidir se permite o merge. " +
                     "\n\n**Orquestração de serviços:**" +
                     "\n1. Valida payload do webhook" +
                     "\n2. Executa análise de impacto (US #16)" +
                     "\n3. Executa decisão de risco (US #18)" +
                     "\n4. Auditoria criada automaticamente (US #20)" +
                     "\n5. Retorna decisão + auditId" +
                     "\n\n**Pipeline CI/CD pode decidir:**" +
                     "\n- `BLOQUEADO` → Falhar o build" +
                     "\n- `APROVADO_COM_RESTRICOES` → Warning mas permite" +
                     "\n- `APROVADO` → Segue normalmente" +
                     "\n\n**Campos obrigatórios:**" +
                     "\n- provider (GITHUB ou GITLAB)" +
                     "\n- pullRequest.id" +
                     "\n- changedFiles (não vazio)" +
                     "\n- environment" +
                     "\n- changeType",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload do webhook de Pull Request",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PullRequestWebhookRequest.class),
                examples = {
                    @ExampleObject(
                        name = "PR de alto risco em PRODUCTION",
                        value = """
                        {
                          "provider": "GITHUB",
                          "eventType": "CREATED",
                          "projectId": "backoffice-core",
                          "repository": "github.com/company/backoffice-core",
                          "pullRequest": {
                            "id": "PR-8745",
                            "title": "Refatoração crítica do módulo de pagamentos",
                            "author": "john.doe",
                            "targetBranch": "main",
                            "sourceBranch": "feature/payment-refactor"
                          },
                          "environment": "PRODUCTION",
                          "changeType": "FEATURE",
                          "changedFiles": [
                            "src/payment/PaymentProcessor.java",
                            "src/payment/TransactionValidator.java",
                            "src/core/DatabaseConnection.java"
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "PR médio risco em STAGING",
                        value = """
                        {
                          "provider": "GITLAB",
                          "eventType": "UPDATED",
                          "projectId": "backoffice-api",
                          "repository": "gitlab.com/company/backoffice-api",
                          "pullRequest": {
                            "id": "MR-523",
                            "title": "Adicionar validação de email",
                            "author": "jane.smith",
                            "targetBranch": "staging",
                            "sourceBranch": "feature/email-validation"
                          },
                          "environment": "STAGING",
                          "changeType": "FEATURE",
                          "changedFiles": [
                            "src/validation/EmailValidator.java",
                            "src/utils/StringUtils.java"
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "PR baixo risco em DEV",
                        value = """
                        {
                          "provider": "GITHUB",
                          "eventType": "CREATED",
                          "projectId": "backoffice-frontend",
                          "repository": "github.com/company/backoffice-frontend",
                          "pullRequest": {
                            "id": "PR-1234",
                            "title": "Atualizar README com exemplos",
                            "author": "bob.jones",
                            "targetBranch": "develop",
                            "sourceBranch": "docs/readme-update"
                          },
                          "environment": "DEV",
                          "changeType": "DOCUMENTATION",
                          "changedFiles": [
                            "README.md",
                            "docs/getting-started.md"
                          ]
                        }
                        """
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Webhook processado com sucesso. Decisão de risco retornada.",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PullRequestWebhookResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Payload inválido (campos obrigatórios ausentes ou vazios)"
            )
        }
    )
    public ResponseEntity<PullRequestWebhookResponse> processPullRequestWebhook(
            @RequestBody PullRequestWebhookRequest request) {
        
        try {
            PullRequestWebhookResponse response = webhookService.processWebhook(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Validação falhou - retorna 400 Bad Request
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pull-request/health")
    @Operation(
        summary = "Verifica saúde do serviço de webhook",
        description = "Endpoint simples para verificar se o webhook está disponível"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Webhook service is UP");
    }
}
