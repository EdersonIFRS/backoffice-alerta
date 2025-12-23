package com.backoffice.alerta.rules;

/**
 * Sinal de aprendizado detectado através de análise de histórico
 * Indica padrões que sugerem necessidade de ajuste
 */
public enum LearningSignal {
    
    /**
     * Tendência de falsos positivos - sistema está muito conservador
     * Múltiplos deploys aprovados com risco ALTO que tiveram sucesso
     */
    FALSE_POSITIVE_TREND,
    
    /**
     * Tendência de falsos negativos - sistema está muito permissivo
     * Múltiplos deploys aprovados com risco BAIXO que causaram incidentes
     */
    FALSE_NEGATIVE_TREND,
    
    /**
     * Incidentes após aprovação - decisão foi permissiva demais
     * Mudanças aprovadas que resultaram em incidentes de produção
     */
    INCIDENT_AFTER_APPROVAL,
    
    /**
     * Mudanças seguras sendo bloqueadas desnecessariamente
     * Deploys bloqueados que posteriormente foram aprovados e tiveram sucesso
     */
    SAFE_CHANGE_OVERBLOCKED
}
