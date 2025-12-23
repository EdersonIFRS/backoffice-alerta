package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.NotificationSummaryResponse;
import com.backoffice.alerta.dto.RiskNotificationResponse;
import com.backoffice.alerta.service.RiskNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller READ-ONLY para consultar notificações organizacionais
 * 
 * US#27 - Notificações Organizacionais Baseadas em Ownership
 * 
 * Permite consultar eventos de notificação gerados automaticamente
 * após decisões de risco que requerem atenção dos times responsáveis.
 * 
 * ⚠️ READ-ONLY - não permite criar/editar notificações manualmente
 * ⚠️ Notificações são geradas automaticamente pelo sistema
 */
@RestController
@RequestMapping("/risk/notifications")
@Tag(name = "Risk Notifications", description = "Consulta de notificações organizacionais (READ-ONLY)")
public class RiskNotificationController {

    private final RiskNotificationService notificationService;

    public RiskNotificationController(RiskNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(
        summary = "Listar todas as notificações",
        description = """
            Retorna todas as notificações organizacionais geradas pelo sistema,
            ordenadas por data de criação (mais recentes primeiro).
            
            **Notificações são geradas automaticamente quando:**
            - Decisão final = BLOQUEADO
            - Decisão final = APROVADO_COM_RESTRICOES
            - Risco ALTO ou CRÍTICO em PRODUÇÃO
            - Histórico de incidentes críticos
            
            **Ownerships notificados:**
            - PRIMARY_OWNER → sempre notificado
            - SECONDARY_OWNER → sempre notificado se existir
            - BACKUP → apenas se PRIMARY não existir OU ambiente = PRODUCTION
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de notificações retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RiskNotificationResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "BLOQUEADO em PRODUÇÃO - Finance PRIMARY",
                            description = "Mudança bloqueada por alto risco em produção, time de finanças como responsável primário",
                            value = """
                                [{
                                  "id": "123e4567-e89b-12d3-a456-426614174000",
                                  "auditId": "550e8400-e29b-41d4-a716-446655440000",
                                  "pullRequestId": "PR-789",
                                  "businessRuleId": "BR-001",
                                  "teamName": "Time de Pagamentos",
                                  "teamType": "FINANCE",
                                  "ownershipRole": "PRIMARY_OWNER",
                                  "notificationTrigger": "RISK_BLOCKED",
                                  "severity": "CRITICAL",
                                  "channel": "EMAIL",
                                  "message": "🔔 NOTIFICAÇÃO DE RISCO - Bloqueado\\n\\n📋 Pull Request: PR-789\\n🏢 Ambiente: Produção\\n⚠️ Nível de Risco: Crítico\\n\\n📌 Regra de Negócio Impactada:\\n   - ID: BR-001\\n   - Nome: Processamento de Pagamentos\\n   - Domínio: Pagamento\\n   - Criticidade: Crítica\\n\\n💡 Motivo: Risco crítico detectado para ambiente de produção",
                                  "createdAt": "2024-03-15T14:30:00Z"
                                }]
                                """
                        ),
                        @ExampleObject(
                            name = "APROVADO_COM_RESTRIÇÕES - Engenharia SECONDARY",
                            description = "Mudança aprovada com restrições, time de engenharia como suporte secundário",
                            value = """
                                [{
                                  "id": "223e4567-e89b-12d3-a456-426614174001",
                                  "auditId": "660e8400-e29b-41d4-a716-446655440001",
                                  "pullRequestId": "PR-456",
                                  "businessRuleId": "BR-002",
                                  "teamName": "Engenharia Backend",
                                  "teamType": "ENGINEERING",
                                  "ownershipRole": "SECONDARY_OWNER",
                                  "notificationTrigger": "RISK_RESTRICTED",
                                  "severity": "WARNING",
                                  "channel": "SLACK",
                                  "message": "🔔 NOTIFICAÇÃO DE RISCO - Aprovado com Restrições\\n\\n📋 Pull Request: PR-456\\n🏢 Ambiente: Staging\\n⚠️ Nível de Risco: Alto\\n\\n📌 Regra de Negócio Impactada:\\n   - ID: BR-002\\n   - Nome: Validação de Email\\n   - Domínio: Autenticação\\n   - Criticidade: Alta\\n\\n✅ Ações Obrigatórias:\\n   • Revisão manual obrigatória por especialista sênior\\n   • Plano de rollback documentado e testado",
                                  "createdAt": "2024-03-15T15:00:00Z"
                                }]
                                """
                        ),
                        @ExampleObject(
                            name = "Regra sem PRIMARY - BACKUP acionado",
                            description = "Regra crítica sem PRIMARY_OWNER, BACKUP é notificado em PRODUÇÃO",
                            value = """
                                [{
                                  "id": "323e4567-e89b-12d3-a456-426614174002",
                                  "auditId": "770e8400-e29b-41d4-a716-446655440002",
                                  "pullRequestId": "PR-999",
                                  "businessRuleId": "BR-003",
                                  "teamName": "Time de Segurança - Backup",
                                  "teamType": "SECURITY",
                                  "ownershipRole": "BACKUP",
                                  "notificationTrigger": "HIGH_RISK_PRODUCTION",
                                  "severity": "CRITICAL",
                                  "channel": "WEBHOOK",
                                  "message": "🔔 NOTIFICAÇÃO DE RISCO - Aprovado com Restrições\\n\\n📋 Pull Request: PR-999\\n🏢 Ambiente: Produção\\n⚠️ Nível de Risco: Crítico\\n\\n📌 Regra de Negócio Impactada:\\n   - ID: BR-003\\n   - Nome: Controle de Acesso\\n   - Domínio: Segurança\\n   - Criticidade: Crítica\\n\\n⚠️ ALERTA: Esta regra possui 3 incidente(s) crítico(s) registrado(s).",
                                  "createdAt": "2024-03-15T16:00:00Z"
                                }]
                                """
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<List<RiskNotificationResponse>> listAllNotifications() {
        List<RiskNotificationResponse> notifications = notificationService.listAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/audit/{auditId}")
    @Operation(
        summary = "Listar notificações por auditoria",
        description = "Retorna todas as notificações geradas para uma auditoria específica (US#20)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Notificações da auditoria retornadas com sucesso"
            )
        }
    )
    public ResponseEntity<List<RiskNotificationResponse>> listByAuditId(
            @Parameter(description = "ID da auditoria (US#20)", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID auditId) {
        
        List<RiskNotificationResponse> notifications = notificationService.listByAuditId(auditId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/team/{teamName}")
    @Operation(
        summary = "Listar notificações por time",
        description = "Retorna todas as notificações direcionadas a um time específico",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Notificações do time retornadas com sucesso"
            )
        }
    )
    public ResponseEntity<List<RiskNotificationResponse>> listByTeam(
            @Parameter(description = "Nome do time", example = "Time de Pagamentos")
            @PathVariable String teamName) {
        
        List<RiskNotificationResponse> notifications = notificationService.listByTeam(teamName);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/summary")
    @Operation(
        summary = "Obter resumo de notificações",
        description = "Retorna estatísticas agregadas de todas as notificações geradas",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Resumo retornado com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationSummaryResponse.class)
                )
            )
        }
    )
    public ResponseEntity<NotificationSummaryResponse> getSummary() {
        NotificationSummaryResponse summary = notificationService.getSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Health check do serviço de notificações",
        description = "Verifica se o serviço de notificações está operacional",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Serviço operacional"
            )
        }
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification service is healthy");
    }
}
