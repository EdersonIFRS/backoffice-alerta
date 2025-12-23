// US#31 - Configuração do Axios com interceptors
import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import type { 
  LoginRequest, 
  LoginResponse, 
  ExecutiveSummary,
  RiskDecisionAudit,
  RiskSlaTracking,
  RiskNotification,
  RiskMetrics,
  Environment
} from '../types';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Token em memória (NÃO usar localStorage)
let authToken: string | null = null;

export const setAuthToken = (token: string | null) => {
  authToken = token;
};

export const getAuthToken = () => authToken;

// Interceptor: adiciona JWT em todas as requisições
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (authToken && config.headers) {
      config.headers.Authorization = `Bearer ${authToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor: trata 401 e 403
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Limpa token e redireciona para login
      setAuthToken(null);
      window.location.href = '/login';
    } else if (error.response?.status === 403) {
      // Acesso negado
      alert('Acesso não autorizado. Você não tem permissão para acessar este recurso.');
    }
    return Promise.reject(error);
  }
);

// API de Autenticação
export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const { data } = await api.post<LoginResponse>('/auth/login', credentials);
    return data;
  }
};

// API de Dashboard Executivo
export const dashboardApi = {
  getExecutiveSummary: async (environment?: Environment): Promise<ExecutiveSummary> => {
    const params = environment ? { environment } : {};
    const { data } = await api.get<ExecutiveSummary>('/risk/dashboard/executive', { params });
    return data;
  }
};

// API de Auditorias
export const auditApi = {
  getAll: async (): Promise<RiskDecisionAudit[]> => {
    const { data } = await api.get<RiskDecisionAudit[]>('/risk/audit');
    return data;
  },
  
  getByPullRequest: async (pullRequestId: string): Promise<RiskDecisionAudit[]> => {
    const { data } = await api.get<RiskDecisionAudit[]>(`/risk/audit/pr/${pullRequestId}`);
    return data;
  },
  
  getByDecision: async (decision: string): Promise<RiskDecisionAudit[]> => {
    const { data } = await api.get<RiskDecisionAudit[]>(`/risk/audit/decision/${decision}`);
    return data;
  }
};

// API de SLAs
export const slaApi = {
  getAll: async (): Promise<RiskSlaTracking[]> => {
    const { data } = await api.get<RiskSlaTracking[]>('/risk/sla');
    return data;
  },
  
  getByStatus: async (status: string): Promise<RiskSlaTracking[]> => {
    const { data } = await api.get<RiskSlaTracking[]>(`/risk/sla/status/${status}`);
    return data;
  },
  
  getBreached: async (): Promise<RiskSlaTracking[]> => {
    const { data } = await api.get<RiskSlaTracking[]>('/risk/sla/breached');
    return data;
  }
};

// API de Notificações
export const notificationApi = {
  getAll: async (): Promise<RiskNotification[]> => {
    const { data } = await api.get<RiskNotification[]>('/risk/notifications');
    return data;
  },
  
  getByAudit: async (auditId: string): Promise<RiskNotification[]> => {
    const { data } = await api.get<RiskNotification[]>(`/risk/notifications/audit/${auditId}`);
    return data;
  },
  
  getByTeam: async (teamName: string): Promise<RiskNotification[]> => {
    const { data } = await api.get<RiskNotification[]>(`/risk/notifications/team/${teamName}`);
    return data;
  }
};

// API de Métricas
export const metricsApi = {
  get: async (): Promise<RiskMetrics> => {
    const { data } = await api.get<RiskMetrics>('/risk/metrics');
    return data;
  }
};

// API de Impacto Sistêmico (US#37)
export const impactGraphApi = {
  generate: async (request: { pullRequestId: string; changedFiles: string[] }): Promise<any> => {
    const { data } = await api.post('/risk/business-impact/graph', request);
    return data;
  }
};

export default api;
