package com.backoffice.alerta.rules;

/**
 * Tipo de dependência entre regras de negócio
 * 
 * Define a natureza da relação entre duas regras para análise de impacto cruzado.
 * 
 * US#36 - Análise de Impacto Cruzado (Cadeia de Regras Afetadas)
 */
public enum BusinessRuleDependencyType {
    
    /**
     * Regra A depende de regra B para executar sua lógica
     * Exemplo: Regra de desconto depende de regra de validação de cliente
     */
    DEPENDS_ON("Depende de", "Esta regra precisa da outra para funcionar corretamente"),
    
    /**
     * Regra A alimenta dados para regra B
     * Exemplo: Regra de cálculo de juros alimenta regra de totalização
     */
    FEEDS("Alimenta", "Esta regra fornece dados/resultados para a outra"),
    
    /**
     * Regra A valida resultados de regra B
     * Exemplo: Regra de auditoria valida regra de pagamento
     */
    VALIDATES("Valida", "Esta regra verifica/valida a execução da outra"),
    
    /**
     * Regra A agrega/totaliza resultados de regra B
     * Exemplo: Regra de faturamento total agrega regras de itens individuais
     */
    AGGREGATES("Agrega", "Esta regra consolida/totaliza resultados da outra"),
    
    /**
     * Regra A deriva seus valores de regra B
     * Exemplo: Regra de comissão deriva valores de regra de vendas
     */
    DERIVES_FROM("Deriva de", "Esta regra calcula seus valores baseada na outra");
    
    private final String label;
    private final String description;
    
    BusinessRuleDependencyType(String label, String description) {
        this.label = label;
        this.description = description;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getDescription() {
        return description;
    }
}
