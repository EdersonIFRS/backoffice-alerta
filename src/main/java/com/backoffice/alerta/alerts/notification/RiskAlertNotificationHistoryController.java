package com.backoffice.alerta.alerts.notification;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.alerts.notification.dto.RiskAlertNotificationHistoryResponse;
import com.backoffice.alerta.alerts.notification.dto.RiskAlertNotificationHistorySummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller para histórico de notificações de alertas
 * 
 * US#59 - Histórico e Rastreabilidade de Notificações de Alerta
 */
@RestController
@RequestMapping("/risk/alerts/history")
@Tag(name = "Alert Notification History", description = "Histórico e auditoria de notificações de alertas")
public class RiskAlertNotificationHistoryController {

    private final RiskAlertNotificationHistoryRepository historyRepository;

    public RiskAlertNotificationHistoryController(RiskAlertNotificationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    /**
     * 1️⃣ Histórico geral com filtros
     * GET /risk/alerts/history
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(summary = "Buscar histórico de notificações com filtros",
               description = "Retorna histórico completo de notificações (SENT, SKIPPED, FAILED) com filtros opcionais")
    public ResponseEntity<List<RiskAlertNotificationHistoryResponse>> getHistory(
            @Parameter(description = "ID do projeto") 
            @RequestParam(required = false) UUID projectId,
            
            @Parameter(description = "ID da regra de negócio") 
            @RequestParam(required = false) String businessRuleId,
            
            @Parameter(description = "Severidade do alerta") 
            @RequestParam(required = false) AlertSeverity severity,
            
            @Parameter(description = "Canal de notificação") 
            @RequestParam(required = false) NotificationChannel channel,
            
            @Parameter(description = "Status da notificação") 
            @RequestParam(required = false) NotificationStatus status,
            
            @Parameter(description = "Data inicial (ISO-8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            
            @Parameter(description = "Data final (ISO-8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate) {
        
        List<RiskAlertNotificationHistory> history;
        
        // Aplicar filtros
        if (fromDate != null && toDate != null) {
            history = historyRepository.findTimeline(fromDate, toDate);
        } else {
            history = historyRepository.findAll();
        }
        
        // Filtrar por campos opcionais
        List<RiskAlertNotificationHistory> filtered = history.stream()
            .filter(h -> projectId == null || Objects.equals(h.getProjectId(), projectId))
            .filter(h -> businessRuleId == null || Objects.equals(h.getBusinessRuleId(), businessRuleId))
            .filter(h -> severity == null || h.getSeverity() == severity)
            .filter(h -> channel == null || h.getChannel() == channel)
            .filter(h -> status == null || h.getStatus() == status)
            .sorted(Comparator.comparing(RiskAlertNotificationHistory::getCreatedAt).reversed())
            .collect(Collectors.toList());
        
        List<RiskAlertNotificationHistoryResponse> response = filtered.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 2️⃣ Histórico por projeto
     * GET /risk/alerts/history/projects/{projectId}
     */
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(summary = "Buscar histórico por projeto",
               description = "Retorna todas as notificações enviadas para um projeto específico")
    public ResponseEntity<List<RiskAlertNotificationHistoryResponse>> getHistoryByProject(
            @Parameter(description = "ID do projeto", required = true) 
            @PathVariable UUID projectId) {
        
        List<RiskAlertNotificationHistory> history = historyRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        
        List<RiskAlertNotificationHistoryResponse> response = history.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 3️⃣ Histórico por regra de negócio
     * GET /risk/alerts/history/rules/{ruleId}
     */
    @GetMapping("/rules/{ruleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(summary = "Buscar histórico por regra de negócio",
               description = "Retorna todas as notificações relacionadas a uma regra específica")
    public ResponseEntity<List<RiskAlertNotificationHistoryResponse>> getHistoryByRule(
            @Parameter(description = "ID da regra de negócio", required = true) 
            @PathVariable String ruleId) {
        
        List<RiskAlertNotificationHistory> history = historyRepository.findByBusinessRuleIdOrderByCreatedAtDesc(ruleId);
        
        List<RiskAlertNotificationHistoryResponse> response = history.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 4️⃣ Timeline de alertas
     * GET /risk/alerts/history/timeline
     */
    @GetMapping("/timeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(summary = "Timeline de notificações",
               description = "Retorna linha do tempo de notificações ordenadas por data (mais recentes primeiro)")
    public ResponseEntity<List<RiskAlertNotificationHistoryResponse>> getTimeline(
            @Parameter(description = "Data inicial (ISO-8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            
            @Parameter(description = "Data final (ISO-8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate) {
        
        Instant from = fromDate != null ? fromDate : Instant.now().minusSeconds(30 * 24 * 60 * 60); // 30 dias
        Instant to = toDate != null ? toDate : Instant.now();
        
        List<RiskAlertNotificationHistory> history = historyRepository.findTimeline(from, to);
        
        List<RiskAlertNotificationHistoryResponse> response = history.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 5️⃣ Resumo executivo (summary)
     * GET /risk/alerts/history/summary
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(summary = "Resumo executivo do histórico",
               description = "Retorna métricas agregadas: totais por status, severidade, top projetos e regras")
    public ResponseEntity<RiskAlertNotificationHistorySummaryResponse> getSummary() {
        
        long totalSent = historyRepository.countByStatus(NotificationStatus.SENT);
        long totalSkipped = historyRepository.countByStatus(NotificationStatus.SKIPPED);
        long totalFailed = historyRepository.countByStatus(NotificationStatus.FAILED);
        long totalCritical = historyRepository.countBySeverity(AlertSeverity.CRITICAL);
        
        // Top 5 projetos
        List<Object[]> topProjectsRaw = historyRepository.findTopProjectsByAlertCount(5);
        List<Map<String, Object>> topProjects = topProjectsRaw.stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("projectName", (String) row[0]);
                map.put("count", ((Number) row[1]).longValue());
                return map;
            })
            .collect(Collectors.toList());
        
        // Top 5 regras
        List<Object[]> topRulesRaw = historyRepository.findTopRulesByAlertCount(5);
        List<Map<String, Object>> topRules = topRulesRaw.stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("ruleName", (String) row[0]);
                map.put("count", ((Number) row[1]).longValue());
                return map;
            })
            .collect(Collectors.toList());
        
        RiskAlertNotificationHistorySummaryResponse summary = new RiskAlertNotificationHistorySummaryResponse(
            totalSent,
            totalSkipped,
            totalFailed,
            totalCritical,
            topProjects,
            topRules
        );
        
        return ResponseEntity.ok(summary);
    }

    /**
     * 6️⃣ Health check do histórico
     * GET /risk/alerts/history/health
     */
    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(summary = "Verificar saúde do sistema de histórico",
               description = "Retorna status de saúde e estatísticas básicas do histórico")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            long totalRecords = historyRepository.count();
            long recentRecords = historyRepository.findTimeline(
                Instant.now().minusSeconds(24 * 60 * 60), 
                Instant.now()
            ).size();
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("totalRecords", totalRecords);
            health.put("last24Hours", recentRecords);
            health.put("timestamp", Instant.now());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", Instant.now());
            
            return ResponseEntity.status(500).body(health);
        }
    }

    /**
     * Converte entity para DTO
     */
    private RiskAlertNotificationHistoryResponse toResponse(RiskAlertNotificationHistory history) {
        RiskAlertNotificationHistoryResponse response = new RiskAlertNotificationHistoryResponse();
        response.setId(history.getId());
        response.setAlertType(history.getAlertType());
        response.setSeverity(history.getSeverity());
        response.setChannel(history.getChannel());
        response.setStatus(history.getStatus());
        response.setProjectId(history.getProjectId());
        response.setProjectName(history.getProjectName());
        response.setBusinessRuleId(history.getBusinessRuleId());
        response.setBusinessRuleName(history.getBusinessRuleName());
        response.setMessageSummary(history.getMessageSummary());
        response.setDeliveryReason(history.getDeliveryReason());
        response.setRecipient(history.getRecipient());
        response.setCreatedAt(history.getCreatedAt());
        response.setCreatedBy(history.getCreatedBy());
        return response;
    }
}
