// US#39 - Service para Explicação Executiva de Impacto Sistêmico
import api from './api';
import type { ExecutiveImpactExplainRequest, ExecutiveImpactExplainResponse } from '../types/executiveImpact';

/**
 * Service para consumir endpoint de explicação executiva
 * 
 * US#39 - Visualização Executiva do Impacto Sistêmico (Frontend)
 * Consome endpoint da US#38
 */
export const executiveImpactApi = {
  /**
   * Gera explicação executiva do impacto sistêmico
   * 
   * @param request - Dados da análise (PR, ambiente, tipo de mudança)
   * @returns Response com explicação executiva estruturada
   */
  async generateExplanation(request: ExecutiveImpactExplainRequest): Promise<ExecutiveImpactExplainResponse> {
    const response = await api.post<ExecutiveImpactExplainResponse>(
      '/risk/business-impact/executive-explain',
      request
    );
    return response.data;
  }
};
