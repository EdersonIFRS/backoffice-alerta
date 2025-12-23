package com.backoffice.alerta.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Implementação simulada de cliente LLM para testes e desenvolvimento
 * NÃO faz chamadas reais a APIs externas
 */
@Component
public class DummyLLMClient implements LLMClient {

    @Override
    public String generateAdvisory(Map<String, Object> context) {
        // Simula processamento de IA baseado no contexto
        StringBuilder advisory = new StringBuilder();

        // Extrai dados do contexto
        String riskLevel = context.getOrDefault("riskLevel", "DESCONHECIDO").toString();
        String finalDecision = context.getOrDefault("finalDecision", "DESCONHECIDA").toString();
        String environment = context.getOrDefault("environment", "DESCONHECIDO").toString();
        String changeType = context.getOrDefault("changeType", "DESCONHECIDO").toString();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rules = (List<Map<String, Object>>) 
            context.getOrDefault("impactedBusinessRules", List.of());
        
        @SuppressWarnings("unchecked")
        List<String> actions = (List<String>) 
            context.getOrDefault("mandatoryActions", List.of());

        // Gera visão executiva
        advisory.append("VISÃO EXECUTIVA:\n");
        if ("ALTO".equals(riskLevel) || "CRITICO".equals(riskLevel)) {
            advisory.append("Esta mudança apresenta risco elevado que requer atenção especial da liderança. ");
        } else if ("MEDIO".equals(riskLevel)) {
            advisory.append("Esta mudança apresenta risco moderado e deve seguir o processo padrão de aprovação. ");
        } else {
            advisory.append("Esta mudança apresenta baixo risco e pode prosseguir com revisão padrão. ");
        }

        if ("PRODUCTION".equals(environment)) {
            advisory.append("O deploy será realizado em PRODUÇÃO, aumentando a necessidade de cautela.");
        }

        advisory.append("\n\n");

        // Interpretação de risco
        advisory.append("INTERPRETAÇÃO DE RISCO:\n");
        advisory.append(String.format("O sistema classificou esta mudança como '%s' baseado em análise técnica e histórica. ", 
            riskLevel));
        
        if (!rules.isEmpty()) {
            advisory.append(String.format("A mudança impacta %d regra(s) de negócio catalogada(s), ", rules.size()));
            
            long criticalRules = rules.stream()
                .filter(r -> "CRITICA".equals(r.get("criticality")))
                .count();
            
            if (criticalRules > 0) {
                advisory.append(String.format("sendo %d crítica(s). ", criticalRules));
            }
        } else {
            advisory.append("A mudança não impacta diretamente regras de negócio catalogadas. ");
        }

        advisory.append("\n\n");

        // Alerta histórico
        advisory.append("PADRÃO HISTÓRICO:\n");
        
        boolean hasIncidents = rules.stream()
            .anyMatch(r -> {
                Object count = r.get("incidentCount");
                return count != null && (int)count > 0;
            });

        if (hasIncidents) {
            advisory.append("ATENÇÃO: Foram identificados incidentes históricos nas áreas impactadas. ");
            
            int totalIncidents = rules.stream()
                .mapToInt(r -> {
                    Object count = r.get("incidentCount");
                    return count != null ? (int)count : 0;
                })
                .sum();
            
            advisory.append(String.format("Total de %d incidente(s) registrado(s). ", totalIncidents));
            advisory.append("Recomenda-se revisar as lições aprendidas destes incidentes antes do deploy.");
        } else {
            advisory.append("Não foram identificados incidentes históricos nas áreas impactadas. ");
            advisory.append("Este é um indicador positivo, mas não elimina a necessidade de cautela.");
        }

        advisory.append("\n\n");

        // Recomendações preventivas
        advisory.append("RECOMENDAÇÕES PREVENTIVAS:\n");
        
        if ("HOTFIX".equals(changeType)) {
            advisory.append("- Hotfix urgente: priorize testes de regressão nas funcionalidades relacionadas\n");
        }
        
        if ("PRODUCTION".equals(environment) && ("ALTO".equals(riskLevel) || "CRITICO".equals(riskLevel))) {
            advisory.append("- Considere deploy gradual (canary) para mitigar riscos\n");
            advisory.append("- Mantenha equipe de plantão durante e após o deploy\n");
            advisory.append("- Configure alertas para métricas críticas de negócio\n");
        }
        
        if (!actions.isEmpty()) {
            advisory.append("- Execute rigorosamente todas as ações obrigatórias definidas\n");
        }
        
        advisory.append("- Documente decisões e mantenha comunicação com stakeholders\n");
        advisory.append("- Tenha plano de rollback validado e pronto para execução\n");

        advisory.append("\n");
        advisory.append("NÍVEL DE CONFIANÇA: Alta (baseado em dados históricos e regras estruturadas)");

        return advisory.toString();
    }
}
