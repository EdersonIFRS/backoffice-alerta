// Types baseados nos DTOs do backend (US#57)

export enum AlertSeverity {
  INFO = 'INFO',
  WARNING = 'WARNING',
  CRITICAL = 'CRITICAL'
}

export enum AlertType {
  RULE_EXECUTION = 'RULE_EXECUTION',
  DEPENDENCY_FAILURE = 'DEPENDENCY_FAILURE',
  SLA_BREACH = 'SLA_BREACH',
  PERFORMANCE_DEGRADATION = 'PERFORMANCE_DEGRADATION',
  DATA_QUALITY = 'DATA_QUALITY',
  COMPLIANCE = 'COMPLIANCE'
}

export enum NotificationChannel {
  SLACK = 'SLACK',
  TEAMS = 'TEAMS',
  EMAIL = 'EMAIL',
  SMS = 'SMS'
}

export enum AlertDeliveryWindow {
  BUSINESS_HOURS = 'BUSINESS_HOURS',
  ANY_TIME = 'ANY_TIME'
}

export enum PreferenceSource {
  RULE = 'RULE',
  PROJECT = 'PROJECT',
  DEFAULT = 'DEFAULT'
}

export interface AlertPreferenceRequest {
  minimumSeverity: AlertSeverity;
  allowedAlertTypes: AlertType[];
  channels: NotificationChannel[];
  deliveryWindow: AlertDeliveryWindow;
}

export interface AlertPreferenceResponse {
  id: string;
  projectId?: string;
  businessRuleId?: string;
  minimumSeverity: AlertSeverity;
  allowedAlertTypes: AlertType[];
  channels: NotificationChannel[];
  deliveryWindow: AlertDeliveryWindow;
  createdAt: string;
  updatedAt: string;
}

export interface EffectiveAlertPreferenceResponse {
  source: PreferenceSource;
  projectId?: string;
  businessRuleId?: string;
  minimumSeverity: AlertSeverity;
  allowedAlertTypes: AlertType[];
  channels: NotificationChannel[];
  deliveryWindow: AlertDeliveryWindow;
}

export interface AlertSimulationRequest {
  projectId?: string;
  businessRuleId?: string;
  alertType: AlertType;
  severity: AlertSeverity;
  channel: NotificationChannel;
}

export interface AlertSimulationResult {
  wouldSend: boolean;
  channel: NotificationChannel;
  reason: string;
  effectivePreference: EffectiveAlertPreferenceResponse;
}
