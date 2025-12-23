package com.backoffice.alerta.executive;

/**
 * Foco da explicação executiva
 * 
 * Define o nível de detalhamento da explicação gerada.
 * 
 * US#38 - Explicação Executiva Inteligente
 */
public enum ExplainFocus {
    /**
     * Foco executivo: linguagem de alto nível, impacto de negócio, decisões estratégicas
     */
    EXECUTIVE,
    
    /**
     * Foco de negócio: impacto em processos, áreas funcionais, times
     */
    BUSINESS,
    
    /**
     * Foco técnico: detalhes de implementação, dependências técnicas, arquitetura
     */
    TECHNICAL
}
