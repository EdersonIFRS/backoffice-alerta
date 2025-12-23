// US#47 - Service para Chat Unificado de Análise de Impacto (Risk Chat)
import api from './api';
import type { ChatQueryRequest, ChatResponse } from '../types/riskChat';

/**
 * Service para consumir endpoint de chat unificado
 * 
 * US#47 - Frontend do Chat Unificado de Análise de Impacto
 * Consome endpoint da US#46 (backend)
 * 
 * Endpoint: POST /risk/chat/query
 * Segurança: JWT obrigatório, RBAC (ADMIN, RISK_MANAGER, ENGINEER)
 * Read-only: Não persiste, não audita, não notifica
 */
export const riskChatApi = {
  /**
   * Envia pergunta ao chat de análise de impacto
   * 
   * @param request - Pergunta e contexto opcional (focus, PR, ambiente)
   * @returns Response com mensagens estruturadas (INFO/WARNING/ACTION)
   * @throws AxiosError em caso de falha HTTP (tratado por interceptor)
   */
  async queryChat(request: ChatQueryRequest): Promise<ChatResponse> {
    try {
      const response = await api.post<ChatResponse>(
        '/risk/chat/query',
        request
      );
      return response.data;
    } catch (error: any) {
      // Fallback amigável se backend falhar
      console.error('Erro ao consultar chat de risco:', error);
      
      // Retorna resposta de fallback (não quebra a UI)
      return {
        answer: 'Não foi possível processar sua pergunta no momento. Por favor, tente novamente.',
        messages: [
          {
            type: 'WARNING',
            title: 'Erro de Comunicação',
            content: 'O serviço de análise de impacto está temporariamente indisponível. Verifique sua conexão ou tente novamente em alguns instantes.',
            sources: []
          }
        ],
        confidence: 'LOW',
        usedFallback: true,
        disclaimer: 'Esta é uma mensagem de erro. O sistema não pôde processar sua solicitação.'
      };
    }
  }
};
