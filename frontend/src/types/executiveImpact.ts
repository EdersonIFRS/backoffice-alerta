// US#39 - Tipos TypeScript para Explicação Executiva de Impacto Sistêmico

export type Environment = 'DEVELOPMENT' | 'STAGING' | 'PRODUCTION';

export type ChangeType = 'FEATURE' | 'HOTFIX' | 'REFACTOR' | 'CONFIG';

export type ExplainFocus = 'EXECUTIVE' | 'BUSINESS' | 'TECHNICAL';

export type RiskLevel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export type ConfidenceLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface ExecutiveImpactExplainRequest {
  pullRequestId: string;
  environment: Environment;
  changeType: ChangeType;
  focus?: ExplainFocus;
}

export interface ExecutiveSummary {
  headline: string;
  businessImpact: string;
  areasAffected: string[];
  historicalContext: string;
  riskInterpretation: string;
  recommendation: string;
}

export interface ExecutiveImpactExplainResponse {
  pullRequestId: string;
  overallRiskLevel: RiskLevel;
  executiveSummary: ExecutiveSummary;
  confidenceLevel: ConfidenceLevel;
  generatedAt: string;
}
