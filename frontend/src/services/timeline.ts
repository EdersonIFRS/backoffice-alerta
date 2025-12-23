// US#40 - Service para Timeline de Decisão
import api from './api';
import type { ChangeTimelineResponse } from '../types/timeline';

/**
 * Service para consumir endpoint de timeline
 * 
 * US#40 - Linha do Tempo de Decisão de Mudança
 */
export const timelineApi = {
  /**
   * Obtém timeline completa de um Pull Request
   * 
   * @param pullRequestId - ID do Pull Request
   * @returns Timeline com todos os eventos ordenados
   */
  async getTimeline(pullRequestId: string): Promise<ChangeTimelineResponse> {
    const { data } = await api.get<ChangeTimelineResponse>(`/risk/timeline/${pullRequestId}`);
    return data;
  },

  /**
   * Verifica saúde/completude da timeline
   * 
   * @param pullRequestId - ID do Pull Request
   * @returns Estatísticas da timeline
   */
  async getTimelineHealth(pullRequestId: string): Promise<any> {
    const { data } = await api.get(`/risk/timeline/${pullRequestId}/health`);
    return data;
  }
};
