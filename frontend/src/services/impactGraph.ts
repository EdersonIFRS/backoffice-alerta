// Serviço de API para visualização de grafo de impacto
// US#37 - Visualização de Impacto Sistêmico (Mapa de Dependências)

import { impactGraphApi } from './api';
import type {
  BusinessImpactGraphRequest,
  BusinessImpactGraphResponse,
} from '../types/impactGraph';

/**
 * Gera grafo visual de impacto sistêmico
 * 
 * @param request Pull Request ID e arquivos alterados
 * @returns Grafo completo com nós, arestas e sumário
 */
export const generateImpactGraph = async (
  request: BusinessImpactGraphRequest
): Promise<BusinessImpactGraphResponse> => {
  return await impactGraphApi.generate(request);
};

