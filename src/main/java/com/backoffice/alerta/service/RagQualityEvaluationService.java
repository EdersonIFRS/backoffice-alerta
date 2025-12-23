package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.*;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.rag.RagQueryResponse;
import com.backoffice.alerta.rag.RagRuleScoreDetail;
import com.backoffice.alerta.rag.RagMatchType;
import com.backoffice.alerta.rag.ConfidenceLevel;
import com.backoffice.alerta.rag.BusinessRuleRagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * US#67 - Serviço de avaliação de qualidade do RAG.
 * 
 * Responsabilidades:
 * - Calcular métricas de qualidade (fallbackRate, avgSemanticScore, etc.)
 * - Suportar contexto GLOBAL e SCOPED (por projeto)
 * - Identificar regras problemáticas
 * - Gerar explicações determinísticas
 * 
 * Princípios:
 * - READ-ONLY absoluto
 * - Determinístico
 * - Sem IA / ML / LLM
 * - Sem persistência
 * - Try-catch em todos métodos públicos
 * - Nunca propagar exceções
 */
@Service
public class RagQualityEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(RagQualityEvaluationService.class);

    private final BusinessRuleRagService ragService;
    private final ProjectRepository projectRepository;

    // Cache simulado de queries recentes (em produção viria de logs ou analytics)
    private final List<RagQueryResponse> recentQueries = new ArrayList<>();

    public RagQualityEvaluationService(BusinessRuleRagService ragService,
                                       ProjectRepository projectRepository) {
        this.ragService = ragService;
        this.projectRepository = projectRepository;
        initializeMockQueries();
    }

    /**
     * Obter métricas de qualidade GLOBAL ou SCOPED.
     */
    public RagQualityMetricsResponse getQualityMetrics(UUID projectId) {
        try {
            log.info("📊 [US#67] Calculando métricas de qualidade | projectId={}", 
                    projectId != null ? projectId : "GLOBAL");

            List<RagQueryResponse> queries = filterQueriesByProject(projectId);

            if (queries.isEmpty()) {
                log.warn("⚠️ [US#67] Nenhuma query encontrada para avaliação");
                return createEmptyMetrics(projectId);
            }

            RagQualityMetricsResponse metrics = new RagQualityMetricsResponse();
            metrics.setProjectContext(projectId);
            metrics.setTotalQueriesEvaluated(queries.size());

            // Contar regras únicas
            Set<String> uniqueRules = queries.stream()
                    .flatMap(q -> q.getRuleScores().stream())
                    .map(RagRuleScoreDetail::getBusinessRuleId)
                    .collect(Collectors.toSet());
            metrics.setTotalRulesEvaluated(uniqueRules.size());

            // Calcular métricas
            metrics.setFallbackRate(calculateFallbackRate(queries));
            metrics.setAvgSemanticScore(calculateAvgSemanticScore(queries));
            metrics.setAvgKeywordScore(calculateAvgKeywordScore(queries));
            metrics.setHybridMatchRate(calculateHybridMatchRate(queries));
            metrics.setSemanticOnlyRate(calculateSemanticOnlyRate(queries));
            metrics.setKeywordOnlyRate(calculateKeywordOnlyRate(queries));
            metrics.setFallbackInclusionRate(calculateFallbackInclusionRate(queries));
            metrics.setConfidenceMismatchRate(calculateConfidenceMismatchRate(queries));

            log.info("✅ [US#67] Métricas calculadas | fallbackRate={} | avgSemantic={}", 
                    metrics.getFallbackRate(), metrics.getAvgSemanticScore());

            return metrics;

        } catch (Exception e) {
            log.error("❌ [US#67] Erro ao calcular métricas de qualidade", e);
            return createEmptyMetrics(projectId);
        }
    }

    /**
     * Obter métricas por projeto.
     */
    public List<RagProjectQualityResponse> getQualityByProject() {
        try {
            log.info("📊 [US#67] Calculando métricas por projeto");

            List<Project> projects = projectRepository.findAll();
            List<RagProjectQualityResponse> result = new ArrayList<>();

            for (Project project : projects) {
                RagProjectQualityResponse projectQuality = new RagProjectQualityResponse();
                projectQuality.setProjectId(project.getId());
                projectQuality.setProjectName(project.getName());
                projectQuality.setMetrics(getQualityMetrics(project.getId()));
                result.add(projectQuality);
            }

            log.info("✅ [US#67] Métricas calculadas para {} projetos", result.size());
            return result;

        } catch (Exception e) {
            log.error("❌ [US#67] Erro ao calcular métricas por projeto", e);
            return new ArrayList<>();
        }
    }

    /**
     * Obter qualidade por regra de negócio.
     */
    public List<RagRuleQualityResponse> getQualityByRule(UUID projectId) {
        try {
            log.info("📊 [US#67] Calculando qualidade por regra | projectId={}", 
                    projectId != null ? projectId : "GLOBAL");

            List<RagQueryResponse> queries = filterQueriesByProject(projectId);

            // Agrupar scores por regra
            Map<String, List<RagRuleScoreDetail>> scoresByRule = new HashMap<>();
            for (RagQueryResponse query : queries) {
                for (RagRuleScoreDetail score : query.getRuleScores()) {
                    scoresByRule.computeIfAbsent(score.getBusinessRuleId(), k -> new ArrayList<>())
                            .add(score);
                }
            }

            // Calcular métricas por regra
            List<RagRuleQualityResponse> result = new ArrayList<>();
            for (Map.Entry<String, List<RagRuleScoreDetail>> entry : scoresByRule.entrySet()) {
                RagRuleQualityResponse ruleQuality = calculateRuleQuality(entry.getKey(), entry.getValue());
                result.add(ruleQuality);
            }

            // Ordenar por avgSemanticScore decrescente
            result.sort((a, b) -> Double.compare(
                    b.getAvgSemanticScore() != null ? b.getAvgSemanticScore() : 0.0,
                    a.getAvgSemanticScore() != null ? a.getAvgSemanticScore() : 0.0
            ));

            log.info("✅ [US#67] Qualidade calculada para {} regras", result.size());
            return result;

        } catch (Exception e) {
            log.error("❌ [US#67] Erro ao calcular qualidade por regra", e);
            return new ArrayList<>();
        }
    }

    /**
     * Obter tendências de qualidade ao longo do tempo.
     */
    public List<RagQualityTrendPoint> getQualityTrends(UUID projectId, LocalDate fromDate, LocalDate toDate) {
        try {
            log.info("📊 [US#67] Calculando tendências | projectId={} | from={} | to={}", 
                    projectId != null ? projectId : "GLOBAL", fromDate, toDate);

            // Em produção, isso viria de logs agregados ou analytics
            // Para esta US, simulamos com dados mock
            List<RagQualityTrendPoint> trends = new ArrayList<>();

            LocalDate currentDate = fromDate != null ? fromDate : LocalDate.now().minusDays(7);
            LocalDate endDate = toDate != null ? toDate : LocalDate.now();

            while (!currentDate.isAfter(endDate)) {
                RagQualityTrendPoint point = new RagQualityTrendPoint();
                point.setDate(currentDate);
                
                // Simular variação (em produção viriam de dados reais)
                point.setFallbackRate(0.10 + Math.random() * 0.15);
                point.setAvgSemanticScore(0.60 + Math.random() * 0.25);
                point.setQueryCount((int) (20 + Math.random() * 30));
                
                trends.add(point);
                currentDate = currentDate.plusDays(1);
            }

            log.info("✅ [US#67] {} pontos de tendência gerados", trends.size());
            return trends;

        } catch (Exception e) {
            log.error("❌ [US#67] Erro ao calcular tendências", e);
            return new ArrayList<>();
        }
    }

    /**
     * Health check do serviço de qualidade.
     */
    public Map<String, Object> getHealthStatus() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "RagQualityEvaluationService");
            health.put("queriesAvailable", recentQueries.size());
            health.put("timestamp", LocalDate.now());
            return health;
        } catch (Exception e) {
            log.error("❌ [US#67] Erro no health check", e);
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return health;
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private List<RagQueryResponse> filterQueriesByProject(UUID projectId) {
        if (projectId == null) {
            return recentQueries; // GLOBAL
        }
        return recentQueries.stream()
                .filter(q -> q.getProjectContext() != null && 
                             q.getProjectContext().isScoped() &&
                             projectId.equals(q.getProjectContext().getProjectId()))
                .collect(Collectors.toList());
    }

    private Double calculateFallbackRate(List<RagQueryResponse> queries) {
        if (queries.isEmpty()) return 0.0;
        long fallbackCount = queries.stream()
                .filter(RagQueryResponse::isUsedFallback)
                .count();
        return (double) fallbackCount / queries.size();
    }

    private Double calculateAvgSemanticScore(List<RagQueryResponse> queries) {
        List<Double> semanticScores = queries.stream()
                .flatMap(q -> q.getRuleScores().stream())
                .map(RagRuleScoreDetail::getSemanticScore)
                .filter(score -> score != null && score > 0.0)
                .collect(Collectors.toList());

        if (semanticScores.isEmpty()) return 0.0;
        return semanticScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private Double calculateAvgKeywordScore(List<RagQueryResponse> queries) {
        List<Integer> keywordScores = queries.stream()
                .flatMap(q -> q.getRuleScores().stream())
                .map(RagRuleScoreDetail::getKeywordScore)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (keywordScores.isEmpty()) return 0.0;
        return keywordScores.stream()
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);
    }

    private Double calculateHybridMatchRate(List<RagQueryResponse> queries) {
        if (queries.isEmpty()) return 0.0;
        long hybridCount = queries.stream()
                .flatMap(q -> q.getRuleScores().stream())
                .filter(s -> s.getMatchType() == RagMatchType.HYBRID)
                .count();
        long totalScores = queries.stream()
                .mapToLong(q -> q.getRuleScores().size())
                .sum();
        return totalScores > 0 ? (double) hybridCount / totalScores : 0.0;
    }

    private Double calculateSemanticOnlyRate(List<RagQueryResponse> queries) {
        if (queries.isEmpty()) return 0.0;
        long semanticCount = queries.stream()
                .flatMap(q -> q.getRuleScores().stream())
                .filter(s -> s.getMatchType() == RagMatchType.SEMANTIC)
                .count();
        long totalScores = queries.stream()
                .mapToLong(q -> q.getRuleScores().size())
                .sum();
        return totalScores > 0 ? (double) semanticCount / totalScores : 0.0;
    }

    private Double calculateKeywordOnlyRate(List<RagQueryResponse> queries) {
        if (queries.isEmpty()) return 0.0;
        long keywordCount = queries.stream()
                .flatMap(q -> q.getRuleScores().stream())
                .filter(s -> s.getMatchType() == RagMatchType.KEYWORD)
                .count();
        long totalScores = queries.stream()
                .mapToLong(q -> q.getRuleScores().size())
                .sum();
        return totalScores > 0 ? (double) keywordCount / totalScores : 0.0;
    }

    private Double calculateFallbackInclusionRate(List<RagQueryResponse> queries) {
        if (queries.isEmpty()) return 0.0;
        long fallbackIncludedCount = queries.stream()
                .flatMap(q -> q.getRuleScores().stream())
                .filter(RagRuleScoreDetail::isIncludedByFallback)
                .count();
        long totalScores = queries.stream()
                .mapToLong(q -> q.getRuleScores().size())
                .sum();
        return totalScores > 0 ? (double) fallbackIncludedCount / totalScores : 0.0;
    }

    private Double calculateConfidenceMismatchRate(List<RagQueryResponse> queries) {
        if (queries.isEmpty()) return 0.0;
        
        long mismatchCount = 0;
        long highConfidenceCount = 0;

        for (RagQueryResponse query : queries) {
            if (ConfidenceLevel.HIGH.equals(query.getConfidence())) {
                highConfidenceCount++;
                
                // Calcular média semântica desta query
                double avgSemantic = query.getRuleScores().stream()
                        .map(RagRuleScoreDetail::getSemanticScore)
                        .filter(s -> s != null && s > 0.0)
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);

                if (avgSemantic < 0.4) {
                    mismatchCount++;
                }
            }
        }

        return highConfidenceCount > 0 ? (double) mismatchCount / highConfidenceCount : 0.0;
    }

    private RagRuleQualityResponse calculateRuleQuality(String ruleId, List<RagRuleScoreDetail> scores) {
        RagRuleQualityResponse quality = new RagRuleQualityResponse();
        quality.setBusinessRuleId(UUID.fromString(ruleId));
        quality.setBusinessRuleName(scores.get(0).getBusinessRuleName());
        quality.setOccurrences(scores.size());

        // Avg Semantic Score
        double avgSemantic = scores.stream()
                .map(RagRuleScoreDetail::getSemanticScore)
                .filter(s -> s != null && s > 0.0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        quality.setAvgSemanticScore(avgSemantic);

        // Keyword Dependency Rate
        long keywordOnlyCount = scores.stream()
                .filter(s -> s.getMatchType() == RagMatchType.KEYWORD)
                .count();
        quality.setKeywordDependencyRate((double) keywordOnlyCount / scores.size());

        // Fallback Inclusion Rate
        long fallbackCount = scores.stream()
                .filter(RagRuleScoreDetail::isIncludedByFallback)
                .count();
        quality.setFallbackInclusionRate((double) fallbackCount / scores.size());

        // Observações
        quality.setObservations(generateObservations(quality));

        return quality;
    }

    private String generateObservations(RagRuleQualityResponse quality) {
        StringBuilder obs = new StringBuilder();

        if (quality.getAvgSemanticScore() != null) {
            if (quality.getAvgSemanticScore() >= 0.7) {
                obs.append("Excelente performance semântica. ");
            } else if (quality.getAvgSemanticScore() >= 0.5) {
                obs.append("Performance semântica adequada. ");
            } else if (quality.getAvgSemanticScore() >= 0.3) {
                obs.append("Performance semântica baixa, considerar ajuste de embeddings. ");
            } else {
                obs.append("Performance semântica crítica, requer revisão. ");
            }
        }

        if (quality.getKeywordDependencyRate() != null && quality.getKeywordDependencyRate() > 0.5) {
            obs.append("Alta dependência de keywords. ");
        }

        if (quality.getFallbackInclusionRate() != null && quality.getFallbackInclusionRate() > 0.2) {
            obs.append("Inclusão frequente via fallback. ");
        }

        if (obs.length() == 0) {
            obs.append("Sem observações significativas.");
        }

        return obs.toString().trim();
    }

    private RagQualityMetricsResponse createEmptyMetrics(UUID projectId) {
        RagQualityMetricsResponse metrics = new RagQualityMetricsResponse();
        metrics.setProjectContext(projectId);
        metrics.setTotalQueriesEvaluated(0);
        metrics.setTotalRulesEvaluated(0);
        metrics.setFallbackRate(0.0);
        metrics.setAvgSemanticScore(0.0);
        metrics.setAvgKeywordScore(0.0);
        metrics.setHybridMatchRate(0.0);
        metrics.setSemanticOnlyRate(0.0);
        metrics.setKeywordOnlyRate(0.0);
        metrics.setFallbackInclusionRate(0.0);
        metrics.setConfidenceMismatchRate(0.0);
        return metrics;
    }

    /**
     * Mock de queries recentes para demonstração.
     * Em produção, isso viria de logs, analytics ou cache de queries reais.
     */
    private void initializeMockQueries() {
        // Simular algumas queries para demonstração
        UUID project1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        String rule1 = "550e8400-e29b-41d4-a716-446655440001";
        String rule2 = "550e8400-e29b-41d4-a716-446655440003";

        // Query 1: HYBRID, sem fallback
        RagQueryResponse query1 = new RagQueryResponse();
        ProjectContext ctx1 = new ProjectContext(true);
        ctx1.setProjectId(project1);
        ctx1.setProjectName("Backoffice Pagamentos");
        query1.setProjectContext(ctx1);
        query1.setUsedFallback(false);
        query1.setConfidence(ConfidenceLevel.HIGH);
        
        RagRuleScoreDetail score1 = new RagRuleScoreDetail();
        score1.setBusinessRuleId(rule1);
        score1.setBusinessRuleName("REGRA_CALCULO_HORAS_PJ");
        score1.setSemanticScore(0.78);
        score1.setKeywordScore(2);
        score1.setMatchType(RagMatchType.HYBRID);
        score1.setFinalRankPosition(1);
        score1.setIncludedByFallback(false);
        
        query1.setRuleScores(List.of(score1));
        recentQueries.add(query1);

        // Query 2: SEMANTIC only
        RagQueryResponse query2 = new RagQueryResponse();
        ProjectContext ctx2 = new ProjectContext(true);
        ctx2.setProjectId(project1);
        ctx2.setProjectName("Backoffice Pagamentos");
        query2.setProjectContext(ctx2);
        query2.setUsedFallback(false);
        query2.setConfidence(ConfidenceLevel.MEDIUM);
        
        RagRuleScoreDetail score2 = new RagRuleScoreDetail();
        score2.setBusinessRuleId(rule2);
        score2.setBusinessRuleName("REGRA_CALCULO_TRIBUTOS");
        score2.setSemanticScore(0.65);
        score2.setKeywordScore(0);
        score2.setMatchType(RagMatchType.SEMANTIC);
        score2.setFinalRankPosition(1);
        score2.setIncludedByFallback(false);
        
        query2.setRuleScores(List.of(score2));
        recentQueries.add(query2);

        // Query 3: FALLBACK
        RagQueryResponse query3 = new RagQueryResponse();
        query3.setProjectContext(null); // GLOBAL
        query3.setUsedFallback(true);
        query3.setConfidence(ConfidenceLevel.LOW);
        
        RagRuleScoreDetail score3 = new RagRuleScoreDetail();
        score3.setBusinessRuleId(rule1);
        score3.setBusinessRuleName("REGRA_CALCULO_HORAS_PJ");
        score3.setSemanticScore(0.0);
        score3.setKeywordScore(0);
        score3.setMatchType(RagMatchType.FALLBACK);
        score3.setFinalRankPosition(1);
        score3.setIncludedByFallback(true);
        
        query3.setRuleScores(List.of(score3));
        recentQueries.add(query3);

        log.info("✅ [US#67] Mock de {} queries inicializadas", recentQueries.size());
    }
}
