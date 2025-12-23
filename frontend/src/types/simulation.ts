// US#31.1 - Tipos para Simulação de Risco (What-If)

export interface SimulationScenario {
  removeHighRiskFiles: boolean;
  reduceFileCount: boolean;
  markAsHotfix: boolean;
  changeEnvironment: boolean;
  targetEnvironment?: 'DEV' | 'STAGING' | 'PRODUCTION';
}

export interface SimulationRequest {
  pullRequestId: string;
  environment: 'DEV' | 'STAGING' | 'PRODUCTION';
  changeType: 'FEATURE' | 'HOTFIX' | 'REFACTOR';
  scenario: SimulationScenario;
}

export interface SimulationRiskResult {
  decision: 'APPROVED' | 'REJECTED' | 'REQUIRES_REVIEW' | 'CONDITIONAL_APPROVAL';
  riskLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  impactedRules: string[];
  restrictions: string[];
  slaTriggered: boolean;
  notifiedTeams: string[];
}

export interface SimulationResponse {
  pullRequestId: string;
  baseline: SimulationRiskResult;
  simulated: SimulationRiskResult;
  changes: {
    decisionChanged: boolean;
    riskReduced: boolean;
    rulesRemoved: string[];
    restrictionsRemoved: string[];
    slaAvoided: boolean;
    lessNotifications: boolean;
  };
}
