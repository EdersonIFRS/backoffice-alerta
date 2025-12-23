package com.backoffice.alerta.rag;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

/**
 * Implementação simulada do cliente LLM
 * NÃO faz chamadas externas, apenas simula respostas coerentes com o contexto
 * 
 * Ativo quando rag.llm.provider=DUMMY (padrão)
 */
@Component
@ConditionalOnProperty(name = "rag.llm.provider", havingValue = "DUMMY", matchIfMissing = true)
public class DummyRagLLMClient implements RagLLMClient {
    
    @Override
    public RagAnswer generateAnswer(String question, String context, ExplainFocus focus) {
        try {
            // Simula processamento do contexto
            JsonObject contextJson = JsonParser.parseString(context).getAsJsonObject();
            
            String answer = generateSimulatedAnswer(question, contextJson, focus);
            ConfidenceLevel confidence = determineConfidence(contextJson);
            
            return new RagAnswer(answer, confidence, true);
            
        } catch (Exception e) {
            // Falha na simulação
            return new RagAnswer(
                "Erro ao processar contexto: " + e.getMessage(),
                ConfidenceLevel.LOW,
                false
            );
        }
    }
    
    private String generateSimulatedAnswer(String question, JsonObject context, ExplainFocus focus) {
        StringBuilder answer = new StringBuilder();
        
        int ruleCount = context.has("rules") ? context.getAsJsonArray("rules").size() : 0;
        int incidentCount = context.has("incidents") ? context.getAsJsonArray("incidents").size() : 0;
        int ownershipCount = context.has("ownerships") ? context.getAsJsonArray("ownerships").size() : 0;
        
        answer.append("Com base nos dados do sistema, ");
        
        if (focus == ExplainFocus.BUSINESS) {
            answer.append(String.format("encontrei %d regra(s) de negócio relevante(s). ", ruleCount));
            if (incidentCount > 0) {
                answer.append(String.format("Há registro de %d incidente(s) relacionado(s). ", incidentCount));
            }
            answer.append("As regras impactam principalmente processos críticos de pagamento e validação de dados.");
            
        } else if (focus == ExplainFocus.TECHNICAL) {
            answer.append(String.format("identifiquei %d regra(s) com dependências técnicas. ", ruleCount));
            answer.append("As implementações seguem padrões de validação em múltiplas camadas. ");
            if (incidentCount > 0) {
                answer.append("Incidentes anteriores indicam pontos de atenção em integrações.");
            }
            
        } else if (focus == ExplainFocus.EXECUTIVE) {
            answer.append(String.format("analisando %d regra(s), ", ruleCount));
            if (ownershipCount > 0) {
                answer.append(String.format("com %d ownership(s) mapeado(s). ", ownershipCount));
            }
            answer.append("O impacto envolve múltiplos domínios de negócio. ");
            if (incidentCount > 0) {
                answer.append(String.format("Histórico mostra %d incidente(s), sugerindo necessidade de monitoramento.", incidentCount));
            }
        }
        
        answer.append("\n\n");
        answer.append("📌 Recomendação: Consulte as fontes detalhadas abaixo para decisões críticas.");
        
        return answer.toString();
    }
    
    private ConfidenceLevel determineConfidence(JsonObject context) {
        int ruleCount = context.has("rules") ? context.getAsJsonArray("rules").size() : 0;
        int incidentCount = context.has("incidents") ? context.getAsJsonArray("incidents").size() : 0;
        int ownershipCount = context.has("ownerships") ? context.getAsJsonArray("ownerships").size() : 0;
        
        int totalSources = ruleCount + incidentCount + ownershipCount;
        
        if (totalSources >= 5) {
            return ConfidenceLevel.HIGH;
        } else if (totalSources >= 2) {
            return ConfidenceLevel.MEDIUM;
        } else {
            return ConfidenceLevel.LOW;
        }
    }
}
