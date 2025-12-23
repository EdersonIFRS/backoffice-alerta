package com.backoffice.alerta.service;

import com.backoffice.alerta.ai.AiAnalysisRequest;
import com.backoffice.alerta.ai.AiAnalysisResponse;
import com.backoffice.alerta.ai.AiChangeAnalysisService;
import com.backoffice.alerta.ai.AiSignal;
import com.backoffice.alerta.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço orquestrador que gera explicações executivas de risco
 * combinando análise técnica, política e IA heurística.
 */
@Service
public class RiskExecutiveExplanationService {

    private final RiskAnalysisService riskAnalysisService;
    private final RiskPolicyService riskPolicyService;
    private final AiChangeAnalysisService aiChangeAnalysisService;

    public RiskExecutiveExplanationService(RiskAnalysisService riskAnalysisService,
                                          RiskPolicyService riskPolicyService,
                                          AiChangeAnalysisService aiChangeAnalysisService) {
        this.riskAnalysisService = riskAnalysisService;
        this.riskPolicyService = riskPolicyService;
        this.aiChangeAnalysisService = aiChangeAnalysisService;
    }

    public RiskExecutiveResponse generateExecutiveExplanation(RiskExecutiveRequest request) {
        // 1. Obter avaliação de política (inclui análise de risco)
        RiskPolicyRequest policyRequest = new RiskPolicyRequest(
            request.getPullRequestId(),
            request.getRuleVersion(),
            request.getPolicy(),
            request.getFiles()
        );
        RiskPolicyResponse policyResponse = riskPolicyService.evaluatePolicy(policyRequest);

        // 2. Obter análise de IA (opcional)
        AiAnalysisResponse aiResponse = null;
        try {
            List<AiAnalysisRequest.FileChange> aiFiles = convertToAiFileChanges(request.getFiles());
            if (!aiFiles.isEmpty()) {
                AiAnalysisRequest aiRequest = new AiAnalysisRequest(
                    request.getPullRequestId(),
                    aiFiles
                );
                aiResponse = aiChangeAnalysisService.analyzeChange(aiRequest);
            }
        } catch (Exception e) {
            // IA é opcional, continua sem ela
            System.err.println("Erro ao obter análise IA (ignorado): " + e.getMessage());
        }

        // 3. Gerar headline
        String headline = generateHeadline(policyResponse.getRiskLevel());

        // 4. Gerar summary
        String summary = generateSummary(request.getFiles(), policyResponse, aiResponse);

        // 5. Gerar key factors
        List<String> keyFactors = generateKeyFactors(request.getFiles(), policyResponse, aiResponse);

        // 6. Gerar recommended actions
        List<String> recommendedActions = generateRecommendedActions(request.getFiles(), 
                                                                     policyResponse, 
                                                                     request.getPolicy());

        ExecutiveSummary executiveSummary = new ExecutiveSummary(
            policyResponse.getRiskLevel(),
            policyResponse.getPolicyDecision(),
            headline,
            summary,
            keyFactors,
            recommendedActions
        );

        return new RiskExecutiveResponse(request.getPullRequestId(), executiveSummary);
    }

    private String generateHeadline(String riskLevel) {
        return switch (riskLevel) {
            case "BAIXO" -> "Pull Request apresenta baixo risco operacional";
            case "MÉDIO" -> "Pull Request apresenta risco moderado";
            case "ALTO" -> "Pull Request apresenta risco elevado";
            case "CRÍTICO" -> "Pull Request apresenta risco crítico para operação";
            default -> "Pull Request requer análise de risco";
        };
    }

    private String generateSummary(List<PullRequestRequest.FileChange> files, 
                                   RiskPolicyResponse policyResponse,
                                   AiAnalysisResponse aiResponse) {
        StringBuilder summary = new StringBuilder();

        // Tipo de módulo afetado
        String moduleType = inferModuleType(files);
        summary.append("A alteração afeta ").append(moduleType);

        // Volume de alteração
        int totalLines = files.stream().mapToInt(PullRequestRequest.FileChange::getLinesChanged).sum();
        if (totalLines > 100) {
            summary.append(", com grande volume de mudanças");
        } else if (totalLines > 50) {
            summary.append(", com volume moderado de mudanças");
        }

        // Testes
        boolean hasFilesWithoutTest = files.stream()
            .anyMatch(f -> f.getHasTest() == null || !f.getHasTest());
        if (hasFilesWithoutTest) {
            summary.append(" e ausência de testes automatizados");
        }

        // Política violada
        if (!"APROVADO".equals(policyResponse.getPolicyDecision())) {
            if ("BLOQUEADO".equals(policyResponse.getPolicyDecision())) {
                summary.append(", excedendo o risco máximo permitido pela política atual");
            } else {
                summary.append(", exigindo revisão manual conforme política de risco");
            }
        }

        summary.append(".");
        return summary.toString();
    }

    private List<String> generateKeyFactors(List<PullRequestRequest.FileChange> files,
                                            RiskPolicyResponse policyResponse,
                                            AiAnalysisResponse aiResponse) {
        List<String> factors = new ArrayList<>();

        // Módulo crítico
        if (isCriticalModule(files)) {
            String moduleName = extractDomain(files.get(0).getFilePath());
            factors.add("Alteração em módulo sensível de " + moduleName);
        }

        // Volume
        int totalLines = files.stream().mapToInt(PullRequestRequest.FileChange::getLinesChanged).sum();
        if (totalLines > 100) {
            factors.add("Grande volume de código modificado");
        } else if (totalLines > 50) {
            factors.add("Volume moderado de alterações");
        }

        // Testes
        boolean hasFilesWithoutTest = files.stream()
            .anyMatch(f -> f.getHasTest() == null || !f.getHasTest());
        if (hasFilesWithoutTest) {
            factors.add("Ausência de testes automatizados");
        }

        // Sinais da IA
        if (aiResponse != null && aiResponse.getAiAssessment() != null) {
            if (aiResponse.getAiAssessment().getSignals().contains(AiSignal.HIGH_BUSINESS_IMPACT)) {
                factors.add("Alto impacto no negócio identificado");
            }
            if (aiResponse.getAiAssessment().getSignals().contains(AiSignal.LOGIC_CHANGE)) {
                factors.add("Mudança significativa na lógica do sistema");
            }
        }

        // Histórico (inferido de certos módulos)
        if (hasHighIncidentHistory(files)) {
            factors.add("Histórico elevado de incidentes");
        }

        return factors.stream().limit(5).collect(Collectors.toList());
    }

    private List<String> generateRecommendedActions(List<PullRequestRequest.FileChange> files,
                                                    RiskPolicyResponse policyResponse,
                                                    RiskPolicyRequest.Policy policy) {
        List<String> actions = new ArrayList<>();

        // Sem testes
        boolean hasFilesWithoutTest = files.stream()
            .anyMatch(f -> f.getHasTest() == null || !f.getHasTest());
        if (hasFilesWithoutTest) {
            actions.add("Adicionar testes automatizados antes do merge");
        }

        // Muitas linhas
        int totalLines = files.stream().mapToInt(PullRequestRequest.FileChange::getLinesChanged).sum();
        if (totalLines > 100) {
            actions.add("Reduzir o escopo da alteração");
        }

        // Módulo crítico
        if (isCriticalModule(files)) {
            actions.add("Solicitar revisão manual especializada");
        }

        // Política violada
        if (!"APROVADO".equals(policyResponse.getPolicyDecision())) {
            actions.add("Reavaliar alteração conforme política de risco");
        }

        return actions;
    }

    private String inferModuleType(List<PullRequestRequest.FileChange> files) {
        for (PullRequestRequest.FileChange file : files) {
            String lowerPath = file.getFilePath().toLowerCase();
            if (lowerPath.contains("billing")) {
                return "módulos sensíveis de faturamento";
            }
            if (lowerPath.contains("payment")) {
                return "módulos sensíveis de pagamento";
            }
            if (lowerPath.contains("order")) {
                return "módulos de processamento de pedidos";
            }
            if (lowerPath.contains("pricing")) {
                return "módulos de precificação";
            }
        }
        return "módulos de suporte";
    }

    private String extractDomain(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.contains("billing")) return "faturamento";
        if (lowerPath.contains("payment")) return "pagamento";
        if (lowerPath.contains("order")) return "pedidos";
        if (lowerPath.contains("pricing")) return "precificação";
        return "negócio";
    }

    private boolean isCriticalModule(List<PullRequestRequest.FileChange> files) {
        return files.stream().anyMatch(file -> {
            String lowerPath = file.getFilePath().toLowerCase();
            return lowerPath.contains("billing") || lowerPath.contains("payment") 
                || lowerPath.contains("order") || lowerPath.contains("pricing");
        });
    }

    private boolean hasHighIncidentHistory(List<PullRequestRequest.FileChange> files) {
        return files.stream().anyMatch(file -> {
            String lowerPath = file.getFilePath().toLowerCase();
            return lowerPath.contains("billing") || lowerPath.contains("payment");
        });
    }

    private List<AiAnalysisRequest.FileChange> convertToAiFileChanges(
        List<PullRequestRequest.FileChange> files
    ) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        return files.stream()
            .filter(f -> f != null && f.getFilePath() != null && f.getLinesChanged() != null)
            .map(f -> new AiAnalysisRequest.FileChange(f.getFilePath(), f.getLinesChanged()))
            .collect(Collectors.toList());
    }
}

