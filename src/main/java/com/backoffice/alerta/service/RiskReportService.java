package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Serviço orquestrador que gera relatórios consolidados de risco
 * agregando análise técnica, política, explicação executiva e recomendações.
 */
@Service
public class RiskReportService {

    private final RiskPolicyService riskPolicyService;
    private final RiskExecutiveExplanationService executiveService;
    private final RiskRecommendationService recommendationService;

    public RiskReportService(RiskPolicyService riskPolicyService,
                            RiskExecutiveExplanationService executiveService,
                            RiskRecommendationService recommendationService) {
        this.riskPolicyService = riskPolicyService;
        this.executiveService = executiveService;
        this.recommendationService = recommendationService;
    }

    public RiskReportResponse generateReport(RiskReportRequest request) {
        // 1. Obter avaliação de política (inclui análise de risco)
        RiskPolicyRequest policyRequest = new RiskPolicyRequest(
            request.getPullRequestId(),
            request.getRuleVersion(),
            request.getPolicy(),
            request.getFiles()
        );
        RiskPolicyResponse policyResponse = riskPolicyService.evaluatePolicy(policyRequest);

        // 2. Obter explicação executiva
        RiskExecutiveRequest executiveRequest = new RiskExecutiveRequest(
            request.getPullRequestId(),
            request.getRuleVersion(),
            request.getPolicy(),
            request.getFiles()
        );
        RiskExecutiveResponse executiveResponse = executiveService.generateExecutiveExplanation(executiveRequest);

        // 3. Obter recomendações
        RiskRecommendationRequest recommendationRequest = new RiskRecommendationRequest(
            request.getPullRequestId(),
            request.getRuleVersion(),
            request.getPolicy().getMaxAllowedRisk(),
            request.getFiles()
        );
        RiskRecommendationResponse recommendationResponse = recommendationService.recommendActions(recommendationRequest);

        // 4. Montar o relatório consolidado
        ExecutiveSummary executiveSummary = executiveResponse.getExecutiveSummary();
        List<String> recommendations = recommendationResponse.getRecommendations().stream()
            .map(RiskRecommendation::getDescription)
            .toList();
        String generatedAt = Instant.now().toString();

        RiskReport riskReport = new RiskReport(
            policyResponse.getRiskLevel(),
            policyResponse.getPolicyDecision(),
            executiveSummary,
            recommendations,
            generatedAt
        );

        return new RiskReportResponse(request.getPullRequestId(), riskReport);
    }
}

