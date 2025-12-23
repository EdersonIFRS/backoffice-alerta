// Tipos TypeScript para visualização de grafo de impacto
// US#37 - Visualização de Impacto Sistêmico (Mapa de Dependências)

export type Domain = 'PAYMENT' | 'BILLING' | 'ORDER' | 'USER' | 'GENERIC';

export type Criticality = 'BAIXA' | 'MEDIA' | 'ALTA' | 'CRITICA';

export type ImpactLevel = 'DIRECT' | 'INDIRECT' | 'CASCADE';

export type DependencyType = 
  | 'DEPENDS_ON' 
  | 'FEEDS' 
  | 'VALIDATES' 
  | 'AGGREGATES' 
  | 'DERIVES_FROM';

export interface OwnershipInfo {
  teamName: string;
  role: string;
}

export interface ImpactGraphNode {
  ruleId: string;
  ruleName: string;
  domain: Domain;
  criticality: Criticality;
  impactLevel: ImpactLevel;
  ownerships: OwnershipInfo[];
  hasIncidents: boolean;
}

export interface ImpactGraphEdge {
  sourceRuleId: string;
  targetRuleId: string;
  dependencyType: DependencyType;
}

export interface GraphSummary {
  totalRules: number;
  direct: number;
  indirect: number;
  cascade: number;
  criticalRules: number;
  requiresExecutiveAttention: boolean;
}

export interface BusinessImpactGraphResponse {
  pullRequestId: string;
  nodes: ImpactGraphNode[];
  edges: ImpactGraphEdge[];
  summary: GraphSummary;
}

export interface BusinessImpactGraphRequest {
  pullRequestId: string;
  changedFiles: string[];
}
