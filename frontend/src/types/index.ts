// US#31 - Frontend Executivo de Risco (Read-Only)
// Tipos TypeScript para todas as entidades do backend

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  username: string;
  roles: string[];
  issuedAt: string;
  expiresAt: string;
}

export type UserRole = 'ADMIN' | 'RISK_MANAGER' | 'ENGINEER' | 'VIEWER';

export interface User {
  username: string;
  roles: UserRole[];
  token: string;
}

// Dashboard Executivo
export type ConfidenceStatus = 'EXCELLENT' | 'HEALTHY' | 'ATTENTION' | 'CRITICAL';
export type Environment = 'GLOBAL' | 'PRODUCTION_ONLY';

export interface ExecutiveSummary {
  confidenceStatus: ConfidenceStatus;
  systemConfidenceScore: number;
  accuracyRate: number;
  falsePositiveRate: number;
  falseNegativeRate: number;
  incidentAfterApprovalRate: number;
  activeAlerts: Alert[];
  topProblematicRules: ProblematicRule[];
  reportGeneratedAt: string;
}

export interface Alert {
  message: string;
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
  category: string;
  affectedArea: string;
}

export interface ProblematicRule {
  ruleId: string;
  ruleName: string;
  incidentCount: number;
  lastIncidentDate: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
}

// Auditoria
export type FinalDecision = 'APPROVED' | 'REJECTED' | 'REQUIRES_REVIEW' | 'CONDITIONAL_APPROVAL';
export type RiskLevel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export interface RiskDecisionAudit {
  id: string;
  pullRequestId: string;
  finalDecision: FinalDecision;
  riskLevel: RiskLevel;
  environment: string;
  impactedBusinessRules: string[];
  createdAt: string;
  createdBy: string;
  incidentSummary: Record<string, number>;
  restrictions?: string[];
}

// SLA
export type SlaStatus = 'ACTIVE' | 'ACKNOWLEDGED' | 'ESCALATED' | 'BREACHED' | 'RESOLVED';
export type EscalationLevel = 'L1_TEAM_LEAD' | 'L2_ENGINEERING_MANAGER' | 'L3_DIRECTOR' | 'L4_VP_CTO';

export interface RiskSlaTracking {
  id: string;
  auditId: string;
  status: SlaStatus;
  riskLevel: RiskLevel;
  teamName: string;
  slaDeadline: string;
  currentLevel: EscalationLevel;
  createdAt: string;
  acknowledgedAt?: string;
  resolvedAt?: string;
  isOverdue: boolean;
}

// Notificações
export type NotificationSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO';
export type NotificationChannel = 'SLACK' | 'EMAIL' | 'TEAMS' | 'PAGERDUTY' | 'JIRA';

export interface RiskNotification {
  id: string;
  auditId: string;
  teamName: string;
  severity: NotificationSeverity;
  channel: NotificationChannel;
  message: string;
  createdAt: string;
}

// Métricas
export interface RiskMetrics {
  accuracyRate: number;
  falsePositiveRate: number;
  falseNegativeRate: number;
  incidentAfterApprovalRate: number;
  systemConfidenceScore: number;
  totalDecisions: number;
  correctDecisions: number;
  falsePositives: number;
  falseNegatives: number;
  reportPeriodStart: string;
  reportPeriodEnd: string;
  trends: Trend[];
}

export interface Trend {
  indicator: string;
  direction: 'UP' | 'DOWN' | 'STABLE';
  description: string;
  severity: 'POSITIVE' | 'NEGATIVE' | 'NEUTRAL';
}
