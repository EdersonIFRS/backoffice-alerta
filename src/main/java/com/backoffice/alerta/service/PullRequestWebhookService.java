package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.*;
import com.backoffice.alerta.rules.RiskDecisionAudit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço de integração CI/CD via webhook de Pull Request
 * 
 * RESPONSABILIDADE: Orquestração EXCLUSIVA
 * - NÃO cria lógica de risco
 * - NÃO recalcula métricas
 * - NÃO chama IA
 * - NÃO modifica dados históricos
 * - Apenas coordena serviços existentes:
 *   * BusinessImpactAnalysisService (US #16)
 *   * RiskDecisionService (US #18)
 *   * RiskDecisionAuditService (US #20) - automático via RiskDecisionService
 * 
 * Permite que pipelines CI/CD (GitHub Actions, GitLab CI) acionem
 * automaticamente o motor de risco quando um PR for criado/atualizado
 */
@Service
public class PullRequestWebhookService {

    private static final Logger log = LoggerFactory.getLogger(PullRequestWebhookService.class);

    private final BusinessImpactAnalysisService impactService;
    private final RiskDecisionService decisionService;
    private final RiskDecisionAuditService auditService;

    public PullRequestWebhookService(BusinessImpactAnalysisService impactService,
                                    RiskDecisionService decisionService,
                                    RiskDecisionAuditService auditService) {
        this.impactService = impactService;
        this.decisionService = decisionService;
        this.auditService = auditService;
    }

    /**
     * Processa webhook de Pull Request e retorna decisão de risco
     * 
     * Fluxo:
     * 1. Valida request
     * 2. Cria BusinessImpactRequest
     * 3. Executa análise de impacto (US #16)
     * 4. Cria RiskDecisionRequest
     * 5. Executa decisão de risco (US #18)
     * 6. Auditoria criada automaticamente (US #20)
     * 7. Retorna response para CI/CD pipeline
     * 
     * @param request Dados do webhook
     * @return Response com decisão de risco e auditId
     * @throws IllegalArgumentException se request inválido
     */
    public PullRequestWebhookResponse processWebhook(PullRequestWebhookRequest request) {
        log.info("Processando webhook - provider: {}, PR: {}, event: {}, env: {}",
                request.getProvider(),
                request.getPullRequest() != null ? request.getPullRequest().getId() : "null",
                request.getEventType(),
                request.getEnvironment());

        // 1. Validar request
        validateRequest(request);

        String pullRequestId = request.getPullRequest().getId();

        // 2. Criar BusinessImpactRequest
        BusinessImpactRequest impactRequest = new BusinessImpactRequest(
                pullRequestId,
                request.getChangedFiles()
        );

        // 3. Executar análise de impacto (reutiliza US #16)
        log.debug("Executando análise de impacto para PR: {}", pullRequestId);
        BusinessImpactResponse impactResponse = impactService.analyze(impactRequest);

        // 4. Criar RiskDecisionRequest (usa construtor completo)
        RiskDecisionRequest decisionRequest = new RiskDecisionRequest(
                pullRequestId,
                "v2",  // versão da regra
                request.getEnvironment(),
                request.getChangeType(),
                null,  // policy padrão
                request.getChangedFiles()
        );

        // 5. Executar decisão de risco (reutiliza US #18)
        log.debug("Executando decisão de risco para PR: {}", pullRequestId);
        RiskDecisionResponse decisionResponse = decisionService.decide(decisionRequest);

        // 6. Buscar auditId (criada automaticamente pelo RiskDecisionService via US #20)
        UUID auditId = findAuditIdForPullRequest(pullRequestId);

        // 7. Construir response (usa requiredActions em vez de restrictions)
        PullRequestWebhookResponse response = new PullRequestWebhookResponse(
                pullRequestId,
                decisionResponse.getFinalDecision(),
                decisionResponse.getRiskLevel(),
                request.getEnvironment(),
                decisionResponse.getRequiredActions(),
                auditId
        );

        log.info("Webhook processado - PR: {}, decisão: {}, risco: {}, auditId: {}",
                pullRequestId,
                decisionResponse.getFinalDecision(),
                decisionResponse.getRiskLevel(),
                auditId);

        return response;
    }

    /**
     * Valida request de webhook
     * 
     * @throws IllegalArgumentException se inválido
     */
    private void validateRequest(PullRequestWebhookRequest request) {
        if (request.getProvider() == null) {
            throw new IllegalArgumentException("Campo 'provider' é obrigatório");
        }

        if (request.getPullRequest() == null) {
            throw new IllegalArgumentException("Campo 'pullRequest' é obrigatório");
        }

        if (request.getPullRequest().getId() == null || request.getPullRequest().getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Campo 'pullRequest.id' é obrigatório");
        }

        if (request.getChangedFiles() == null || request.getChangedFiles().isEmpty()) {
            throw new IllegalArgumentException("Campo 'changedFiles' é obrigatório e não pode ser vazio");
        }

        if (request.getEnvironment() == null) {
            throw new IllegalArgumentException("Campo 'environment' é obrigatório");
        }

        if (request.getChangeType() == null) {
            throw new IllegalArgumentException("Campo 'changeType' é obrigatório");
        }
    }

    /**
     * Busca auditId para o Pull Request
     * 
     * A auditoria é criada automaticamente pelo RiskDecisionService (US #20)
     * quando decide() é chamado
     */
    private UUID findAuditIdForPullRequest(String pullRequestId) {
        try {
            // Busca a auditoria mais recente para este PR
            return auditService.findByPullRequestId(pullRequestId).stream()
                    .findFirst()
                    .map(RiskDecisionAudit::getId)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Não foi possível buscar auditId para PR: {}", pullRequestId, e);
            return null;
        }
    }
}

