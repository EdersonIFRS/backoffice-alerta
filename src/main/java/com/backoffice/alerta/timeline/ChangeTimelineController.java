package com.backoffice.alerta.timeline;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * US#40 - Controller para Linha do Tempo de Decisão
 * 
 * Endpoints READ-ONLY para visualizar o histórico completo
 * de eventos relacionados a um Pull Request.
 * 
 * GOVERNANÇA:
 * - RBAC: ADMIN e RISK_MANAGER apenas
 * - Read-only: Não modifica dados
 * - Determinístico: Não executa cálculos novos
 * - Auditável: Consulta dados históricos
 */
@RestController
@RequestMapping("/risk/timeline")
@Tag(name = "Timeline de Decisão", description = "Visualização cronológica do histórico de decisões (US#40)")
public class ChangeTimelineController {

    private final ChangeTimelineService timelineService;

    public ChangeTimelineController(ChangeTimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping("/{pullRequestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Obter linha do tempo completa de um Pull Request",
        description = """
            Retorna a linha do tempo cronológica completa de eventos relacionados a um Pull Request,
            incluindo: decisões, auditorias, notificações, SLAs e feedbacks.
            
            **GOVERNANÇA:**
            - Endpoint READ-ONLY: Não modifica nenhum dado
            - Determinístico: Não executa novos cálculos ou chamadas externas
            - Auditável: Consulta apenas dados históricos já persistidos
            
            **RBAC:**
            - Permitido: ADMIN, RISK_MANAGER
            - Negado: ENGINEER, VIEWER
            
            **Casos de Uso:**
            - Rastreamento completo da jornada de uma mudança
            - Análise de decisões tomadas e seus resultados
            - Verificação de cumprimento de SLAs
            - Auditoria de processo de aprovação
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Timeline gerada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChangeTimelineResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "PR de Baixo Risco",
                            description = "Pull Request simples aprovado sem intercorrências",
                            value = """
                                {
                                  "pullRequestId": "PR-2024-001",
                                  "environment": "DEVELOPMENT",
                                  "finalDecision": "APPROVED",
                                  "overallRiskLevel": "LOW",
                                  "requiresExecutiveAttention": false,
                                  "events": [
                                    {
                                      "id": "123e4567-e89b-12d3-a456-426614174000",
                                      "eventType": "DECISION",
                                      "title": "Decisão: Aprovada",
                                      "description": "Decisão Aprovada para mudança em DEVELOPMENT. Risco: LOW. Justificativa: Mudança simples em ambiente de dev",
                                      "createdAt": "2024-12-18T10:00:00Z",
                                      "actor": "USER",
                                      "severity": "INFO",
                                      "relatedEntityId": "123e4567-e89b-12d3-a456-426614174000",
                                      "metadata": {
                                        "riskLevel": "LOW",
                                        "environment": "DEVELOPMENT",
                                        "decision": "APPROVED"
                                      }
                                    },
                                    {
                                      "id": "123e4567-e89b-12d3-a456-426614174000_audit",
                                      "eventType": "AUDIT",
                                      "title": "Auditoria Registrada",
                                      "description": "Registro de auditoria criado. Decisor: john.doe, Timestamp: 2024-12-18T10:00:00Z",
                                      "createdAt": "2024-12-18T10:00:00Z",
                                      "actor": "SYSTEM",
                                      "severity": "INFO",
                                      "relatedEntityId": "123e4567-e89b-12d3-a456-426614174000",
                                      "metadata": {
                                        "decisionMaker": "john.doe",
                                        "pullRequestId": "PR-2024-001"
                                      }
                                    }
                                  ]
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "PR Crítico com SLA",
                            description = "Pull Request crítico em produção com SLA vencido",
                            value = """
                                {
                                  "pullRequestId": "PR-2024-PAYMENT-HOTFIX",
                                  "environment": "PRODUCTION",
                                  "finalDecision": "APPROVED_WITH_CONDITIONS",
                                  "overallRiskLevel": "CRITICAL",
                                  "requiresExecutiveAttention": true,
                                  "events": [
                                    {
                                      "id": "abc-123",
                                      "eventType": "DECISION",
                                      "title": "Decisão: Aprovada com Condições",
                                      "description": "Decisão Aprovada com Condições para mudança em PRODUCTION. Risco: CRITICAL. Justificativa: Hotfix urgente com monitoramento reforçado",
                                      "createdAt": "2024-12-18T14:00:00Z",
                                      "actor": "USER",
                                      "severity": "CRITICAL",
                                      "relatedEntityId": "abc-123",
                                      "metadata": {
                                        "riskLevel": "CRITICAL",
                                        "environment": "PRODUCTION",
                                        "decision": "APPROVED_WITH_CONDITIONS"
                                      }
                                    },
                                    {
                                      "id": "sla-456",
                                      "eventType": "SLA_CREATED",
                                      "title": "SLA Criado",
                                      "description": "SLA definido com deadline: 2024-12-18T16:00:00Z. Responsável: payments-team",
                                      "createdAt": "2024-12-18T14:00:00Z",
                                      "actor": "SYSTEM",
                                      "severity": "INFO",
                                      "relatedEntityId": "abc-123",
                                      "metadata": {
                                        "deadline": "2024-12-18T16:00:00Z",
                                        "responsibleTeam": "payments-team",
                                        "status": "BREACHED"
                                      }
                                    },
                                    {
                                      "id": "sla-456_escalated",
                                      "eventType": "SLA_ESCALATED",
                                      "title": "⚠️ SLA Vencido - Escalonamento",
                                      "description": "SLA ultrapassou o deadline. Ação requerida do time: payments-team",
                                      "createdAt": "2024-12-18T16:00:00Z",
                                      "actor": "SYSTEM",
                                      "severity": "CRITICAL",
                                      "relatedEntityId": "sla-456",
                                      "metadata": {
                                        "deadline": "2024-12-18T16:00:00Z",
                                        "responsibleTeam": "payments-team",
                                        "status": "BREACHED"
                                      }
                                    },
                                    {
                                      "id": "notif-789",
                                      "eventType": "NOTIFICATION",
                                      "title": "Notificação: SLA em risco",
                                      "description": "SLA próximo do vencimento. Ação urgente necessária.",
                                      "createdAt": "2024-12-18T15:45:00Z",
                                      "actor": "SYSTEM",
                                      "severity": "CRITICAL",
                                      "relatedEntityId": "abc-123",
                                      "metadata": {
                                        "severity": "CRITICAL",
                                        "teamName": "payments-team",
                                        "channel": "SLACK"
                                      }
                                    }
                                  ]
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "PR com Feedback Negativo",
                            description = "Pull Request que gerou incidentes pós-deploy",
                            value = """
                                {
                                  "pullRequestId": "PR-2024-BILLING-FEATURE",
                                  "environment": "PRODUCTION",
                                  "finalDecision": "APPROVED",
                                  "overallRiskLevel": "MEDIUM",
                                  "requiresExecutiveAttention": false,
                                  "events": [
                                    {
                                      "id": "xyz-001",
                                      "eventType": "DECISION",
                                      "title": "Decisão: Aprovada",
                                      "description": "Decisão Aprovada para mudança em PRODUCTION. Risco: MEDIUM. Justificativa: Feature bem testada",
                                      "createdAt": "2024-12-15T10:00:00Z",
                                      "actor": "USER",
                                      "severity": "WARNING",
                                      "relatedEntityId": "xyz-001",
                                      "metadata": {
                                        "riskLevel": "MEDIUM",
                                        "environment": "PRODUCTION",
                                        "decision": "APPROVED"
                                      }
                                    },
                                    {
                                      "id": "feedback-123",
                                      "eventType": "FEEDBACK",
                                      "title": "Feedback Pós-Deploy: ✗ Incorreto",
                                      "description": "Feedback recebido de ops-team. Incidentes: Sim. Comentário: Deploy causou lentidão no faturamento",
                                      "createdAt": "2024-12-16T08:00:00Z",
                                      "actor": "USER",
                                      "severity": "WARNING",
                                      "relatedEntityId": "xyz-001",
                                      "metadata": {
                                        "wasCorrect": "false",
                                        "hadIncidents": "true",
                                        "provider": "ops-team"
                                      }
                                    }
                                  ]
                                }
                                """
                        )
                    }
                )
            ),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer perfil ADMIN ou RISK_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Pull Request não encontrado")
        }
    )
    public ResponseEntity<ChangeTimelineResponse> getTimeline(@PathVariable String pullRequestId) {
        ChangeTimelineResponse timeline = timelineService.generateTimeline(pullRequestId);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/{pullRequestId}/health")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Verificar saúde da timeline",
        description = """
            Retorna informações sobre completude e integridade da timeline.
            Útil para diagnóstico e validação.
            """
    )
    public ResponseEntity<Map<String, Object>> getTimelineHealth(@PathVariable String pullRequestId) {
        ChangeTimelineResponse timeline = timelineService.generateTimeline(pullRequestId);
        
        long decisionCount = timeline.events().stream()
            .filter(e -> e.eventType() == TimelineEventType.DECISION)
            .count();
        
        long feedbackCount = timeline.events().stream()
            .filter(e -> e.eventType() == TimelineEventType.FEEDBACK)
            .count();
        
        long slaCount = timeline.events().stream()
            .filter(e -> e.eventType() == TimelineEventType.SLA_CREATED || 
                        e.eventType() == TimelineEventType.SLA_ESCALATED)
            .count();
        
        boolean hasCompleteCycle = decisionCount > 0 && feedbackCount > 0;
        
        return ResponseEntity.ok(Map.of(
            "pullRequestId", pullRequestId,
            "totalEvents", timeline.events().size(),
            "decisionCount", decisionCount,
            "feedbackCount", feedbackCount,
            "slaCount", slaCount,
            "hasCompleteCycle", hasCompleteCycle,
            "requiresExecutiveAttention", timeline.requiresExecutiveAttention()
        ));
    }
}
