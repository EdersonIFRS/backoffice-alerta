package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.RiskSlaResponse;
import com.backoffice.alerta.dto.SlaSummaryResponse;
import com.backoffice.alerta.service.RiskSlaService;
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
 * Controller READ-ONLY para consultar SLAs de risco
 * 
 * US#28 - SLA & Escalonamento Automático por Ownership
 * 
 * Permite consultar SLAs de resposta organizacional gerados automaticamente
 * para decisões de risco críticas, com controle de vencimento e escalonamento.
 * 
 * ⚠️ READ-ONLY - não permite criar/editar SLAs manualmente
 * ⚠️ SLAs são gerados automaticamente pelo sistema
 */
@RestController
@RequestMapping("/risk/sla")
@Tag(name = "Risk SLA", description = "Consulta de SLAs de resposta organizacional (READ-ONLY)")
public class RiskSlaController {

    private final RiskSlaService slaService;

    public RiskSlaController(RiskSlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping
    @Operation(
        summary = "Listar todos os SLAs",
        description = """
            Retorna todos os SLAs de resposta organizacional rastreados pelo sistema,
            ordenados por data de criação (mais recentes primeiro).
            
            **SLAs são criados automaticamente quando:**
            - Notificação CRÍTICA é gerada (US#27)
            - Risco = CRÍTICO, ALTO ou MÉDIO
            
            **Deadlines por RiskLevel:**
            - CRÍTICO → 30 minutos
            - ALTO → 2 horas
            - MÉDIO → 24 horas
            - BAIXO → não cria SLA
            
            **Escalonamento automático:**
            - PRIMARY → SECONDARY → BACKUP → ORGANIZATIONAL
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de SLAs retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RiskSlaResponse.class),
                    examples = {
                        @ExampleObject(
                            name = "SLA Crítico Ativo",
                            description = "SLA de 30 minutos para risco crítico em produção, aguardando resposta",
                            value = """
                                [{
                                  "id": "123e4567-e89b-12d3-a456-426614174000",
                                  "notificationId": "550e8400-e29b-41d4-a716-446655440000",
                                  "auditId": "660e8400-e29b-41d4-a716-446655440001",
                                  "pullRequestId": "PR-789",
                                  "riskLevel": "CRITICO",
                                  "currentLevel": "PRIMARY",
                                  "slaDeadline": "2024-03-15T15:00:00Z",
                                  "status": "PENDING",
                                  "createdAt": "2024-03-15T14:30:00Z",
                                  "lastEscalationAt": null,
                                  "overdue": false
                                }]
                                """
                        ),
                        @ExampleObject(
                            name = "SLA Vencido com Escalonamento",
                            description = "SLA vencido, escalonado automaticamente para nível SECONDARY",
                            value = """
                                [{
                                  "id": "223e4567-e89b-12d3-a456-426614174001",
                                  "notificationId": "770e8400-e29b-41d4-a716-446655440002",
                                  "auditId": "880e8400-e29b-41d4-a716-446655440003",
                                  "pullRequestId": "PR-456",
                                  "riskLevel": "ALTO",
                                  "currentLevel": "SECONDARY",
                                  "slaDeadline": "2024-03-15T16:30:00Z",
                                  "status": "ESCALATED",
                                  "createdAt": "2024-03-15T14:30:00Z",
                                  "lastEscalationAt": "2024-03-15T16:45:00Z",
                                  "overdue": true
                                }]
                                """
                        ),
                        @ExampleObject(
                            name = "SLA Resolvido",
                            description = "SLA resolvido com sucesso antes do vencimento",
                            value = """
                                [{
                                  "id": "323e4567-e89b-12d3-a456-426614174002",
                                  "notificationId": "990e8400-e29b-41d4-a716-446655440004",
                                  "auditId": "aa0e8400-e29b-41d4-a716-446655440005",
                                  "pullRequestId": "PR-123",
                                  "riskLevel": "MEDIO",
                                  "currentLevel": "PRIMARY",
                                  "slaDeadline": "2024-03-16T14:30:00Z",
                                  "status": "RESOLVED",
                                  "createdAt": "2024-03-15T14:30:00Z",
                                  "lastEscalationAt": null,
                                  "overdue": false
                                }]
                                """
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<List<RiskSlaResponse>> listAllSlas() {
        List<RiskSlaResponse> slas = slaService.listAll();
        return ResponseEntity.ok(slas);
    }

    @GetMapping("/breached")
    @Operation(
        summary = "Listar SLAs vencidos",
        description = "Retorna apenas os SLAs que venceram (status = BREACHED)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de SLAs vencidos retornada com sucesso"
            )
        }
    )
    public ResponseEntity<List<RiskSlaResponse>> listBreachedSlas() {
        List<RiskSlaResponse> slas = slaService.listBreached();
        return ResponseEntity.ok(slas);
    }

    @GetMapping("/audit/{auditId}")
    @Operation(
        summary = "Listar SLAs por auditoria",
        description = "Retorna todos os SLAs associados a uma auditoria específica (US#20)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "SLAs da auditoria retornados com sucesso"
            )
        }
    )
    public ResponseEntity<List<RiskSlaResponse>> listByAuditId(
            @Parameter(description = "ID da auditoria (US#20)", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID auditId) {
        
        List<RiskSlaResponse> slas = slaService.listByAuditId(auditId);
        return ResponseEntity.ok(slas);
    }

    @GetMapping("/summary")
    @Operation(
        summary = "Obter resumo de SLAs",
        description = "Retorna estatísticas agregadas de todos os SLAs rastreados",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Resumo retornado com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SlaSummaryResponse.class)
                )
            )
        }
    )
    public ResponseEntity<SlaSummaryResponse> getSummary() {
        SlaSummaryResponse summary = slaService.getSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Health check do serviço de SLA",
        description = "Verifica se o serviço de SLA está operacional",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Serviço operacional"
            )
        }
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("SLA service is healthy");
    }
}
