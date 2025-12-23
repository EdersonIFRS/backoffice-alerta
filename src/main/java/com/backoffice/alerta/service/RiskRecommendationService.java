package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.PullRequestRequest;
import com.backoffice.alerta.dto.RiskAnalysisResponse;
import com.backoffice.alerta.dto.RiskRecommendation;
import com.backoffice.alerta.dto.RiskRecommendationRequest;
import com.backoffice.alerta.dto.RiskRecommendationResponse;
import com.backoffice.alerta.rules.RiskRuleSet;
import com.backoffice.alerta.rules.RiskRuleSetFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class RiskRecommendationService {

    private static final List<String> RISK_LEVELS = Arrays.asList("BAIXO", "MÉDIO", "ALTO", "CRÍTICO");

    private final RiskAnalysisService riskAnalysisService;
    private final RiskRuleSetFactory ruleSetFactory;

    public RiskRecommendationService(RiskAnalysisService riskAnalysisService, RiskRuleSetFactory ruleSetFactory) {
        this.riskAnalysisService = riskAnalysisService;
        this.ruleSetFactory = ruleSetFactory;
    }

    public RiskRecommendationResponse recommendActions(RiskRecommendationRequest request) {
        // 1. Executar análise de risco atual
        PullRequestRequest analysisRequest = new PullRequestRequest(
            request.getPullRequestId(),
            request.getFiles(),
            request.getRuleVersion()
        );
        RiskAnalysisResponse currentAnalysis = riskAnalysisService.analyzeRisk(analysisRequest);

        // 2. Obter RuleSet para calcular impactos
        RiskRuleSet ruleSet = ruleSetFactory.getRuleSet(request.getRuleVersion());

        // 3. Gerar recomendações baseadas nos fatores de risco
        List<RiskRecommendation> recommendations = generateRecommendations(
            request.getFiles(),
            ruleSet
        );

        // 4. Calcular score esperado
        int totalImpact = recommendations.stream()
            .mapToInt(RiskRecommendation::getEstimatedImpact)
            .sum();
        int expectedScore = Math.max(0, Math.min(100, currentAnalysis.getRiskScore() + totalImpact));
        String expectedLevel = calculateRiskLevel(expectedScore);

        return new RiskRecommendationResponse(
            request.getPullRequestId(),
            request.getRuleVersion() != null ? request.getRuleVersion() : "v1",
            currentAnalysis.getRiskScore(),
            currentAnalysis.getRiskLevel(),
            request.getTargetRiskLevel(),
            recommendations,
            expectedScore,
            expectedLevel
        );
    }

    private List<RiskRecommendation> generateRecommendations(
        List<PullRequestRequest.FileChange> files,
        RiskRuleSet ruleSet
    ) {
        List<RiskRecommendation> recommendations = new ArrayList<>();

        // Verificar arquivos sem teste (trata null como false)
        boolean hasFilesWithoutTest = files.stream()
            .anyMatch(f -> f.getHasTest() == null || !f.getHasTest());
        if (hasFilesWithoutTest) {
            List<String> filesWithoutTest = files.stream()
                .filter(f -> f.getHasTest() == null || !f.getHasTest())
                .map(PullRequestRequest.FileChange::getFilePath)
                .map(this::getFileName)
                .toList();

            String description = filesWithoutTest.size() == 1
                ? String.format("Adicionar testes automatizados para %s", filesWithoutTest.get(0))
                : String.format("Adicionar testes automatizados para %d arquivo(s)", filesWithoutTest.size());

            recommendations.add(new RiskRecommendation(
                "ADICIONAR_TESTES",
                description,
                -ruleSet.getNoTestScore()
            ));
        }

        // Verificar muitas linhas alteradas
        boolean hasHighLineCount = files.stream().anyMatch(f -> f.getLinesChanged() > 100);
        if (hasHighLineCount) {
            recommendations.add(new RiskRecommendation(
                "REDUZIR_LINHAS_ALTERADAS",
                "Reduzir alterações para menos de 100 linhas por arquivo",
                -ruleSet.getLinesOver100Score()
            ));
        }

        // Verificar arquivos críticos
        boolean hasCriticalFiles = files.stream().anyMatch(f -> ruleSet.isCriticalFile(f.getFilePath()));
        if (hasCriticalFiles) {
            List<String> criticalFiles = files.stream()
                .filter(f -> ruleSet.isCriticalFile(f.getFilePath()))
                .map(PullRequestRequest.FileChange::getFilePath)
                .map(this::getFileName)
                .toList();

            String description = criticalFiles.size() == 1
                ? String.format("Considerar segmentar módulo crítico: %s", criticalFiles.get(0))
                : String.format("Considerar segmentar %d módulo(s) crítico(s)", criticalFiles.size());

            recommendations.add(new RiskRecommendation(
                "SEGMENTAR_MODULO",
                description,
                -ruleSet.getCriticalFileScore()
            ));
        }

        // Verificar histórico de incidentes elevado
        boolean hasHighIncidentHistory = files.stream()
            .anyMatch(f -> ruleSet.getIncidentHistory(f.getFilePath()) >= 3);
        if (hasHighIncidentHistory) {
            recommendations.add(new RiskRecommendation(
                "REVISAO_MANUAL",
                "Solicitar revisão manual de especialista para arquivos com histórico elevado",
                -ruleSet.getMaxIncidentScore()
            ));
        }

        return recommendations;
    }

    private String getFileName(String filePath) {
        if (filePath == null) {
            return "";
        }
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    private String calculateRiskLevel(int score) {
        if (score < 30) {
            return "BAIXO";
        } else if (score < 60) {
            return "MÉDIO";
        } else if (score < 80) {
            return "ALTO";
        } else {
            return "CRÍTICO";
        }
    }
}

