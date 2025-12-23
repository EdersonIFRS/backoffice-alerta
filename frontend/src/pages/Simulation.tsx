// US#31.1 - Página de Simulação de Risco (What-If)
import React, { useState } from 'react';
import { 
  Container, 
  Box, 
  Typography,
  Grid,
  Alert,
  CircularProgress
} from '@mui/material';
import { SimulationForm } from '../components/SimulationForm';
import { SimulationResult } from '../components/SimulationResult';
import { SimulationComparison } from '../components/SimulationComparison';
import type { SimulationRequest, SimulationResponse, SimulationRiskResult } from '../types/simulation';

export const Simulation: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<SimulationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSimulate = async (request: SimulationRequest) => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // Simular baseline (cenário atual)
      const baseline = simulateBaseline(request);
      
      // Simular cenário com alterações
      const simulated = simulateScenario(request);
      
      // Calcular diferenças
      const changes = calculateChanges(baseline, simulated);

      // Simular delay de processamento
      await new Promise(resolve => setTimeout(resolve, 1000));

      setResult({
        pullRequestId: request.pullRequestId,
        baseline,
        simulated,
        changes
      });
    } catch (err) {
      setError('Erro ao executar simulação. Tente novamente.');
      console.error('Simulation error:', err);
    } finally {
      setLoading(false);
    }
  };

  // Simula o cenário baseline (atual) baseado nos inputs
  const simulateBaseline = (request: SimulationRequest): SimulationRiskResult => {
    const { environment, changeType } = request;
    
    // Lógica simplificada de risco baseada em ambiente e tipo
    let riskLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' = 'MEDIUM';
    let decision: 'APPROVED' | 'REJECTED' | 'REQUIRES_REVIEW' | 'CONDITIONAL_APPROVAL' = 'APPROVED';
    
    if (environment === 'PRODUCTION') {
      if (changeType === 'HOTFIX') {
        riskLevel = 'CRITICAL';
        decision = 'REQUIRES_REVIEW';
      } else {
        riskLevel = 'HIGH';
        decision = 'CONDITIONAL_APPROVAL';
      }
    } else if (environment === 'STAGING') {
      riskLevel = 'MEDIUM';
      decision = 'APPROVED';
    } else {
      riskLevel = 'LOW';
      decision = 'APPROVED';
    }

    const impactedRules = [
      'RULE_PROD_DEPLOY_RESTRICTIONS',
      'RULE_CODE_REVIEW_REQUIRED',
      'RULE_SECURITY_SCAN_MANDATORY',
      'RULE_PERFORMANCE_BASELINE',
      'RULE_ROLLBACK_PLAN_REQUIRED'
    ];

    const restrictions = riskLevel === 'CRITICAL' || riskLevel === 'HIGH' 
      ? [
          'Aprovação de 2 senior engineers obrigatória',
          'Testes de integração completos',
          'Plano de rollback documentado',
          'Monitoramento em tempo real por 2 horas'
        ]
      : riskLevel === 'MEDIUM'
      ? ['Aprovação de 1 senior engineer', 'Testes de integração']
      : [];

    return {
      decision,
      riskLevel,
      impactedRules,
      restrictions,
      slaTriggered: riskLevel === 'CRITICAL',
      notifiedTeams: riskLevel === 'CRITICAL' || riskLevel === 'HIGH' 
        ? ['Platform Team', 'Security Team', 'DevOps Team']
        : ['Platform Team']
    };
  };

  // Simula o cenário com as alterações do what-if
  const simulateScenario = (request: SimulationRequest): SimulationRiskResult => {
    const { scenario } = request;
    const baseline = simulateBaseline(request);
    
    let newRiskLevel = baseline.riskLevel;
    let newDecision = baseline.decision;
    let impactedRules = [...baseline.impactedRules];
    let restrictions = [...baseline.restrictions];
    let slaTriggered = baseline.slaTriggered;
    let notifiedTeams = [...baseline.notifiedTeams];

    // Aplicar efeitos de cada cenário
    if (scenario.removeHighRiskFiles) {
      // Remover regras de segurança e performance
      impactedRules = impactedRules.filter(r => 
        !r.includes('SECURITY') && !r.includes('PERFORMANCE')
      );
      
      // Reduzir risco em um nível
      if (newRiskLevel === 'CRITICAL') {
        newRiskLevel = 'HIGH';
        slaTriggered = false;
      } else if (newRiskLevel === 'HIGH') {
        newRiskLevel = 'MEDIUM';
      } else if (newRiskLevel === 'MEDIUM') {
        newRiskLevel = 'LOW';
      }
    }

    if (scenario.reduceFileCount) {
      // Remover regra de revisão de código
      impactedRules = impactedRules.filter(r => !r.includes('CODE_REVIEW'));
      
      // Reduzir restrições
      restrictions = restrictions.slice(0, Math.max(0, restrictions.length - 2));
    }

    if (scenario.markAsHotfix) {
      // HOTFIX aumenta risco mas pode justificar aprovação condicional
      if (newDecision === 'REQUIRES_REVIEW') {
        newDecision = 'CONDITIONAL_APPROVAL';
      }
    }

    if (scenario.changeEnvironment && scenario.targetEnvironment) {
      // Simular mudança de ambiente
      if (scenario.targetEnvironment === 'DEV') {
        newRiskLevel = 'LOW';
        newDecision = 'APPROVED';
        impactedRules = impactedRules.slice(0, 2);
        restrictions = [];
        slaTriggered = false;
        notifiedTeams = [];
      } else if (scenario.targetEnvironment === 'STAGING') {
        if (newRiskLevel === 'CRITICAL') newRiskLevel = 'HIGH';
        if (newRiskLevel === 'HIGH') newRiskLevel = 'MEDIUM';
        newDecision = 'APPROVED';
        slaTriggered = false;
        notifiedTeams = notifiedTeams.slice(0, 1);
      }
    }

    // Recalcular decisão baseada no novo risco
    if (newRiskLevel === 'LOW') {
      newDecision = 'APPROVED';
      restrictions = [];
      notifiedTeams = [];
    } else if (newRiskLevel === 'MEDIUM' && newDecision === 'REQUIRES_REVIEW') {
      newDecision = 'CONDITIONAL_APPROVAL';
    }

    return {
      decision: newDecision,
      riskLevel: newRiskLevel,
      impactedRules,
      restrictions,
      slaTriggered,
      notifiedTeams
    };
  };

  // Calcula as diferenças entre baseline e simulação
  const calculateChanges = (baseline: SimulationRiskResult, simulated: SimulationRiskResult) => {
    const riskLevels = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
    const baselineRiskIndex = riskLevels.indexOf(baseline.riskLevel);
    const simulatedRiskIndex = riskLevels.indexOf(simulated.riskLevel);

    const rulesRemoved = baseline.impactedRules.filter(
      rule => !simulated.impactedRules.includes(rule)
    );

    const restrictionsRemoved = baseline.restrictions.filter(
      restriction => !simulated.restrictions.includes(restriction)
    );

    return {
      decisionChanged: baseline.decision !== simulated.decision,
      riskReduced: simulatedRiskIndex < baselineRiskIndex,
      rulesRemoved,
      restrictionsRemoved,
      slaAvoided: baseline.slaTriggered && !simulated.slaTriggered,
      lessNotifications: simulated.notifiedTeams.length < baseline.notifiedTeams.length
    };
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          🧪 Simulação de Decisão de Risco
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Simule diferentes cenários e veja o impacto nas decisões de risco sem alterar dados reais.
        </Typography>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} lg={5}>
          <SimulationForm onSimulate={handleSimulate} loading={loading} />
        </Grid>

        <Grid item xs={12} lg={7}>
          {loading && (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
              <CircularProgress size={60} />
            </Box>
          )}

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          {result && !loading && (
            <Box>
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <SimulationResult 
                    title="📋 Cenário Atual (Baseline)" 
                    result={result.baseline}
                    isBaseline={true}
                  />
                </Grid>
                <Grid item xs={12} md={6}>
                  <SimulationResult 
                    title="✨ Cenário Simulado (What-If)" 
                    result={result.simulated}
                  />
                </Grid>
                <Grid item xs={12}>
                  <SimulationComparison comparison={result} />
                </Grid>
              </Grid>
            </Box>
          )}

          {!loading && !error && !result && (
            <Box 
              sx={{ 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center', 
                minHeight: 400,
                border: '2px dashed',
                borderColor: 'grey.300',
                borderRadius: 2,
                bgcolor: 'grey.50'
              }}
            >
              <Typography variant="h6" color="text.secondary">
                Configure a simulação e clique em "Executar Simulação"
              </Typography>
            </Box>
          )}
        </Grid>
      </Grid>
    </Container>
  );
};
