package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.RiskDecisionFeedbackRequest;
import com.backoffice.alerta.rules.*;
import com.backoffice.alerta.repository.RiskDecisionFeedbackRepository;
import com.backoffice.alerta.repository.RiskDecisionAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço responsável por gerenciar feedbacks humanos pós-deploy
 * 
 * IMPORTANTE: Este serviço NÃO modifica:
 * - Auditorias (RiskDecisionAudit)
 * - Decisões de risco
 * - Cálculos de risco
 * - Análises de IA
 * 
 * Apenas registra feedback humano para aprendizado organizacional
 */
@Service
public class RiskDecisionFeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(RiskDecisionFeedbackService.class);

    private final RiskDecisionFeedbackRepository feedbackRepository;
    private final RiskDecisionAuditRepository auditRepository;

    public RiskDecisionFeedbackService(RiskDecisionFeedbackRepository feedbackRepository,
                                      RiskDecisionAuditRepository auditRepository) {
        this.feedbackRepository = feedbackRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Cria feedback humano pós-deploy
     * @param request Requisição com dados do feedback
     * @return Feedback criado
     * @throws IllegalArgumentException se dados inválidos
     * @throws IllegalStateException se feedback duplicado
     */
    public RiskDecisionFeedback createFeedback(RiskDecisionFeedbackRequest request) {
        logger.info("Criando feedback para auditoria: {}", request.getAuditId());

        // Validações
        validateRequest(request);
        
        // Busca auditoria referenciada
        RiskDecisionAudit audit = findAuditOrThrow(request.getAuditId());
        
        // Verifica se já existe feedback para esta auditoria
        checkForDuplicateFeedback(request.getAuditId());

        // Extrai dados da auditoria (não modifica nada)
        String pullRequestId = audit.getPullRequestId();
        FinalDecision finalDecision = audit.getFinalDecision();
        RiskLevel riskLevel = audit.getRiskLevel();

        // Cria feedback imutável
        RiskDecisionFeedback feedback = new RiskDecisionFeedback(
            request.getAuditId(),
            pullRequestId,
            finalDecision,
            riskLevel,
            request.getOutcome(),
            request.getComments(),
            request.getAuthor()
        );

        // Persiste
        RiskDecisionFeedback saved = feedbackRepository.save(feedback);
        logger.info("Feedback criado com sucesso: ID={}, auditId={}", 
                   saved.getId(), saved.getAuditId());

        return saved;
    }

    /**
     * Busca todos os feedbacks
     * @return Lista de feedbacks
     */
    public List<RiskDecisionFeedback> findAll() {
        return feedbackRepository.findAll();
    }

    /**
     * Busca feedback por ID da auditoria
     * @param auditId ID da auditoria
     * @return Optional com feedback, se existir
     */
    public Optional<RiskDecisionFeedback> findByAuditId(UUID auditId) {
        return feedbackRepository.findByAuditId(auditId);
    }

    /**
     * Busca feedback por ID
     * @param id ID do feedback
     * @return Optional com feedback, se existir
     */
    public Optional<RiskDecisionFeedback> findById(UUID id) {
        return feedbackRepository.findById(id);
    }

    /**
     * Valida campos obrigatórios
     */
    private void validateRequest(RiskDecisionFeedbackRequest request) {
        if (request.getAuditId() == null) {
            throw new IllegalArgumentException("auditId é obrigatório");
        }
        if (request.getOutcome() == null) {
            throw new IllegalArgumentException("outcome é obrigatório");
        }
        if (request.getComments() == null || request.getComments().isBlank()) {
            throw new IllegalArgumentException("comments é obrigatório e não pode estar vazio");
        }
        if (request.getAuthor() == null || request.getAuthor().isBlank()) {
            throw new IllegalArgumentException("author é obrigatório e não pode estar vazio");
        }
    }

    /**
     * Busca auditoria ou lança exceção se não encontrada
     */
    private RiskDecisionAudit findAuditOrThrow(UUID auditId) {
        List<RiskDecisionAudit> allAudits = auditRepository.findAll();
        
        return allAudits.stream()
            .filter(a -> a.getId().equals(auditId))
            .findFirst()
            .orElseThrow(() -> {
                logger.warn("Auditoria não encontrada: {}", auditId);
                return new IllegalArgumentException(
                    "Auditoria não encontrada: " + auditId
                );
            });
    }

    /**
     * Verifica se já existe feedback para a auditoria
     */
    private void checkForDuplicateFeedback(UUID auditId) {
        if (feedbackRepository.existsByAuditId(auditId)) {
            logger.warn("Feedback duplicado para auditoria: {}", auditId);
            throw new IllegalStateException(
                "Já existe feedback para esta auditoria: " + auditId
            );
        }
    }
}

