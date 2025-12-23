package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.PullRequestRequest;
import com.backoffice.alerta.dto.RiskAnalysisResponse;
import com.backoffice.alerta.dto.RiskComparisonRequest;
import com.backoffice.alerta.dto.RiskComparisonResponse;
import com.backoffice.alerta.rules.RiskRuleSet;
import com.backoffice.alerta.rules.RiskRuleSetFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service para comparar análise de risco entre duas versões de regras
 */
@Service
public class RiskComparisonService {

    private final RiskAnalysisService riskAnalysisService;
    private final RiskRuleSetFactory ruleSetFactory;

    public RiskComparisonService(RiskAnalysisService riskAnalysisService, RiskRuleSetFactory ruleSetFactory) {
        this.riskAnalysisService = riskAnalysisService;
        this.ruleSetFactory = ruleSetFactory;
    }

    /**
     * Compara o risco de um Pull Request entre duas versões de regras
     */
    public RiskComparisonResponse compareRisk(RiskComparisonRequest request) {
        // Valida que as versões existem
        validateVersions(request.getFromVersion(), request.getToVersion());

        // Executa análise com versão FROM
        PullRequestRequest fromRequest = new PullRequestRequest(
            request.getPullRequestId(),
            request.getFiles(),
            request.getFromVersion()
        );
        RiskAnalysisResponse fromAnalysis = riskAnalysisService.analyzeRisk(fromRequest);

        // Executa análise com versão TO
        PullRequestRequest toRequest = new PullRequestRequest(
            request.getPullRequestId(),
            request.getFiles(),
            request.getToVersion()
        );
        RiskAnalysisResponse toAnalysis = riskAnalysisService.analyzeRisk(toRequest);

        // Calcula delta e decisão
        int riskDelta = toAnalysis.getRiskScore() - fromAnalysis.getRiskScore();
        String decision = determineDecision(riskDelta);

        // Gera explicação das diferenças
        List<String> explanation = generateExplanation(
            request.getFromVersion(),
            request.getToVersion(),
            riskDelta
        );

        return new RiskComparisonResponse(
            request.getPullRequestId(),
            request.getFromVersion(),
            request.getToVersion(),
            fromAnalysis.getRiskScore(),
            toAnalysis.getRiskScore(),
            riskDelta,
            fromAnalysis.getRiskLevel(),
            toAnalysis.getRiskLevel(),
            decision,
            explanation
        );
    }

    /**
     * Valida se as versões informadas existem
     */
    private void validateVersions(String fromVersion, String toVersion) {
        try {
            ruleSetFactory.getRuleSet(fromVersion);
            ruleSetFactory.getRuleSet(toVersion);
        } catch (Exception e) {
            throw new IllegalArgumentException("Versão de regras inválida");
        }
    }

    /**
     * Determina a decisão baseada no delta
     */
    private String determineDecision(int riskDelta) {
        if (riskDelta > 0) {
            return "RISCO AUMENTOU";
        } else if (riskDelta < 0) {
            return "RISCO DIMINUIU";
        } else {
            return "RISCO MANTEVE";
        }
    }

    /**
     * Gera explicação das diferenças entre versões
     */
    private List<String> generateExplanation(String fromVersion, String toVersion, int riskDelta) {
        List<String> explanation = new ArrayList<>();

        // Explicações específicas das diferenças entre v1 e v2
        if ("v1".equals(fromVersion) && "v2".equals(toVersion)) {
            explanation.add("A versão v2 considera controllers como semi-críticos (+15 pontos)");
            explanation.add("A versão v2 penaliza ausência de testes com +25 pontos");
            explanation.add("A versão v2 ajusta o histórico de incidentes por módulo");
        } else if ("v2".equals(fromVersion) && "v1".equals(toVersion)) {
            explanation.add("A versão v1 não considera controllers como críticos");
            explanation.add("A versão v1 penaliza ausência de testes com +20 pontos");
            explanation.add("A versão v1 possui histórico menor de incidentes por módulo");
        } else if (fromVersion.equals(toVersion)) {
            explanation.add("Ambas as versões aplicam as mesmas regras");
        }

        return explanation;
    }
}

