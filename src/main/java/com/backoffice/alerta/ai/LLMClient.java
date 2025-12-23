package com.backoffice.alerta.ai;

import java.util.Map;

/**
 * Interface para cliente de LLM (Large Language Model)
 * Abstração que permite trocar implementações sem alterar o código
 */
public interface LLMClient {

    /**
     * Gera análise consultiva baseada no contexto fornecido
     * @param context Contexto estruturado com dados da análise de risco
     * @return Texto em linguagem natural com análise consultiva
     * @throws RuntimeException se houver falha na comunicação com a IA
     */
    String generateAdvisory(Map<String, Object> context);
}
