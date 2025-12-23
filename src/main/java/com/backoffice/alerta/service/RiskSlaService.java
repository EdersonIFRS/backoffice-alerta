package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.RiskSlaResponse;
import com.backoffice.alerta.dto.SlaSummaryResponse;
import com.backoffice.alerta.notification.RiskNotification;
import com.backoffice.alerta.rules.RiskLevel;
import com.backoffice.alerta.sla.*;
import com.backoffice.alerta.repository.RiskSlaTrackingRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço de gerenciamento de SLA de risco
 * 
 * Responsável por:
 * - Criar SLA automaticamente quando notificação CRÍTICA é gerada (US#27)
 * - Calcular deadline baseado no RiskLevel
 * - Controlar status e escalonamento
 * - Fornecer consultas read-only
 * 
 * ⚠️ NÃO envia notificações reais (apenas registra estados)
 * ⚠️ NÃO altera decisões ou auditorias
 */
@Service
public class RiskSlaService {

    private final RiskSlaTrackingRepository slaRepository;

    public RiskSlaService(RiskSlaTrackingRepository slaRepository) {
        this.slaRepository = slaRepository;
    }

    /**
     * Cria SLA automaticamente para notificação crítica
     * 
     * Deadlines por RiskLevel:
     * - CRÍTICO → 30 minutos
     * - ALTO → 2 horas
     * - MÉDIO → 24 horas
     * - BAIXO → NÃO cria SLA
     * 
     * @param notification Notificação crítica gerada
     * @return SLA criado ou null se não aplicável
     */
    public RiskSlaTracking createSlaForNotification(RiskNotification notification) {
        // Só cria SLA para notificações de risco relevante
        RiskLevel riskLevel = determineRiskLevelFromNotification(notification);
        
        if (riskLevel == RiskLevel.BAIXO) {
            return null; // Não cria SLA para risco baixo
        }

        // Calcula deadline baseado no risco
        Instant deadline = calculateDeadline(riskLevel);

        // Cria SLA tracking inicial
        RiskSlaTracking sla = new RiskSlaTracking(
            notification.getId(),
            notification.getAuditId(),
            notification.getPullRequestId(),
            riskLevel,
            EscalationLevel.PRIMARY, // Sempre inicia no PRIMARY
            deadline,
            SlaStatus.PENDING, // Sempre inicia como PENDING
            null // Sem escalonamento inicial
        );

        return slaRepository.save(sla);
    }

    /**
     * Calcula deadline baseado no nível de risco
     */
    private Instant calculateDeadline(RiskLevel riskLevel) {
        Instant now = Instant.now();
        
        return switch (riskLevel) {
            case CRITICO -> now.plus(Duration.ofMinutes(30));
            case ALTO -> now.plus(Duration.ofHours(2));
            case MEDIO -> now.plus(Duration.ofHours(24));
            case BAIXO -> now.plus(Duration.ofDays(7)); // Fallback (não deve ocorrer)
        };
    }

    /**
     * Determina RiskLevel a partir da notificação
     * Como a notificação não tem RiskLevel diretamente, inferimos da severidade
     */
    private RiskLevel determineRiskLevelFromNotification(RiskNotification notification) {
        // Pode buscar do auditId ou usar lógica da severidade
        // Por simplicidade, vamos inferir da trigger
        return switch (notification.getNotificationTrigger()) {
            case RISK_BLOCKED, HIGH_RISK_PRODUCTION, INCIDENT_HISTORY_ALERT -> RiskLevel.CRITICO;
            case RISK_RESTRICTED -> RiskLevel.ALTO;
        };
    }

    /**
     * Marca SLA como reconhecido
     */
    public RiskSlaTracking acknowledgeSla(UUID slaId) {
        RiskSlaTracking existing = slaRepository.findById(slaId)
                .orElseThrow(() -> new IllegalArgumentException("SLA não encontrado: " + slaId));

        RiskSlaTracking updated = existing.withStatus(SlaStatus.ACKNOWLEDGED);
        return slaRepository.save(updated);
    }

    /**
     * Marca SLA como resolvido
     */
    public RiskSlaTracking resolveSla(UUID slaId) {
        RiskSlaTracking existing = slaRepository.findById(slaId)
                .orElseThrow(() -> new IllegalArgumentException("SLA não encontrado: " + slaId));

        RiskSlaTracking updated = existing.withStatus(SlaStatus.RESOLVED);
        return slaRepository.save(updated);
    }

    /**
     * Escalona SLA para próximo nível
     */
    public RiskSlaTracking escalateSla(UUID slaId) {
        RiskSlaTracking existing = slaRepository.findById(slaId)
                .orElseThrow(() -> new IllegalArgumentException("SLA não encontrado: " + slaId));

        EscalationLevel nextLevel = existing.getCurrentLevel().next();
        RiskSlaTracking updated = existing.withEscalation(nextLevel, SlaStatus.ESCALATED);
        return slaRepository.save(updated);
    }

    /**
     * Lista todos os SLAs
     */
    public List<RiskSlaResponse> listAll() {
        return slaRepository.findAll().stream()
                .map(RiskSlaResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Lista SLAs vencidos
     */
    public List<RiskSlaResponse> listBreached() {
        return slaRepository.findBreached().stream()
                .map(RiskSlaResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Lista SLAs por auditoria
     */
    public List<RiskSlaResponse> listByAuditId(UUID auditId) {
        return slaRepository.findByAuditIdOrderByCreatedAtDesc(auditId).stream()
                .map(RiskSlaResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Gera resumo de SLAs
     */
    public SlaSummaryResponse getSummary() {
        List<RiskSlaTracking> all = slaRepository.findAll();
        
        int total = all.size();
        
        int breached = (int) all.stream()
            .filter(sla -> sla.getStatus() == SlaStatus.BREACHED)
            .count();
        
        int escalated = (int) all.stream()
            .filter(sla -> sla.getStatus() == SlaStatus.ESCALATED)
            .count();

        // Calcula tempo médio de resposta para SLAs resolvidos/reconhecidos
        double avgResponseMinutes = all.stream()
            .filter(sla -> sla.getStatus() == SlaStatus.RESOLVED || 
                          sla.getStatus() == SlaStatus.ACKNOWLEDGED)
            .filter(sla -> sla.getLastEscalationAt() != null || 
                          sla.getStatus() == SlaStatus.ACKNOWLEDGED)
            .mapToLong(sla -> {
                Instant responseTime = sla.getLastEscalationAt() != null 
                    ? sla.getLastEscalationAt() 
                    : sla.getCreatedAt();
                return Duration.between(sla.getCreatedAt(), responseTime).toMinutes();
            })
            .average()
            .orElse(0.0);

        return new SlaSummaryResponse(total, breached, escalated, avgResponseMinutes);
    }
}

