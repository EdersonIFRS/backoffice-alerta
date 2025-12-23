package com.backoffice.alerta.simulation;

import com.backoffice.alerta.rules.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service para simulação "What-If" de decisões de risco
 * 
 * ⚠️ IMPORTANTE: 100% READ-ONLY
 * - NÃO cria auditoria
 * - NÃO persiste dados
 * - NÃO chama notificações
 * - NÃO cria SLA
 * - Apenas simulação em memória
 * 
 * US#33 - Simulação Executiva de Decisão de Risco
 */
@Service
public class RiskWhatIfSimulationService {

    public RiskWhatIfSimulationResponse simulate(RiskWhatIfSimulationRequest request) {
        RiskWhatIfSimulationResponse response = new RiskWhatIfSimulationResponse();
        response.setPullRequestId(request.getPullRequestId());

        // 1. Executar baseline (cenário atual)
        SimulationResult baseline = executeBaseline(request);
        response.setBaseline(baseline);

        // 2. Executar simulação (com what-if)
        SimulationResult simulation = executeSimulation(request);
        response.setSimulation(simulation);

        // 3. Calcular delta
        SimulationDelta delta = calculateDelta(baseline, simulation);
        response.setDelta(delta);

        // 4. Gerar recomendação executiva
        ExecutiveRecommendation recommendation = generateRecommendation(baseline, simulation, delta);
        response.setExecutiveRecommendation(recommendation);

        return response;
    }

    private SimulationResult executeBaseline(RiskWhatIfSimulationRequest request) {
        SimulationResult result = new SimulationResult();
        
        // Simula cenário baseline
        result.setFinalDecision(request.getBaselineDecision());
        result.setRiskLevel(calculateRiskLevel(request.getEnvironment(), request.getChangeType()));
        result.setImpactedRules(5); // Simulado
        result.setSlaTriggered(result.getRiskLevel() == RiskLevel.CRITICO || result.getRiskLevel() == RiskLevel.ALTO);
        result.setNotifiedTeams(List.of("Platform Team", "Security Team"));
        
        if (result.getRiskLevel() == RiskLevel.CRITICO) {
            result.setRestrictions(List.of("Requer aprovação VP", "Deploy apenas em horário comercial"));
        }
        
        return result;
    }

    private SimulationResult executeSimulation(RiskWhatIfSimulationRequest request) {
        SimulationResult result = new SimulationResult();
        
        WhatIfScenario whatIf = request.getWhatIf();
        
        // Aplicar overrides
        Environment env = whatIf.getOverrideEnvironment() != null ? 
            whatIf.getOverrideEnvironment() : request.getEnvironment();
        ChangeType changeType = whatIf.getOverrideChangeType() != null ? 
            whatIf.getOverrideChangeType() : request.getChangeType();
        
        // Calcular novo risco
        RiskLevel newRiskLevel = calculateRiskLevel(env, changeType);
        
        // Ajustar impacto se arquivos foram excluídos
        int impactedRules = 5;
        if (whatIf.getExcludeFiles() != null && !whatIf.getExcludeFiles().isEmpty()) {
            impactedRules = Math.max(1, impactedRules - whatIf.getExcludeFiles().size());
            // Reduzir risco se muitos arquivos foram excluídos
            if (whatIf.getExcludeFiles().size() >= 3) {
                newRiskLevel = reduceRiskLevel(newRiskLevel);
            }
        }
        
        result.setRiskLevel(newRiskLevel);
        result.setImpactedRules(impactedRules);
        result.setFinalDecision(calculateDecision(newRiskLevel, env));
        result.setSlaTriggered(newRiskLevel == RiskLevel.CRITICO || newRiskLevel == RiskLevel.ALTO);
        
        // Notificações reduzidas se risco menor
        List<String> teams = new ArrayList<>();
        if (newRiskLevel == RiskLevel.CRITICO) {
            teams.add("Platform Team");
            teams.add("Security Team");
        } else if (newRiskLevel == RiskLevel.ALTO) {
            teams.add("Platform Team");
        }
        result.setNotifiedTeams(teams);
        
        // Restrições
        List<String> restrictions = new ArrayList<>();
        if (newRiskLevel == RiskLevel.CRITICO) {
            restrictions.add("Requer aprovação VP");
            restrictions.add("Deploy apenas em horário comercial");
        } else if (newRiskLevel == RiskLevel.ALTO) {
            restrictions.add("Requer peer review adicional");
        }
        result.setRestrictions(restrictions);
        
        return result;
    }

    private SimulationDelta calculateDelta(SimulationResult baseline, SimulationResult simulation) {
        SimulationDelta delta = new SimulationDelta();
        
        // Redução de risco
        if (baseline.getRiskLevel() != simulation.getRiskLevel()) {
            delta.setRiskReduction(baseline.getRiskLevel().name() + " → " + simulation.getRiskLevel().name());
        } else {
            delta.setRiskReduction("Sem mudança");
        }
        
        // Regras não mais impactadas
        int rulesReduced = baseline.getImpactedRules() - simulation.getImpactedRules();
        if (rulesReduced > 0) {
            delta.setRulesNoLongerImpacted(List.of(rulesReduced + " regra(s) não mais impactada(s)"));
        } else {
            delta.setRulesNoLongerImpacted(List.of());
        }
        
        // Impacto SLA
        if (baseline.isSlaTriggered() && !simulation.isSlaTriggered()) {
            delta.setSlaImpact("SLA não mais necessário");
        } else if (!baseline.isSlaTriggered() && simulation.isSlaTriggered()) {
            delta.setSlaImpact("SLA agora necessário");
        } else {
            delta.setSlaImpact("Sem impacto em SLA");
        }
        
        return delta;
    }

    private ExecutiveRecommendation generateRecommendation(SimulationResult baseline, 
                                                          SimulationResult simulation, 
                                                          SimulationDelta delta) {
        ExecutiveRecommendation rec = new ExecutiveRecommendation();
        
        // Comparar níveis de risco
        int baselineOrdinal = baseline.getRiskLevel().ordinal();
        int simulationOrdinal = simulation.getRiskLevel().ordinal();
        
        if (simulationOrdinal < baselineOrdinal) {
            // Risco reduziu
            rec.setConfidence("ALTA");
            rec.setHeadline("Redução significativa de risco");
            rec.setSummary("A mudança proposta reduz o nível de risco de " + 
                          baseline.getRiskLevel().name() + " para " + 
                          simulation.getRiskLevel().name() + ". Recomenda-se considerar esta alternativa.");
        } else if (simulationOrdinal > baselineOrdinal) {
            // Risco aumentou
            rec.setConfidence("BAIXA");
            rec.setHeadline("Aumento de risco detectado");
            rec.setSummary("A mudança proposta aumenta o nível de risco de " + 
                          baseline.getRiskLevel().name() + " para " + 
                          simulation.getRiskLevel().name() + ". Não recomendada.");
        } else {
            // Risco igual
            rec.setConfidence("MEDIA");
            rec.setHeadline("Risco mantido");
            rec.setSummary("A mudança proposta mantém o mesmo nível de risco (" + 
                          baseline.getRiskLevel().name() + "). Considere outros fatores para decisão.");
        }
        
        return rec;
    }

    private RiskLevel calculateRiskLevel(Environment env, ChangeType changeType) {
        // Lógica simplificada
        if (env == Environment.PRODUCTION) {
            return changeType == ChangeType.HOTFIX ? RiskLevel.CRITICO : RiskLevel.ALTO;
        } else if (env == Environment.STAGING) {
            return RiskLevel.MEDIO;
        } else {
            return RiskLevel.BAIXO;
        }
    }

    private RiskLevel reduceRiskLevel(RiskLevel current) {
        switch (current) {
            case CRITICO:
                return RiskLevel.ALTO;
            case ALTO:
                return RiskLevel.MEDIO;
            case MEDIO:
                return RiskLevel.BAIXO;
            default:
                return RiskLevel.BAIXO;
        }
    }

    private FinalDecision calculateDecision(RiskLevel riskLevel, Environment env) {
        if (riskLevel == RiskLevel.CRITICO && env == Environment.PRODUCTION) {
            return FinalDecision.BLOQUEADO;
        } else if (riskLevel == RiskLevel.ALTO) {
            return FinalDecision.APROVADO_COM_RESTRICOES;
        } else {
            return FinalDecision.APROVADO;
        }
    }
}
