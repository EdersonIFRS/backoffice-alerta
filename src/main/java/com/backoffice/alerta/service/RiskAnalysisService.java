package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.PullRequestRequest;
import com.backoffice.alerta.dto.RiskAnalysisResponse;
import com.backoffice.alerta.rules.RiskRuleSet;
import com.backoffice.alerta.rules.RiskRuleSetFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RiskAnalysisService {

    private final RiskRuleSetFactory ruleSetFactory;

    public RiskAnalysisService(RiskRuleSetFactory ruleSetFactory) {
        this.ruleSetFactory = ruleSetFactory;
    }

    public RiskAnalysisResponse analyzeRisk(PullRequestRequest request) {
        // Obtém o conjunto de regras baseado na versão solicitada
        RiskRuleSet rules = ruleSetFactory.getRuleSet(request.getRuleVersion());
        
        int riskScore = 0;
        List<String> explanation = new ArrayList<>();
        explanation.add("Análise realizada com regras " + rules.getVersion());
        
        int totalIncidents = 0;

        // Analisa cada arquivo
        for (PullRequestRequest.FileChange file : request.getFiles()) {
            String fileName = extractFileName(file.getFilePath());
            
            // Verifica se é arquivo crítico
            if (rules.isCriticalFile(file.getFilePath())) {
                riskScore += rules.getCriticalFileScore();
                explanation.add("Arquivo crítico detectado: " + fileName + " (+" + rules.getCriticalFileScore() + " pontos)");
            }
            // Verifica se é arquivo semi-crítico
            else if (rules.isSemiCriticalFile(file.getFilePath())) {
                riskScore += rules.getSemiCriticalFileScore();
                explanation.add("Arquivo semi-crítico detectado: " + fileName + " (+" + rules.getSemiCriticalFileScore() + " pontos)");
            }

            // Verifica linhas alteradas
            if (file.getLinesChanged() > 100) {
                riskScore += rules.getLinesOver100Score();
                explanation.add("Arquivo com mais de 100 linhas alteradas: " + fileName + " (+" + rules.getLinesOver100Score() + " pontos)");
            } else if (file.getLinesChanged() >= 50) {
                riskScore += rules.getLines50To100Score();
                explanation.add("Arquivo com 50-100 linhas alteradas: " + fileName + " (+" + rules.getLines50To100Score() + " pontos)");
            }

            // Verifica se não possui testes (trata null como false)
            if (file.getHasTest() == null || !file.getHasTest()) {
                riskScore += rules.getNoTestScore();
                explanation.add("Arquivo sem testes: " + fileName + " (+" + rules.getNoTestScore() + " pontos)");
            }

            // Acumula incidentes históricos
            int incidents = rules.getIncidentHistory(file.getFilePath());
            totalIncidents += incidents;
        }

        // Aplica pontuação de histórico de incidentes
        if (totalIncidents > 0) {
            int incidentScore = Math.min(totalIncidents * rules.getIncidentScore(), rules.getMaxIncidentScore());
            riskScore += incidentScore;
            explanation.add("Histórico de " + totalIncidents + " incidente(s) detectado(s) (+" + incidentScore + " pontos)");
        }

        // Garante que o score não ultrapasse o máximo
        riskScore = Math.min(riskScore, rules.getMaxScore());

        String riskLevel = determineRiskLevel(riskScore);

        return new RiskAnalysisResponse(
                request.getPullRequestId(),
                riskScore,
                riskLevel,
                explanation,
                rules.getVersion()
        );
    }

    private String determineRiskLevel(int score) {
        if (score >= 80) {
            return "CRÍTICO";
        } else if (score >= 60) {
            return "ALTO";
        } else if (score >= 30) {
            return "MÉDIO";
        } else {
            return "BAIXO";
        }
    }

    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        
        // Trata separadores Windows e Unix
        String[] parts = filePath.replace("\\", "/").split("/");
        return parts[parts.length - 1];
    }
}

