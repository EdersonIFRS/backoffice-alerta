package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.PullRequestRequest;
import com.backoffice.alerta.dto.RiskAnalysisResponse;
import com.backoffice.alerta.dto.RiskSimulationRequest;
import com.backoffice.alerta.dto.RiskSimulationResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RiskSimulationService {

    private final RiskAnalysisService riskAnalysisService;

    public RiskSimulationService(RiskAnalysisService riskAnalysisService) {
        this.riskAnalysisService = riskAnalysisService;
    }

    public RiskSimulationResponse simulateRisk(RiskSimulationRequest request) {
        // 1. Analisar situação atual
        PullRequestRequest currentRequest = new PullRequestRequest(
            request.getPullRequestId(),
            request.getBaseFiles(),
            request.getRuleVersion()
        );
        RiskAnalysisResponse currentAnalysis = riskAnalysisService.analyzeRisk(currentRequest);

        // 2. Clonar arquivos e aplicar simulação
        List<PullRequestRequest.FileChange> simulatedFiles = applySimulation(
            request.getBaseFiles(),
            request.getSimulation()
        );

        // 3. Analisar situação simulada
        PullRequestRequest simulatedRequest = new PullRequestRequest(
            request.getPullRequestId(),
            simulatedFiles,
            request.getRuleVersion()
        );
        RiskAnalysisResponse simulatedAnalysis = riskAnalysisService.analyzeRisk(simulatedRequest);

        // 4. Calcular delta e decisão
        int riskDelta = simulatedAnalysis.getRiskScore() - currentAnalysis.getRiskScore();
        String decision = generateDecision(riskDelta);

        // 5. Gerar explicação conceitual das mudanças
        List<String> explanation = generateSimulationExplanation(
            request.getBaseFiles(),
            request.getSimulation()
        );

        return new RiskSimulationResponse(
            request.getPullRequestId(),
            request.getRuleVersion() != null ? request.getRuleVersion() : "v1",
            currentAnalysis.getRiskScore(),
            simulatedAnalysis.getRiskScore(),
            riskDelta,
            currentAnalysis.getRiskLevel(),
            simulatedAnalysis.getRiskLevel(),
            decision,
            explanation
        );
    }

    private List<PullRequestRequest.FileChange> applySimulation(
        List<PullRequestRequest.FileChange> baseFiles,
        RiskSimulationRequest.Simulation simulation
    ) {
        List<PullRequestRequest.FileChange> clonedFiles = new ArrayList<>();

        for (PullRequestRequest.FileChange file : baseFiles) {
            // Clonar arquivo
            String filePath = file.getFilePath();
            int linesChanged = file.getLinesChanged();
            boolean hasTest = file.getHasTest();

            // Aplicar overrideLinesChanged
            if (simulation.getOverrideLinesChanged() != null) {
                linesChanged = simulation.getOverrideLinesChanged();
            }

            // Aplicar applyTests
            if (simulation.getApplyTests() != null && simulation.getApplyTests()) {
                hasTest = true;
            }

            clonedFiles.add(new PullRequestRequest.FileChange(filePath, linesChanged, hasTest));
        }

        return clonedFiles;
    }

    private String generateDecision(int riskDelta) {
        if (riskDelta < 0) {
            return "RISCO DIMINUIU";
        } else if (riskDelta > 0) {
            return "RISCO AUMENTOU";
        } else {
            return "RISCO MANTEVE";
        }
    }

    private List<String> generateSimulationExplanation(
        List<PullRequestRequest.FileChange> baseFiles,
        RiskSimulationRequest.Simulation simulation
    ) {
        List<String> explanation = new ArrayList<>();

        // Verificar se a simulação aplica testes
        if (simulation.getApplyTests() != null && simulation.getApplyTests()) {
            boolean anyFileWithoutTest = baseFiles.stream()
                .anyMatch(file -> !file.getHasTest());
            
            if (anyFileWithoutTest) {
                explanation.add("Aplicação de testes remove penalidade por ausência de testes");
            } else {
                explanation.add("Todos os arquivos já possuem testes, simulação não altera este fator");
            }
        }

        // Verificar se a simulação sobrescreve linhas alteradas
        if (simulation.getOverrideLinesChanged() != null) {
            int newLines = simulation.getOverrideLinesChanged();
            boolean anyChange = baseFiles.stream()
                .anyMatch(file -> file.getLinesChanged() != newLines);
            
            if (anyChange) {
                if (newLines > 100) {
                    explanation.add("Redução de linhas alteradas para " + newLines + " linhas (penalidade alta mantida)");
                } else if (newLines >= 50) {
                    explanation.add("Redução de linhas alteradas para " + newLines + " linhas (penalidade moderada)");
                } else {
                    explanation.add("Redução de linhas alteradas para " + newLines + " linhas (sem penalidade por volume)");
                }
            } else {
                explanation.add("Linhas alteradas já estão em " + newLines + ", simulação não altera este fator");
            }
        }

        // Se nenhuma simulação foi aplicada
        if (explanation.isEmpty()) {
            explanation.add("Nenhuma mudança simulada");
        }

        return explanation;
    }
}

