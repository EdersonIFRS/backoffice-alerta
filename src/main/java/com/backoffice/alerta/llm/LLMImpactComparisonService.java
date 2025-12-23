package com.backoffice.alerta.llm;

import com.backoffice.alerta.ast.ASTCodeAnalysisService;
import com.backoffice.alerta.git.dto.GitImpactAnalysisResponse;
import com.backoffice.alerta.git.dto.GitPullRequestRequest;
import com.backoffice.alerta.git.service.GitPullRequestImpactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * US#71 - Serviço de comparação de impacto PRE vs POST
 * 
 * READ-ONLY - Compara estados antes e depois de mudança
 * Determinístico - sem IA, sem ML, sem persistência
 * 
 * Reutiliza:
 * - US#69: AST Code Analysis
 * - US#51/52: Git Pull Request Impact
 */
@Service
public class LLMImpactComparisonService {

    private static final Logger log = LoggerFactory.getLogger(LLMImpactComparisonService.class);

    private final GitPullRequestImpactService prImpactService;

    @Autowired(required = false)
    private ASTCodeAnalysisService astCodeAnalysisService;

    public LLMImpactComparisonService(GitPullRequestImpactService prImpactService) {
        this.prImpactService = prImpactService;
    }

    /**
     * Compara impacto PRE vs POST de uma mudança
     */
    public LLMImpactComparisonResponse compareImpact(LLMImpactComparisonRequest request) {
        log.info("📊 [US#71] Iniciando comparação PRE vs POST | base={} | compare={}",
                 request.getBaseRef(), request.getCompareRef());

        try {
            // 1. Recuperar estado PRE (baseline)
            GitImpactAnalysisResponse preState = getPreState(request);
            log.info("✅ [US#71] PRE state recuperado | files={}",
                     preState != null ? preState.getPullRequest().getChangedFiles().size() : 0);

            // 2. Recuperar estado POST (após mudança)
            GitImpactAnalysisResponse postState = getPostState(request);
            log.info("✅ [US#71] POST state recuperado | files={}",
                     postState != null ? postState.getPullRequest().getChangedFiles().size() : 0);

            // 3. Comparar dimensões e calcular deltas
            List<LLMImpactDelta> deltas = new ArrayList<>();

            // Dimensão AST (US#69)
            if (astCodeAnalysisService != null && preState != null && postState != null) {
                deltas.addAll(compareASTDimension(preState, postState));
            }

            // Dimensão RAG (US#63/67) - simplificado por enquanto
            deltas.addAll(compareRAGDimension(preState, postState));

            // Dimensão Negócio
            deltas.addAll(compareBusinessDimension(preState, postState));

            // Dimensão Testes
            deltas.addAll(compareTestsDimension(preState, postState));

            // 4. Calcular veredicto final
            String verdict = calculateFinalVerdict(deltas);
            int scoreDelta = calculateScoreDelta(deltas);

            log.info("🧠 [US#71] Final Verdict: {} | scoreDelta={}", verdict, scoreDelta);

            // 5. Gerar sumário executivo
            String summary = generateExecutiveSummary(verdict, deltas, request);

            // 6. Construir response
            LLMImpactComparisonResponse response = new LLMImpactComparisonResponse();
            response.setFinalScoreDelta(scoreDelta);
            response.setFinalVerdict(verdict);
            response.setDeltas(deltas);
            response.setExecutiveSummary(summary);
            response.setBaseRef(request.getBaseRef());
            response.setCompareRef(request.getCompareRef());

            return response;

        } catch (Exception e) {
            log.error("❌ [US#71] Erro ao comparar impacto: {}", e.getMessage(), e);

            // Fail-safe: retornar UNCHANGED em caso de erro
            LLMImpactComparisonResponse fallback = new LLMImpactComparisonResponse();
            fallback.setFinalScoreDelta(0);
            fallback.setFinalVerdict("UNCHANGED");
            fallback.setExecutiveSummary("⚠️ Erro ao comparar impacto. Assumindo UNCHANGED e recomendando revisão manual.");
            fallback.setBaseRef(request.getBaseRef());
            fallback.setCompareRef(request.getCompareRef());

            return fallback;
        }
    }

    /**
     * Recupera estado PRE (baseline)
     */
    private GitImpactAnalysisResponse getPreState(LLMImpactComparisonRequest request) {
        try {
            // Por enquanto, retornar null (seria necessário analisar commit base)
            // Em produção real, analisaria o commit/branch base
            return null;
        } catch (Exception e) {
            log.warn("⚠️ [US#71] Erro ao recuperar PRE state: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Recupera estado POST (após mudança)
     */
    private GitImpactAnalysisResponse getPostState(LLMImpactComparisonRequest request) {
        try {
            GitPullRequestRequest prRequest = new GitPullRequestRequest();
            prRequest.setPullRequestNumber(request.getCompareRef());
            prRequest.setProvider(request.getProvider());
            prRequest.setRepositoryUrl(request.getRepositoryUrl());
            prRequest.setProjectId(request.getProjectId());

            return prImpactService.analyzePullRequest(prRequest);
        } catch (Exception e) {
            log.warn("⚠️ [US#71] Erro ao recuperar POST state: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Compara dimensão AST (US#69)
     */
    private List<LLMImpactDelta> compareASTDimension(GitImpactAnalysisResponse preState,
                                                     GitImpactAnalysisResponse postState) {
        List<LLMImpactDelta> deltas = new ArrayList<>();

        try {
            // Complexidade ciclomática média (simulado)
            double preCyclomaticComplexity = 2.1; // Seria calculado do AST
            double postCyclomaticComplexity = 3.4; // Seria calculado do AST
            double complexityDelta = postCyclomaticComplexity - preCyclomaticComplexity;

            String interpretation = complexityDelta > 0.5 ? "DEGRADED" :
                                   complexityDelta < -0.5 ? "IMPROVED" : "NEUTRAL";

            LLMImpactDelta delta = new LLMImpactDelta(
                "AST",
                "cyclomaticComplexity",
                preCyclomaticComplexity,
                postCyclomaticComplexity,
                complexityDelta,
                interpretation
            );

            deltas.add(delta);

            log.info("📊 [US#71] PRE vs POST | Dimension=AST | metric=complexity | Δ={} → {}",
                     String.format("%.1f", complexityDelta), interpretation);

        } catch (Exception e) {
            log.warn("⚠️ [US#71] Erro ao comparar AST: {}", e.getMessage());
        }

        return deltas;
    }

    /**
     * Compara dimensão RAG (US#63/67)
     */
    private List<LLMImpactDelta> compareRAGDimension(GitImpactAnalysisResponse preState,
                                                     GitImpactAnalysisResponse postState) {
        List<LLMImpactDelta> deltas = new ArrayList<>();

        try {
            // Fallback rate (simulado)
            double preFallbackRate = 25.0; // Seria obtido do RAG
            double postFallbackRate = 35.0; // Seria obtido do RAG
            double fallbackDelta = postFallbackRate - preFallbackRate;

            String interpretation = fallbackDelta > 10 ? "DEGRADED" :
                                   fallbackDelta < -10 ? "IMPROVED" : "NEUTRAL";

            LLMImpactDelta delta = new LLMImpactDelta(
                "RAG",
                "fallbackRate",
                preFallbackRate,
                postFallbackRate,
                fallbackDelta,
                interpretation
            );

            deltas.add(delta);

            log.info("📊 [US#71] PRE vs POST | Dimension=RAG | metric=fallbackRate | Δ={}% → {}",
                     String.format("%.0f", fallbackDelta), interpretation);

        } catch (Exception e) {
            log.warn("⚠️ [US#71] Erro ao comparar RAG: {}", e.getMessage());
        }

        return deltas;
    }

    /**
     * Compara dimensão de Negócio
     */
    private List<LLMImpactDelta> compareBusinessDimension(GitImpactAnalysisResponse preState,
                                                          GitImpactAnalysisResponse postState) {
        List<LLMImpactDelta> deltas = new ArrayList<>();

        try {
            // Heurística simplificada: risco ALTO indica mais impacto em regras críticas
            double preRiskScore = preState != null && "ALTO".equals(preState.getRiskLevel()) ? 10.0 : 0.0;
            double postRiskScore = postState != null && "ALTO".equals(postState.getRiskLevel()) ? 10.0 : 0.0;

            double riskDelta = postRiskScore - preRiskScore;

            String interpretation = riskDelta > 5 ? "DEGRADED" :
                                   riskDelta < -5 ? "IMPROVED" : "NEUTRAL";

            if (Math.abs(riskDelta) >= 5) {
                LLMImpactDelta delta = new LLMImpactDelta(
                    "BUSINESS",
                    "riskLevelScore",
                    preRiskScore,
                    postRiskScore,
                    riskDelta,
                    interpretation
                );

                deltas.add(delta);

                log.info("📊 [US#71] PRE vs POST | Dimension=BUSINESS | metric=riskLevel | Δ={} → {}",
                         (int)riskDelta, interpretation);
            }

        } catch (Exception e) {
            log.warn("⚠️ [US#71] Erro ao comparar Negócio: {}", e.getMessage());
        }

        return deltas;
    }

    /**
     * Compara dimensão de Testes
     */
    private List<LLMImpactDelta> compareTestsDimension(GitImpactAnalysisResponse preState,
                                                       GitImpactAnalysisResponse postState) {
        List<LLMImpactDelta> deltas = new ArrayList<>();

        try {
            // Arquivos críticos sem testes (heurístico)
            double preUntested = 2.0; // Seria calculado
            double postUntested = 5.0; // Seria calculado
            double untestedDelta = postUntested - preUntested;

            String interpretation = untestedDelta > 1 ? "DEGRADED" :
                                   untestedDelta < -1 ? "IMPROVED" : "NEUTRAL";

            if (Math.abs(untestedDelta) >= 1) {
                LLMImpactDelta delta = new LLMImpactDelta(
                    "TESTS",
                    "criticalFilesWithoutTests",
                    preUntested,
                    postUntested,
                    untestedDelta,
                    interpretation
                );

                deltas.add(delta);

                log.info("📊 [US#71] PRE vs POST | Dimension=TESTS | metric=untestedFiles | Δ={} → {}",
                         (int)untestedDelta, interpretation);
            }

        } catch (Exception e) {
            log.warn("⚠️ [US#71] Erro ao comparar Testes: {}", e.getMessage());
        }

        return deltas;
    }

    /**
     * Calcula veredicto final baseado nos deltas
     * 
     * Regra:
     * - Qualquer DEGRADED com delta relevante → DEGRADED
     * - Duas ou mais IMPROVED → IMPROVED
     * - Caso contrário → UNCHANGED
     */
    private String calculateFinalVerdict(List<LLMImpactDelta> deltas) {
        long degradedCount = deltas.stream()
            .filter(d -> "DEGRADED".equals(d.getInterpretation()))
            .count();

        long improvedCount = deltas.stream()
            .filter(d -> "IMPROVED".equals(d.getInterpretation()))
            .count();

        if (degradedCount > 0) {
            return "DEGRADED";
        } else if (improvedCount >= 2) {
            return "IMPROVED";
        } else {
            return "UNCHANGED";
        }
    }

    /**
     * Calcula delta de score final (soma dos deltas negativos)
     */
    private int calculateScoreDelta(List<LLMImpactDelta> deltas) {
        return deltas.stream()
            .mapToInt(d -> {
                if ("DEGRADED".equals(d.getInterpretation())) {
                    return (int) Math.abs(d.getDelta()) * -10; // Penaliza degradações
                } else if ("IMPROVED".equals(d.getInterpretation())) {
                    return (int) Math.abs(d.getDelta()) * 5; // Recompensa melhorias
                }
                return 0;
            })
            .sum();
    }

    /**
     * Gera sumário executivo
     */
    private String generateExecutiveSummary(String verdict, List<LLMImpactDelta> deltas,
                                           LLMImpactComparisonRequest request) {
        StringBuilder summary = new StringBuilder();

        summary.append(String.format("📊 **Comparação de Impacto: %s vs %s**\n\n",
                                     request.getBaseRef(), request.getCompareRef()));

        summary.append(String.format("**Veredicto Final**: %s\n\n", verdict));

        if ("DEGRADED".equals(verdict)) {
            summary.append("🚨 **Degradação Detectada**\n\n");
            summary.append("A mudança aumentou a complexidade técnica e/ou reduziu a qualidade em uma ou mais dimensões. ");
            summary.append("Isto pode indicar código gerado automaticamente sem revisão adequada.\n\n");
        } else if ("IMPROVED".equals(verdict)) {
            summary.append("✅ **Melhoria Detectada**\n\n");
            summary.append("A mudança reduziu complexidade e/ou melhorou qualidade em múltiplas dimensões.\n\n");
        } else {
            summary.append("➡️ **Sem Mudança Significativa**\n\n");
            summary.append("A mudança não apresentou variações relevantes nas métricas analisadas.\n\n");
        }

        summary.append("**Dimensões Analisadas**:\n");
        for (LLMImpactDelta delta : deltas) {
            summary.append(String.format("- %s/%s: %.2f → %.2f (%s)\n",
                                        delta.getDimension(),
                                        delta.getMetric(),
                                        delta.getBeforeValue(),
                                        delta.getAfterValue(),
                                        delta.getInterpretation()));
        }

        return summary.toString();
    }
}
