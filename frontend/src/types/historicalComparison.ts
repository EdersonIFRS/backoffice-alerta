// US#42 - Visualização Executiva da Comparação Histórica
// Types mapeando exatamente os DTOs do backend

export type Environment = 'DEV' | 'STAGING' | 'PRODUCTION';
export type ChangeType = 'FEATURE' | 'HOTFIX' | 'REFACTOR' | 'CONFIG';
export type FinalDecision = 'APROVADO' | 'APROVADO_COM_RESTRICOES' | 'BLOQUEADO';
export type RiskLevel = 'BAIXO' | 'MEDIO' | 'ALTO' | 'CRITICO';
export type Domain = 'PAYMENT' | 'AUTH' | 'NOTIFICATION' | 'REPORTING';

export interface DecisionHistoricalComparisonRequest {
  currentPullRequestId: string;
  environment?: Environment;
  changeType?: ChangeType;
  changedFiles: string[];
  lookbackDays?: number; // default 180
  maxComparisons?: number; // default 5
}

export interface CurrentDecisionContextResponse {
  riskLevel: RiskLevel;
  finalDecision: FinalDecision;
  businessDomains: Domain[];
  criticalRules: number;
}

export type FeedbackOutcome = 
  | 'CORRECT_APPROVAL'
  | 'CORRECT_REJECTION'
  | 'FALSE_POSITIVE_BLOCK'
  | 'FALSE_NEGATIVE_RISK';

export type IncidentSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export interface HistoricalDecisionComparisonResponse {
  pullRequestId: string;
  similarityScore: number;
  decision: FinalDecision;
  riskLevel: RiskLevel;
  environment: Environment;
  outcome: FeedbackOutcome | null;
  incidentSeverity: IncidentSeverity | null;
  slaBreached: boolean;
  summary: string;
}

export interface ExecutiveHistoricalInsightResponse {
  patternDetected: boolean;
  patternDescription: string;
  recommendation: string;
}

export interface DecisionHistoricalComparisonResponse {
  currentContextSummary: CurrentDecisionContextResponse;
  historicalComparisons: HistoricalDecisionComparisonResponse[];
  executiveInsights: ExecutiveHistoricalInsightResponse;
}
