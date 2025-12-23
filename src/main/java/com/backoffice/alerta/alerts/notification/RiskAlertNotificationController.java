package com.backoffice.alerta.alerts.notification;

import com.backoffice.alerta.alerts.notification.dto.RiskAlertNotificationRequest;
import com.backoffice.alerta.alerts.notification.dto.RiskAlertNotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller para notificações de alertas de risco
 * 
 * US#56 - Alertas Inteligentes via Slack / Microsoft Teams
 * 
 * PRINCÍPIOS:
 * - READ-ONLY: apenas consultas e envios de notificações
 * - RBAC: apenas ADMIN e RISK_MANAGER
 * - Validação: @Valid nos DTOs
 * - Documentação: Swagger completo
 */
@RestController
@RequestMapping("/risk/alerts/notify")
@Tag(name = "Notificações de Alertas", description = "Envio de alertas para Slack/Microsoft Teams")
@PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
public class RiskAlertNotificationController {

    private static final Logger log = LoggerFactory.getLogger(RiskAlertNotificationController.class);

    private final RiskAlertNotificationService notificationService;

    public RiskAlertNotificationController(RiskAlertNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/{alertId}")
    @Operation(
        summary = "Envia notificação de alerta",
        description = """
            Envia um alerta de risco para Slack ou Microsoft Teams via webhook.
            
            **Regras:**
            - Apenas alertas WARNING e CRITICAL são notificados (INFO retorna SKIPPED)
            - Não persiste dados (READ-ONLY)
            - Webhook configurado pelo cliente
            - Fallback seguro em caso de erro
            
            **Severidades notificadas:**
            - CRITICAL: alta prioridade
            - WARNING: média prioridade
            - INFO: ignorado (SKIPPED)
            
            **RBAC:** Apenas ADMIN e RISK_MANAGER podem enviar notificações
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Notificação processada (SENT, FAILED ou SKIPPED)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RiskAlertNotificationResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Sucesso",
                        value = """
                            {
                              "alertId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                              "channel": "SLACK",
                              "status": "SENT",
                              "sentAt": "2024-01-15T10:30:00Z",
                              "errorMessage": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Falha",
                        value = """
                            {
                              "alertId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                              "channel": "TEAMS",
                              "status": "FAILED",
                              "sentAt": "2024-01-15T10:30:00Z",
                              "errorMessage": "Webhook inválido ou inacessível"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Ignorado (INFO)",
                        value = """
                            {
                              "alertId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                              "channel": "SLACK",
                              "status": "SKIPPED",
                              "sentAt": "2024-01-15T10:30:00Z",
                              "errorMessage": "Alerta com severidade INFO não é notificado"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN ou RISK_MANAGER)")
    })
    public ResponseEntity<RiskAlertNotificationResponse> notifyAlert(
            @Parameter(description = "UUID do alerta a ser notificado", required = true)
            @PathVariable UUID alertId,
            
            @Parameter(
                description = "Dados da notificação (canal e webhook)",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = RiskAlertNotificationRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "Slack",
                            value = """
                                                                {
                                                                    "channel": "SLACK",
                                                                    "webhookUrl": "<REDACTED_SLACK_WEBHOOK>"
                                                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Teams",
                            value = """
                                {
                                  "channel": "TEAMS",
                                  "webhookUrl": "https://outlook.office.com/webhook/..."
                                }
                                """
                        )
                    }
                )
            )
            @Valid @RequestBody RiskAlertNotificationRequest request
    ) {
        log.info("📬 POST /risk/alerts/notify/{} - channel: {}", alertId, request.getChannel());
        
        RiskAlertNotificationResponse response = notificationService.notifyAlert(alertId, request);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Verifica saúde do serviço de notificações",
        description = """
            Retorna status do serviço e canais disponíveis.
            
            **Status possíveis:**
            - UP: Serviço funcionando corretamente
            - DOWN: Erro interno
            
            **RBAC:** Apenas ADMIN e RISK_MANAGER
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Status do serviço",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "status": "UP",
                          "availableChannels": 2,
                          "supportedChannels": ["SLACK", "TEAMS"],
                          "timestamp": "2024-01-15T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<RiskAlertNotificationService.NotificationHealthResponse> healthCheck() {
        log.info("🏥 GET /risk/alerts/notify/health");
        
        RiskAlertNotificationService.NotificationHealthResponse health = 
            notificationService.checkHealth();
        
        return ResponseEntity.ok(health);
    }
}
