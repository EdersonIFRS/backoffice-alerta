// US#40 - Types para Timeline de Decisão

export enum TimelineEventType {
  PROPOSAL = 'PROPOSAL',
  IMPACT_ANALYSIS = 'IMPACT_ANALYSIS',
  EXECUTIVE_EXPLANATION = 'EXECUTIVE_EXPLANATION',
  SIMULATION = 'SIMULATION',
  DECISION = 'DECISION',
  AUDIT = 'AUDIT',
  NOTIFICATION = 'NOTIFICATION',
  SLA_CREATED = 'SLA_CREATED',
  SLA_ESCALATED = 'SLA_ESCALATED',
  FEEDBACK = 'FEEDBACK'
}

export interface TimelineEventResponse {
  id: string;
  eventType: TimelineEventType;
  title: string;
  description: string;
  createdAt: string;
  actor: 'SYSTEM' | 'USER' | 'AI';
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  relatedEntityId: string;
  metadata: Record<string, string>;
}

export interface ChangeTimelineResponse {
  pullRequestId: string;
  environment: string;
  finalDecision: string;
  overallRiskLevel: string;
  requiresExecutiveAttention: boolean;
  events: TimelineEventResponse[];
}
