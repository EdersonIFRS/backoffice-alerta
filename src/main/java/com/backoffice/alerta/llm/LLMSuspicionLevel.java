package com.backoffice.alerta.llm;

/**
 * US#70 - Nível de suspeição de mudança gerada por LLM
 * 
 * Classificação determinística baseada em score (0-100):
 * - LOW: 0-29 pontos
 * - MEDIUM: 30-59 pontos
 * - HIGH: 60-100 pontos
 */
public enum LLMSuspicionLevel {
    
    /**
     * Baixo risco (0-29 pontos)
     * Mudanças pequenas, escopo claro, sem padrões suspeitos
     */
    LOW,
    
    /**
     * Risco médio (30-59 pontos)
     * Mudanças moderadas, alguns padrões de código gerado
     */
    MEDIUM,
    
    /**
     * Alto risco (60-100 pontos)
     * Mudanças massivas, fora de escopo, sem testes, muitos padrões suspeitos
     */
    HIGH;
    
    /**
     * Determina nível baseado no score total
     */
    public static LLMSuspicionLevel fromScore(int score) {
        if (score < 30) {
            return LOW;
        } else if (score < 60) {
            return MEDIUM;
        } else {
            return HIGH;
        }
    }
}
