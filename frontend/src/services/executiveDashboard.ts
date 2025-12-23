import api from './api';

export interface ExecutiveDashboardSummary {
  totalGates: number;
  blockRate: number;
  warningRate: number;
  criticalAlertsLast7Days: number;
  alertFatigueDetected: boolean;
}

export interface ProjectRiskSummary {
  projectId: string;
  projectName: string;
  blockRate: number;
  alertsLast30Days: number;
}

export interface RuleRiskSummary {
  ruleId: string;
  ruleName: string;
  blockCount: number;
  incidentCount: number;
}

export interface AlertTrendPoint {
  date: string;
  sent: number;
  skipped: number;
  failed: number;
}

export interface ActiveAlertSummary {
  alertType: string;
  severity: string;
  message: string;
}

export interface ExecutiveDashboardResponse {
  summary: ExecutiveDashboardSummary;
  topProjects: ProjectRiskSummary[];
  topRules: RuleRiskSummary[];
  alertTrends: AlertTrendPoint[];
  activeAlerts: ActiveAlertSummary[];
}

/**
 * Buscar dashboard executivo consolidado
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
export const getExecutiveDashboard = async (): Promise<ExecutiveDashboardResponse> => {
  const response = await api.get<ExecutiveDashboardResponse>('/risk/dashboard/alerts-executive');
  return response.data;
};
