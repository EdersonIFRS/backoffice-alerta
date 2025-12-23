package com.backoffice.alerta.executive;

/**
 * Nível de confiança da explicação gerada
 * 
 * Indica quão completa e confiável é a análise baseada nos dados disponíveis.
 * 
 * US#38 - Explicação Executiva Inteligente
 */
public enum ConfidenceLevel {
    /**
     * Baixa confiança: dados insuficientes ou contexto incompleto
     */
    LOW,
    
    /**
     * Confiança média: dados básicos disponíveis, análise parcial
     */
    MEDIUM,
    
    /**
     * Alta confiança: dados completos, histórico robusto, análise completa
     */
    HIGH
}
