package com.backoffice.alerta.service;

import com.backoffice.alerta.repository.RiskSlaTrackingRepository;
import com.backoffice.alerta.sla.RiskSlaTracking;
import com.backoffice.alerta.sla.SlaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Serviço de avaliação periódica de SLAs vencidos
 * 
 * Responsável por:
 * - Identificar SLAs que venceram (deadline passou)
 * - Atualizar status para BREACHED
 * - Aplicar escalonamento automático
 * 
 * Preparado para uso com @Scheduled, mas não configurado ainda.
 * 
 * ⚠️ NÃO envia notificações reais (apenas atualiza estados)
 * ⚠️ NÃO altera decisões ou auditorias
 */
@Service
public class RiskSlaEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(RiskSlaEvaluationService.class);

    private final RiskSlaTrackingRepository slaRepository;
    private final RiskSlaService slaService;

    public RiskSlaEvaluationService(RiskSlaTrackingRepository slaRepository,
                                   RiskSlaService slaService) {
        this.slaRepository = slaRepository;
        this.slaService = slaService;
    }

    /**
     * Avalia SLAs vencidos e aplica escalonamento automático
     * 
     * Processo:
     * 1. Busca SLAs PENDING
     * 2. Verifica se deadline passou
     * 3. Atualiza status para BREACHED
     * 4. Escalona para próximo nível
     * 
     * Preparado para uso futuro com @Scheduled (ex: @Scheduled(fixedRate = 60000))
     */
    public void evaluateExpiredSlas() {
        logger.info("Iniciando avaliação de SLAs vencidos");

        // Busca SLAs pendentes
        List<RiskSlaTracking> pendingSlas = slaRepository.findByStatusOrderByCreatedAtDesc(SlaStatus.PENDING);
        
        Instant now = Instant.now();
        int breachedCount = 0;
        int escalatedCount = 0;

        for (RiskSlaTracking sla : pendingSlas) {
            // Verifica se venceu
            if (now.isAfter(sla.getSlaDeadline())) {
                logger.warn("SLA vencido detectado - PR: {}, Deadline: {}", 
                           sla.getPullRequestId(), 
                           sla.getSlaDeadline());

                try {
                    // Atualiza status para BREACHED
                    RiskSlaTracking breached = sla.withStatus(SlaStatus.BREACHED);
                    slaRepository.save(breached);
                    breachedCount++;

                    // Aplica escalonamento automático
                    slaService.escalateSla(breached.getId());
                    escalatedCount++;

                    logger.info("SLA escalonado automaticamente - PR: {}, Novo nível: {}", 
                               breached.getPullRequestId(), 
                               breached.getCurrentLevel().next().getDescription());

                } catch (Exception e) {
                    logger.error("Erro ao processar SLA vencido - ID: {}", sla.getId(), e);
                }
            }
        }

        logger.info("Avaliação de SLAs concluída - Vencidos: {}, Escalonados: {}", 
                   breachedCount, escalatedCount);
    }

    /**
     * Avalia SLAs escalonados que continuam vencidos
     * Pode escalonar novamente se necessário
     */
    public void evaluateEscalatedSlas() {
        logger.info("Iniciando avaliação de SLAs escalonados");

        List<RiskSlaTracking> escalatedSlas = slaRepository.findByStatusOrderByCreatedAtDesc(SlaStatus.ESCALATED);
        
        Instant now = Instant.now();
        int reEscalatedCount = 0;

        for (RiskSlaTracking sla : escalatedSlas) {
            // Verifica se continua vencido por muito tempo (ex: mais de 30 min desde último escalonamento)
            if (sla.getLastEscalationAt() != null) {
                Instant reEscalationThreshold = sla.getLastEscalationAt().plusSeconds(1800); // 30 min
                
                if (now.isAfter(reEscalationThreshold)) {
                    logger.warn("SLA escalonado continua sem resposta - PR: {}, Nível: {}", 
                               sla.getPullRequestId(), 
                               sla.getCurrentLevel().getDescription());

                    try {
                        // Re-escalona se não está no nível máximo
                        if (sla.getCurrentLevel() != sla.getCurrentLevel().next()) {
                            slaService.escalateSla(sla.getId());
                            reEscalatedCount++;
                            
                            logger.info("SLA re-escalonado - PR: {}, Novo nível: {}", 
                                       sla.getPullRequestId(), 
                                       sla.getCurrentLevel().next().getDescription());
                        }
                    } catch (Exception e) {
                        logger.error("Erro ao re-escalonar SLA - ID: {}", sla.getId(), e);
                    }
                }
            }
        }

        logger.info("Avaliação de SLAs escalonados concluída - Re-escalonados: {}", reEscalatedCount);
    }

    /**
     * Força avaliação completa de todos os SLAs
     * Útil para testes e manutenção
     */
    public void forceEvaluateAll() {
        logger.info("Iniciando avaliação forçada de todos os SLAs");
        evaluateExpiredSlas();
        evaluateEscalatedSlas();
        logger.info("Avaliação forçada concluída");
    }
}

