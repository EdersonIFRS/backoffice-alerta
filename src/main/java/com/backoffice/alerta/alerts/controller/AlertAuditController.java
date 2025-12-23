package com.backoffice.alerta.alerts.controller;

import com.backoffice.alerta.alerts.dto.AlertAuditDetailResponse;
import com.backoffice.alerta.alerts.dto.AlertAuditSummaryResponse;
import com.backoffice.alerta.alerts.dto.AlertAuditTimelineResponse;
import com.backoffice.alerta.alerts.service.AlertAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller REST para auditoria detalhada de alertas e notificações
 * 
 * US#61 - Auditoria Detalhada de Alertas e Notificações
 * 
 * RBAC: Apenas ADMIN e RISK_MANAGER
 * READ-ONLY: Não altera dados
 * CSRF: Desabilitado para /risk/alerts/audit/**
 */
@RestController
@RequestMapping("/risk/alerts/audit")
@Tag(name = "Alert Audit", description = "Auditoria profunda de alertas e notificações com explicação determinística")
public class AlertAuditController {
    
    private static final Logger log = LoggerFactory.getLogger(AlertAuditController.class);
    
    private final AlertAuditService auditService;
    
    public AlertAuditController(AlertAuditService auditService) {
        this.auditService = auditService;
    }
    
    /**
     * GET /risk/alerts/audit/{id}
     * 
     * Busca auditoria detalhada de um alerta específico
     * Responde: "Por que este alerta foi enviado/bloqueado?"
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Auditoria detalhada de alerta",
        description = "Retorna explicação completa do motivo de um alerta ter sido enviado, bloqueado ou falhado, " +
                     "incluindo preferência resolvida, hierarquia aplicada e compliance flags",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Auditoria encontrada",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AlertAuditDetailResponse.class),
                    examples = @ExampleObject(
                        name = "Alerta Bloqueado por Severidade",
                        value = """
                        {
                          "alertHistoryId": 123456789,
                          "alertType": "GATE_BLOCKED",
                          "severity": "INFO",
                          "channel": "SLACK",
                          "status": "SKIPPED",
                          "messageSummary": "Gate bloqueado por regra de cobertura",
                          "deliveryReason": "Severidade inferior ao mínimo configurado",
                          "createdAt": "2025-01-15T10:30:00",
                          "createdBy": "system",
                          "project": {
                            "id": 1,
                            "name": "Backoffice Pagamentos"
                          },
                          "businessRule": {
                            "id": "BR001",
                            "name": "Cobertura Mínima 80%"
                          },
                          "resolvedPreference": {
                            "source": "PROJECT",
                            "minimumSeverity": "WARNING",
                            "allowedAlertTypes": ["GATE_BLOCKED", "RISK_DETECTED"],
                            "allowedChannels": ["SLACK"],
                            "deliveryWindow": "BUSINESS_HOURS"
                          },
                          "explanation": "🚫 Este alerta foi BLOQUEADO porque a severidade INFO é inferior à severidade mínima configurada (WARNING) na preferência de PROJECT 'Backoffice Pagamentos', que sobrescreveu o DEFAULT.",
                          "complianceFlags": {
                            "respectedSeverity": false,
                            "respectedChannel": true,
                            "respectedWindow": true,
                            "respectedHierarchy": true
                          }
                        }
                        """
                    )
                )
            ),
            @ApiResponse(responseCode = "404", description = "Histórico de alerta não encontrado"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
        }
    )
    public ResponseEntity<AlertAuditDetailResponse> getAuditDetail(
            @Parameter(description = "ID do histórico de alerta (UUID)", required = true)
            @PathVariable UUID id) {
        
        log.info("🔍 [API] GET /risk/alerts/audit/{} - Auditoria detalhada", id);
        
        try {
            AlertAuditDetailResponse audit = auditService.getAuditDetail(id);
            
            if (audit.getAlertHistoryId() == null) {
                log.warn("⚠️ Histórico não encontrado - id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(audit);
            }
            
            log.info("✅ Auditoria retornada - id: {}, status: {}", id, audit.getStatus());
            return ResponseEntity.ok(audit);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ ID inválido - id: {}", id, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Erro ao buscar auditoria - id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /risk/alerts/audit/summary
     * 
     * Busca resumo agregado de auditoria
     * Responde: "Qual o panorama geral dos alertas bloqueados?"
     */
    @GetMapping("/summary")
    @Operation(
        summary = "Resumo agregado de auditoria",
        description = "Retorna panorama geral de alertas enviados/bloqueados/falhados, " +
                     "incluindo top projetos e regras com mais bloqueios",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Resumo de auditoria",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AlertAuditSummaryResponse.class),
                    examples = @ExampleObject(
                        name = "Resumo Geral",
                        value = """
                        {
                          "totalAlerts": 1250,
                          "sent": 800,
                          "skipped": 380,
                          "failed": 70,
                          "mostBlockedSeverity": "INFO",
                          "mostBlockedChannel": "TEAMS",
                          "topProjectsByBlocked": [
                            {
                              "projectId": 1,
                              "projectName": "Backoffice Pagamentos",
                              "blockedCount": 120
                            },
                            {
                              "projectId": 2,
                              "projectName": "Portal Cliente",
                              "blockedCount": 85
                            }
                          ],
                          "topRulesByBlocked": [
                            {
                              "ruleId": "BR001",
                              "ruleName": "Cobertura Mínima 80%",
                              "blockedCount": 95
                            },
                            {
                              "ruleId": "BR003",
                              "ruleName": "Duplicação Máxima 3%",
                              "blockedCount": 67
                            }
                          ]
                        }
                        """
                    )
                )
            ),
            @ApiResponse(responseCode = "200", description = "Resumo vazio se não houver dados")
        }
    )
    public ResponseEntity<AlertAuditSummaryResponse> getAuditSummary(
            @Parameter(description = "Filtrar por projeto (UUID)")
            @RequestParam(required = false) UUID projectId,
            
            @Parameter(description = "Filtrar por regra de negócio")
            @RequestParam(required = false) String businessRuleId,
            
            @Parameter(description = "Data inicial (formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @Parameter(description = "Data final (formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        log.info("📊 [API] GET /risk/alerts/audit/summary - projectId: {}, ruleId: {}, from: {}, to: {}", 
                 projectId, businessRuleId, fromDate, toDate);
        
        try {
            AlertAuditSummaryResponse summary = auditService.getAuditSummary(projectId, businessRuleId, fromDate, toDate);
            
            log.info("✅ Resumo retornado - total: {}, sent: {}, skipped: {}, failed: {}", 
                     summary.getTotalAlerts(), summary.getSent(), summary.getSkipped(), summary.getFailed());
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("❌ Erro ao buscar resumo de auditoria", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /risk/alerts/audit/timeline
     * 
     * Busca timeline de alertas agrupados por data
     */
    @GetMapping("/timeline")
    @Operation(
        summary = "Timeline de auditoria",
        description = "Retorna série temporal de alertas enviados/bloqueados/falhados agrupados por data, " +
                     "útil para identificar padrões e tendências ao longo do tempo",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Timeline de alertas",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AlertAuditTimelineResponse.class),
                    examples = @ExampleObject(
                        name = "Timeline 7 dias",
                        value = """
                        [
                          {
                            "date": "2025-01-10",
                            "totalSent": 45,
                            "totalSkipped": 12,
                            "totalFailed": 3
                          },
                          {
                            "date": "2025-01-11",
                            "totalSent": 52,
                            "totalSkipped": 18,
                            "totalFailed": 1
                          },
                          {
                            "date": "2025-01-12",
                            "totalSent": 38,
                            "totalSkipped": 9,
                            "totalFailed": 2
                          }
                        ]
                        """
                    )
                )
            ),
            @ApiResponse(responseCode = "200", description = "Lista vazia se não houver dados no período")
        }
    )
    public ResponseEntity<List<AlertAuditTimelineResponse>> getAuditTimeline(
            @Parameter(description = "Filtrar por projeto (UUID)")
            @RequestParam(required = false) UUID projectId,
            
            @Parameter(description = "Filtrar por regra de negócio")
            @RequestParam(required = false) String businessRuleId,
            
            @Parameter(description = "Data inicial (formato: yyyy-MM-dd, padrão: -30 dias)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @Parameter(description = "Data final (formato: yyyy-MM-dd, padrão: hoje)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        log.info("📈 [API] GET /risk/alerts/audit/timeline - projectId: {}, ruleId: {}, from: {}, to: {}", 
                 projectId, businessRuleId, fromDate, toDate);
        
        try {
            List<AlertAuditTimelineResponse> timeline = auditService.getAuditTimeline(projectId, businessRuleId, fromDate, toDate);
            
            log.info("✅ Timeline retornada - {} pontos de dados", timeline.size());
            
            return ResponseEntity.ok(timeline);
            
        } catch (Exception e) {
            log.error("❌ Erro ao buscar timeline de auditoria", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /risk/alerts/audit/health
     * 
     * Health check para validar disponibilidade do serviço
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check de auditoria",
        description = "Verifica se o serviço de auditoria está operacional",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Serviço operacional",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                          "status": "UP",
                          "service": "AlertAuditService",
                          "version": "US#61",
                          "timestamp": "2025-01-15T10:30:00Z"
                        }
                        """
                    )
                )
            )
        }
    )
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("🏥 [API] GET /risk/alerts/audit/health");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "AlertAuditService");
        health.put("version", "US#61");
        health.put("timestamp", java.time.Instant.now().toString());
        
        return ResponseEntity.ok(health);
    }
}
