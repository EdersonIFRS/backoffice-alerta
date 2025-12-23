package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para sugestão automática de testes baseada em análise de risco e heurísticas IA.
 * Reusa RiskAnalysisService para identificar arquivos críticos sem duplicar lógica.
 */
@Service
public class TestSuggestionService {

    private final RiskAnalysisService riskAnalysisService;

    public TestSuggestionService(RiskAnalysisService riskAnalysisService) {
        this.riskAnalysisService = riskAnalysisService;
    }

    public TestSuggestionResponse suggestTests(TestSuggestionRequest request) {
        // 1. Executar análise de risco para identificar criticidade
        PullRequestRequest analysisRequest = new PullRequestRequest(
            request.getPullRequestId(),
            request.getFiles(),
            request.getRuleVersion()
        );
        RiskAnalysisResponse riskAnalysis = riskAnalysisService.analyzeRisk(analysisRequest);

        // 2. Gerar sugestões baseadas em heurísticas IA
        List<TestRecommendation> suggestions = generateTestSuggestions(
            request.getFiles(),
            request.getTargetCoverageLevel(),
            riskAnalysis.getRiskLevel()
        );

        // 3. Calcular impacto total de cobertura
        int totalImpact = suggestions.stream()
            .mapToInt(TestRecommendation::getEstimatedImpact)
            .sum();

        return new TestSuggestionResponse(
            request.getPullRequestId(),
            request.getRuleVersion() != null ? request.getRuleVersion() : "v1",
            suggestions,
            totalImpact
        );
    }

    private List<TestRecommendation> generateTestSuggestions(
        List<PullRequestRequest.FileChange> files,
        String targetCoverageLevel,
        String overallRiskLevel
    ) {
        List<TestRecommendation> suggestions = new ArrayList<>();

        for (PullRequestRequest.FileChange file : files) {
            String fileName = getFileName(file.getFilePath());
            boolean isCritical = isCriticalFile(file.getFilePath());
            boolean hasTest = file.getHasTest() != null && file.getHasTest();
            int linesChanged = file.getLinesChanged();

            // Arquivos críticos sem teste → UNIT + INTEGRATION
            if (isCritical && !hasTest) {
                suggestions.add(new TestRecommendation(
                    fileName,
                    "UNIT",
                    String.format("Adicionar testes unitários para %s. Arquivo crítico sem cobertura de testes.", fileName),
                    calculateImpact(linesChanged, "UNIT", isCritical)
                ));

                suggestions.add(new TestRecommendation(
                    fileName,
                    "INTEGRATION",
                    String.format("Adicionar testes de integração para %s. Validar fluxo completo com dependências.", fileName),
                    calculateImpact(linesChanged, "INTEGRATION", isCritical)
                ));
            }
            // Arquivos críticos com teste → Sugerir cobertura adicional
            else if (isCritical && hasTest && "ALTA".equals(targetCoverageLevel)) {
                suggestions.add(new TestRecommendation(
                    fileName,
                    "INTEGRATION",
                    String.format("Expandir testes de integração para %s. Cobrir cenários de erro e edge cases.", fileName),
                    calculateImpact(linesChanged, "INTEGRATION", isCritical) / 2
                ));
            }
            // Arquivos médios sem teste → UNIT ou MOCK
            else if (!isCritical && !hasTest && linesChanged > 50) {
                if (isController(file.getFilePath()) || isService(file.getFilePath())) {
                    suggestions.add(new TestRecommendation(
                        fileName,
                        "MOCK",
                        String.format("Criar mocks para testar %s sem depender de dependências externas.", fileName),
                        calculateImpact(linesChanged, "MOCK", false)
                    ));
                } else {
                    suggestions.add(new TestRecommendation(
                        fileName,
                        "UNIT",
                        String.format("Adicionar testes unitários para %s. Validar lógica de negócio.", fileName),
                        calculateImpact(linesChanged, "UNIT", false)
                    ));
                }
            }
            // Arquivos baixos sem teste → Apenas se cobertura ALTA
            else if (!isCritical && !hasTest && "ALTA".equals(targetCoverageLevel)) {
                suggestions.add(new TestRecommendation(
                    fileName,
                    "UNIT",
                    String.format("Adicionar testes unitários básicos para %s. Melhorar cobertura geral.", fileName),
                    calculateImpact(linesChanged, "UNIT", false) / 2
                ));
            }
        }

        return suggestions;
    }

    private int calculateImpact(int linesChanged, String testType, boolean isCritical) {
        int baseImpact = switch (testType) {
            case "UNIT" -> 15;
            case "INTEGRATION" -> 25;
            case "MOCK" -> 20;
            default -> 10;
        };

        // Ajustar por volume de linhas
        if (linesChanged > 200) {
            baseImpact += 15;
        } else if (linesChanged > 100) {
            baseImpact += 10;
        } else if (linesChanged > 50) {
            baseImpact += 5;
        }

        // Ajustar por criticidade
        if (isCritical) {
            baseImpact += 10;
        }

        return Math.min(baseImpact, 40); // Cap máximo de 40%
    }

    private boolean isCriticalFile(String filePath) {
        String lowerPath = filePath.toLowerCase();
        return lowerPath.contains("billing") 
            || lowerPath.contains("payment") 
            || lowerPath.contains("order") 
            || lowerPath.contains("pricing");
    }

    private boolean isController(String filePath) {
        return filePath.toLowerCase().contains("controller");
    }

    private boolean isService(String filePath) {
        return filePath.toLowerCase().contains("service");
    }

    private String getFileName(String filePath) {
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }
}

