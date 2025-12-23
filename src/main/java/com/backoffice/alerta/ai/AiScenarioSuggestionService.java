package com.backoffice.alerta.ai;

import com.backoffice.alerta.rules.*;
import com.backoffice.alerta.simulation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de IA para sugerir automaticamente cenários ótimos de decisão
 * 
 * ⚠️ IMPORTANTE: 100% READ-ONLY
 * - NÃO decide automaticamente
 * - NÃO cria auditoria
 * - NÃO persiste dados
 * - NÃO chama notificações
 * - NÃO cria SLA
 * - Apenas simula e recomenda
 * 
 * US#34 - IA sugere automaticamente cenários ótimos de decisão
 */
@Service
public class AiScenarioSuggestionService {

    private static final Logger log = LoggerFactory.getLogger(AiScenarioSuggestionService.class);
    
    private final RiskWhatIfSimulationService simulationService;

    public AiScenarioSuggestionService(RiskWhatIfSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    public AiScenarioSuggestionResponse suggestScenarios(AiScenarioSuggestionRequest request) {
        log.info("🤖 [AI] Gerando sugestões automáticas para PR {}", request.getPullRequestId());

        AiScenarioSuggestionResponse response = new AiScenarioSuggestionResponse();

        // 1. Executar baseline (cenário atual)
        SimulationResult baseline = executeBaseline(request);
        response.setBaseline(createBaselineInfo(baseline));

        // 2. Gerar variações automáticas
        List<ScenarioVariation> variations = generateVariations(request);
        log.info("🤖 [AI] {} variações geradas", variations.size());

        // 3. Simular cada variação
        List<SuggestedScenario> scenarios = new ArrayList<>();
        int scenarioCounter = 1;
        
        for (ScenarioVariation variation : variations) {
            try {
                RiskWhatIfSimulationResponse simResponse = simulateVariation(request, variation);
                SuggestedScenario scenario = createSuggestedScenario(
                    "SC-" + scenarioCounter++,
                    variation,
                    baseline,
                    simResponse
                );
                scenarios.add(scenario);
            } catch (Exception e) {
                log.warn("⚠️ [AI] Erro ao simular variação {}: {}", variation.description, e.getMessage());
            }
        }

        // 4. Avaliar e ordenar por score
        scenarios.forEach(scenario -> scenario.setScore(calculateScore(baseline, scenario)));
        scenarios.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));

        // 5. Retornar apenas top N
        int maxScenarios = request.getMaxScenarios() != null ? request.getMaxScenarios() : 3;
        response.setSuggestedScenarios(
            scenarios.stream()
                .limit(maxScenarios)
                .collect(Collectors.toList())
        );

        log.info("✅ [AI] {} cenários sugeridos para PR {}", 
            response.getSuggestedScenarios().size(), 
            request.getPullRequestId());

        return response;
    }

    private SimulationResult executeBaseline(AiScenarioSuggestionRequest request) {
        // Simular cenário baseline usando mesma lógica do simulador
        RiskWhatIfSimulationRequest simRequest = new RiskWhatIfSimulationRequest();
        simRequest.setPullRequestId(request.getPullRequestId());
        simRequest.setEnvironment(request.getEnvironment());
        simRequest.setChangeType(request.getChangeType());
        simRequest.setBaselineDecision(calculateBaselineDecision(request.getEnvironment(), request.getChangeType()));
        
        WhatIfScenario emptyScenario = new WhatIfScenario();
        simRequest.setWhatIf(emptyScenario);
        
        RiskWhatIfSimulationResponse response = simulationService.simulate(simRequest);
        return response.getBaseline();
    }

    private FinalDecision calculateBaselineDecision(Environment env, ChangeType changeType) {
        // Lógica simplificada de decisão baseline
        if (env == Environment.PRODUCTION && changeType == ChangeType.HOTFIX) {
            return FinalDecision.BLOQUEADO;
        } else if (env == Environment.PRODUCTION) {
            return FinalDecision.APROVADO_COM_RESTRICOES;
        } else {
            return FinalDecision.APROVADO;
        }
    }

    private List<ScenarioVariation> generateVariations(AiScenarioSuggestionRequest request) {
        List<ScenarioVariation> variations = new ArrayList<>();

        // Variação 1: Alterar environment (se PRODUCTION → STAGING)
        if (request.getEnvironment() == Environment.PRODUCTION) {
            ScenarioVariation v = new ScenarioVariation();
            v.description = "Mover deploy para STAGING reduz risco crítico";
            v.overrideEnvironment = Environment.STAGING;
            variations.add(v);
        }

        // Variação 2: Alterar environment (se PRODUCTION → DEV)
        if (request.getEnvironment() == Environment.PRODUCTION) {
            ScenarioVariation v = new ScenarioVariation();
            v.description = "Testar em DEV primeiro elimina riscos de produção";
            v.overrideEnvironment = Environment.DEV;
            variations.add(v);
        }

        // Variação 3: Alterar changeType (se HOTFIX → FEATURE)
        if (request.getChangeType() == ChangeType.HOTFIX) {
            ScenarioVariation v = new ScenarioVariation();
            v.description = "Reclassificar como FEATURE reduz urgência e permite mais revisões";
            v.overrideChangeType = ChangeType.FEATURE;
            variations.add(v);
        }

        // Variação 4: Alterar changeType (se FEATURE → REFACTOR)
        if (request.getChangeType() == ChangeType.FEATURE) {
            ScenarioVariation v = new ScenarioVariation();
            v.description = "Tratar como REFACTOR reduz impacto em regras de negócio";
            v.overrideChangeType = ChangeType.REFACTOR;
            variations.add(v);
        }

        // Variação 5: Excluir arquivos críticos (simular redução de escopo)
        ScenarioVariation v5 = new ScenarioVariation();
        v5.description = "Remover arquivos críticos da análise reduz impacto em regras financeiras";
        v5.excludeFiles = List.of("payment-service.java", "transaction-processor.java", "billing-engine.java");
        variations.add(v5);

        // Variação 6: Simular divisão de PR (menos arquivos)
        ScenarioVariation v6 = new ScenarioVariation();
        v6.description = "Dividir PR em partes menores facilita aprovação e reduz risco";
        v6.excludeFiles = List.of("module-a.java", "module-b.java");
        variations.add(v6);

        // Variação 7: Combinação STAGING + FEATURE
        if (request.getEnvironment() == Environment.PRODUCTION && request.getChangeType() == ChangeType.HOTFIX) {
            ScenarioVariation v7 = new ScenarioVariation();
            v7.description = "STAGING + FEATURE oferece melhor balanço entre segurança e agilidade";
            v7.overrideEnvironment = Environment.STAGING;
            v7.overrideChangeType = ChangeType.FEATURE;
            variations.add(v7);
        }

        return variations;
    }

    private RiskWhatIfSimulationResponse simulateVariation(
        AiScenarioSuggestionRequest request,
        ScenarioVariation variation
    ) {
        RiskWhatIfSimulationRequest simRequest = new RiskWhatIfSimulationRequest();
        simRequest.setPullRequestId(request.getPullRequestId());
        simRequest.setEnvironment(request.getEnvironment());
        simRequest.setChangeType(request.getChangeType());
        simRequest.setBaselineDecision(calculateBaselineDecision(request.getEnvironment(), request.getChangeType()));

        WhatIfScenario whatIf = new WhatIfScenario();
        whatIf.setOverrideEnvironment(variation.overrideEnvironment);
        whatIf.setOverrideChangeType(variation.overrideChangeType);
        whatIf.setExcludeFiles(variation.excludeFiles);
        simRequest.setWhatIf(whatIf);

        return simulationService.simulate(simRequest);
    }

    private SuggestedScenario createSuggestedScenario(
        String scenarioId,
        ScenarioVariation variation,
        SimulationResult baseline,
        RiskWhatIfSimulationResponse simResponse
    ) {
        SuggestedScenario scenario = new SuggestedScenario();
        scenario.setScenarioId(scenarioId);
        scenario.setDescription(variation.description);
        
        SimulationResult simResult = simResponse.getSimulation();
        scenario.setRiskLevel(simResult.getRiskLevel());
        scenario.setDecision(simResult.getFinalDecision());
        scenario.setSlaRemoved(baseline.isSlaTriggered() && !simResult.isSlaTriggered());
        scenario.setTeamsNotified(simResult.getNotifiedTeams());
        
        // Explicação detalhada
        scenario.setExplanation(generateExplanation(baseline, simResult, simResponse.getDelta()));
        
        return scenario;
    }

    private int calculateScore(SimulationResult baseline, SuggestedScenario scenario) {
        int score = 0;

        // Peso 1: Redução de risco (0-40 pontos)
        score += calculateRiskReductionScore(baseline.getRiskLevel(), scenario.getRiskLevel());

        // Peso 2: Remoção de SLA (0-30 pontos)
        if (scenario.isSlaRemoved()) {
            score += 30;
        }

        // Peso 3: Menos times notificados (0-15 pontos)
        int baselineTeams = baseline.getNotifiedTeams() != null ? baseline.getNotifiedTeams().size() : 0;
        int scenarioTeams = scenario.getTeamsNotified() != null ? scenario.getTeamsNotified().size() : 0;
        if (scenarioTeams < baselineTeams) {
            score += 15;
        }

        // Peso 4: Melhoria de decisão (0-15 pontos)
        if (isDecisionImproved(baseline.getFinalDecision(), scenario.getDecision())) {
            score += 15;
        }

        return Math.min(100, score);
    }

    private int calculateRiskReductionScore(RiskLevel baseline, RiskLevel scenario) {
        int baselineLevel = getRiskLevelNumeric(baseline);
        int scenarioLevel = getRiskLevelNumeric(scenario);
        int reduction = baselineLevel - scenarioLevel;

        if (reduction == 3) return 40; // CRÍTICO → BAIXO
        if (reduction == 2) return 30; // CRÍTICO → MÉDIO ou ALTO → BAIXO
        if (reduction == 1) return 20; // Redução de 1 nível
        return 0;
    }

    private int getRiskLevelNumeric(RiskLevel level) {
        if (level == RiskLevel.CRITICO) return 3;
        if (level == RiskLevel.ALTO) return 2;
        if (level == RiskLevel.MEDIO) return 1;
        if (level == RiskLevel.BAIXO) return 0;
        return 0;
    }

    private boolean isDecisionImproved(FinalDecision baseline, FinalDecision scenario) {
        int baselineLevel = getDecisionLevel(baseline);
        int scenarioLevel = getDecisionLevel(scenario);
        return scenarioLevel > baselineLevel;
    }

    private int getDecisionLevel(FinalDecision decision) {
        if (decision == FinalDecision.BLOQUEADO) return 0;
        if (decision == FinalDecision.APROVADO_COM_RESTRICOES) return 2;
        if (decision == FinalDecision.APROVADO) return 3;
        return 0;
    }

    private String generateExplanation(SimulationResult baseline, SimulationResult simulated, SimulationDelta delta) {
        StringBuilder explanation = new StringBuilder();

        // Redução de risco
        if (!delta.getRiskReduction().contains("→")) {
            explanation.append("Mantém nível de risco estável. ");
        } else {
            explanation.append(String.format("Reduz risco de %s. ", delta.getRiskReduction()));
        }

        // SLA
        if (!delta.getSlaImpact().equals("SEM ALTERAÇÃO")) {
            explanation.append(delta.getSlaImpact()).append(". ");
        }

        // Regras
        if (!delta.getRulesNoLongerImpacted().isEmpty()) {
            explanation.append(String.format("Remove impacto de %d regras críticas. ", 
                delta.getRulesNoLongerImpacted().size()));
        }

        // Times
        int baselineTeams = baseline.getNotifiedTeams() != null ? baseline.getNotifiedTeams().size() : 0;
        int simulatedTeams = simulated.getNotifiedTeams() != null ? simulated.getNotifiedTeams().size() : 0;
        if (simulatedTeams < baselineTeams) {
            explanation.append(String.format("Reduz notificações de %d para %d times. ", 
                baselineTeams, simulatedTeams));
        }

        // Restrições
        int baselineRestrictions = baseline.getRestrictions() != null ? baseline.getRestrictions().size() : 0;
        int simulatedRestrictions = simulated.getRestrictions() != null ? simulated.getRestrictions().size() : 0;
        if (simulatedRestrictions < baselineRestrictions) {
            explanation.append(String.format("Remove %d restrições operacionais. ", 
                baselineRestrictions - simulatedRestrictions));
        }

        return explanation.toString().trim();
    }

    private AiScenarioSuggestionResponse.BaselineInfo createBaselineInfo(SimulationResult baseline) {
        AiScenarioSuggestionResponse.BaselineInfo info = new AiScenarioSuggestionResponse.BaselineInfo();
        info.setRiskLevel(baseline.getRiskLevel().toString());
        info.setDecision(baseline.getFinalDecision().toString());
        return info;
    }

    /**
     * Classe interna para representar uma variação de cenário
     */
    private static class ScenarioVariation {
        String description;
        Environment overrideEnvironment;
        ChangeType overrideChangeType;
        List<String> excludeFiles;
    }
}
