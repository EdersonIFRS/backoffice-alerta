package com.backoffice.alerta.rag;

/**
 * Interface para cliente LLM do RAG
 * Permite troca de implementação (dummy, OpenAI, local, etc.)
 */
public interface RagLLMClient {
    
    /**
     * Gera resposta baseada em contexto estruturado
     * 
     * @param question Pergunta do usuário
     * @param context Contexto JSON com dados reais do sistema
     * @param focus Foco da explicação
     * @return Resposta gerada pela IA
     */
    RagAnswer generateAnswer(String question, String context, ExplainFocus focus);
    
    /**
     * Classe interna para resposta da IA
     */
    class RagAnswer {
        private String answer;
        private ConfidenceLevel confidence;
        private boolean success;
        
        public RagAnswer(String answer, ConfidenceLevel confidence, boolean success) {
            this.answer = answer;
            this.confidence = confidence;
            this.success = success;
        }
        
        public String getAnswer() {
            return answer;
        }
        
        public ConfidenceLevel getConfidence() {
            return confidence;
        }
        
        public boolean isSuccess() {
            return success;
        }
    }
}
