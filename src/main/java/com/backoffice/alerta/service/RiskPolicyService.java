package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.PullRequestRequest;
import com.backoffice.alerta.dto.RiskAnalysisResponse;
import com.backoffice.alerta.dto.RiskPolicyRequest;
import com.backoffice.alerta.dto.RiskPolicyResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RiskPolicyService {

    private static final List<String> RISK_LEVELS = Arrays.asList("BAIXO", "MÉDIO", "ALTO", "CRÍTICO");

    private final RiskAnalysisService riskAnalysisService;

    public RiskPolicyService(RiskAnalysisService riskAnalysisService) {
        this.riskAnalysisService = riskAnalysisService;
    }

    public RiskPolicyResponse evaluatePolicy(RiskPolicyRequest request) {
        // 1. Executar análise de risco
        PullRequestRequest analysisRequest = new PullRequestRequest(
            request.getPullRequestId(),
            request.getFiles(),
            request.getRuleVersion()
        );
        RiskAnalysisResponse analysisResult = riskAnalysisService.analyzeRisk(analysisRequest);

        // 2. Avaliar contra política
        String maxAllowedRisk = request.getPolicy().getMaxAllowedRisk();
        String actualRiskLevel = analysisResult.getRiskLevel();

        String policyDecision = evaluateRiskLevel(actualRiskLevel, maxAllowedRisk);
        String reason = generateReason(actualRiskLevel, maxAllowedRisk, policyDecision);

        return new RiskPolicyResponse(
            request.getPullRequestId(),
            request.getRuleVersion() != null ? request.getRuleVersion() : "v1",
            analysisResult.getRiskScore(),
            actualRiskLevel,
            policyDecision,
            reason
        );
    }

    private String evaluateRiskLevel(String actualRiskLevel, String maxAllowedRisk) {
        int actualIndex = RISK_LEVELS.indexOf(actualRiskLevel);
        int maxAllowedIndex = RISK_LEVELS.indexOf(maxAllowedRisk);

        if (actualIndex == -1 || maxAllowedIndex == -1) {
            throw new IllegalArgumentException("Nível de risco inválido");
        }

        if (actualIndex <= maxAllowedIndex) {
            return "APROVADO";
        } else if (actualIndex == maxAllowedIndex + 1) {
            return "REVISÃO OBRIGATÓRIA";
        } else {
            return "BLOQUEADO";
        }
    }

    private String generateReason(String actualRiskLevel, String maxAllowedRisk, String policyDecision) {
        switch (policyDecision) {
            case "APROVADO":
                return String.format("O nível de risco %s está dentro do limite permitido (%s)", 
                    actualRiskLevel, maxAllowedRisk);
            case "REVISÃO OBRIGATÓRIA":
                return String.format("O nível de risco %s está um nível acima do permitido (%s), exige revisão manual", 
                    actualRiskLevel, maxAllowedRisk);
            case "BLOQUEADO":
                return String.format("O nível de risco %s excede o máximo permitido (%s)", 
                    actualRiskLevel, maxAllowedRisk);
            default:
                return "Decisão desconhecida";
        }
    }
}

