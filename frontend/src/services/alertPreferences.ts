import api from './api';
import {
  AlertPreferenceRequest,
  AlertPreferenceResponse,
  EffectiveAlertPreferenceResponse
} from '../types/alertPreferences';

/**
 * Serviço para gerenciamento de preferências de alertas (US#57)
 * Endpoints fornecidos pelo backend:
 * - ProjectAlertPreferenceController
 * - BusinessRuleAlertPreferenceController
 * - AlertPreferenceController
 */

// ========== Preferências por Projeto ==========

export const getProjectPreferences = async (
  projectId: string
): Promise<AlertPreferenceResponse | null> => {
  try {
    const response = await api.get<AlertPreferenceResponse>(
      `/api/projects/${projectId}/alert-preferences`
    );
    return response.data;
  } catch (error: any) {
    if (error.response?.status === 404) {
      return null; // Projeto sem preferências configuradas
    }
    throw error;
  }
};

export const saveProjectPreferences = async (
  projectId: string,
  payload: AlertPreferenceRequest
): Promise<AlertPreferenceResponse> => {
  try {
    // Tenta criar primeiro (POST)
    const response = await api.post<AlertPreferenceResponse>(
      `/api/projects/${projectId}/alert-preferences`,
      payload
    );
    return response.data;
  } catch (error: any) {
    // Se já existe, atualiza (PUT)
    if (error.response?.status === 409 || error.response?.status === 400) {
      const response = await api.put<AlertPreferenceResponse>(
        `/api/projects/${projectId}/alert-preferences`,
        payload
      );
      return response.data;
    }
    throw error;
  }
};

// ========== Preferências por Regra de Negócio ==========

export const getRulePreferences = async (
  ruleId: string
): Promise<AlertPreferenceResponse | null> => {
  try {
    const response = await api.get<AlertPreferenceResponse>(
      `/api/business-rules/${ruleId}/alert-preferences`
    );
    return response.data;
  } catch (error: any) {
    if (error.response?.status === 404) {
      return null; // Regra sem preferências configuradas
    }
    throw error;
  }
};

export const saveRulePreferences = async (
  ruleId: string,
  payload: AlertPreferenceRequest
): Promise<AlertPreferenceResponse> => {
  const response = await api.post<AlertPreferenceResponse>(
    `/api/business-rules/${ruleId}/alert-preferences`,
    payload
  );
  return response.data;
};

// ========== Preferência Efetiva (Resolução Hierárquica) ==========

export const getEffectivePreferences = async (
  projectId?: string,
  businessRuleId?: string
): Promise<EffectiveAlertPreferenceResponse> => {
  const params: any = {};
  if (projectId) params.projectId = projectId;
  if (businessRuleId) params.businessRuleId = businessRuleId;

  const response = await api.get<EffectiveAlertPreferenceResponse>(
    '/api/alerts/preferences/effective',
    { params }
  );
  return response.data;
};

// ========== Helpers para UI ==========

export const getDefaultPreferences = (): AlertPreferenceRequest => ({
  minimumSeverity: 'INFO' as any,
  allowedAlertTypes: [],
  channels: ['SLACK', 'TEAMS'] as any[],
  deliveryWindow: 'ANY_TIME' as any
});
