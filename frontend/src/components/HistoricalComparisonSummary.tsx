// US#42 - Resumo Comparativo Executivo
import React from 'react';
import { Box, Paper, Typography, Grid, Chip } from '@mui/material';
import { TrendingUp, TrendingDown, TrendingFlat } from '@mui/icons-material';
import type { 
  DecisionHistoricalComparisonResponse,
  RiskLevel,
  FinalDecision 
} from '../types/historicalComparison';

interface Props {
  data: DecisionHistoricalComparisonResponse;
}

const RiskLevelBadge: React.FC<{ level: RiskLevel }> = ({ level }) => {
  const colors = {
    CRITICO: 'error',
    ALTO: 'warning',
    MEDIO: 'info',
    BAIXO: 'success'
  } as const;

  return <Chip label={level} color={colors[level]} size="small" />;
};

const DecisionBadge: React.FC<{ decision: FinalDecision }> = ({ decision }) => {
  const colors = {
    APROVADO: 'success',
    APROVADO_COM_RESTRICOES: 'warning',
    BLOQUEADO: 'error'
  } as const;

  const labels = {
    APROVADO: 'Aprovado',
    APROVADO_COM_RESTRICOES: 'Aprovado c/ Restrições',
    BLOQUEADO: 'Bloqueado'
  } as const;

  return <Chip label={labels[decision]} color={colors[decision]} size="small" />;
};

export const HistoricalComparisonSummary: React.FC<Props> = ({ data }) => {
  const { currentContextSummary, historicalComparisons } = data;

  // Calcular métricas históricas
  const historicalBlockedCount = historicalComparisons.filter(
    h => h.decision === 'BLOQUEADO'
  ).length;
  const historicalBlockedPercent = historicalComparisons.length > 0
    ? Math.round((historicalBlockedCount / historicalComparisons.length) * 100)
    : 0;

  const avgRiskLevel = calculateAverageRiskLevel(historicalComparisons);
  const mostCommonEnvironment = getMostCommon(historicalComparisons.map(h => h.environment));

  return (
    <Paper sx={{ p: 3, mb: 3 }}>
      <Typography variant="h6" gutterBottom>
        Resumo Comparativo Executivo
      </Typography>

      <Grid container spacing={3}>
        {/* Decisão Atual */}
        <Grid item xs={12} md={6}>
          <Paper variant="outlined" sx={{ p: 2, bgcolor: 'background.default' }}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              📊 DECISÃO ATUAL
            </Typography>
            
            <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="body2">Nível de Risco:</Typography>
                <RiskLevelBadge level={currentContextSummary.riskLevel} />
              </Box>
              
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="body2">Decisão Final:</Typography>
                <DecisionBadge decision={currentContextSummary.finalDecision} />
              </Box>
              
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="body2">Regras Críticas:</Typography>
                <Typography variant="h6">{currentContextSummary.criticalRules}</Typography>
              </Box>
              
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="body2">Domínios Impactados:</Typography>
                <Typography variant="body2">
                  {currentContextSummary.businessDomains.join(', ') || 'Nenhum'}
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>

        {/* Histórico Similar */}
        <Grid item xs={12} md={6}>
          <Paper variant="outlined" sx={{ p: 2, bgcolor: 'primary.50' }}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              📈 HISTÓRICO SIMILAR
            </Typography>
            
            <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="body2">Risco Médio:</Typography>
                <RiskLevelBadge level={avgRiskLevel} />
              </Box>
              
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="body2">Taxa de Bloqueio:</Typography>
                <Typography variant="h6" color={historicalBlockedPercent > 50 ? 'error.main' : 'success.main'}>
                  {historicalBlockedPercent}%
                </Typography>
              </Box>
              
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="body2">Comparações Encontradas:</Typography>
                <Typography variant="h6">{historicalComparisons.length}</Typography>
              </Box>
              
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="body2">Ambiente Mais Comum:</Typography>
                <Chip label={mostCommonEnvironment || 'N/A'} size="small" variant="outlined" />
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Paper>
  );
};

// Helpers
function calculateAverageRiskLevel(comparisons: any[]): RiskLevel {
  if (comparisons.length === 0) return 'BAIXO';
  
  const riskScores = { CRITICO: 4, ALTO: 3, MEDIO: 2, BAIXO: 1 };
  const avg = comparisons.reduce((sum, c) => sum + riskScores[c.riskLevel], 0) / comparisons.length;
  
  if (avg >= 3.5) return 'CRITICO';
  if (avg >= 2.5) return 'ALTO';
  if (avg >= 1.5) return 'MEDIO';
  return 'BAIXO';
}

function getMostCommon<T>(items: T[]): T | null {
  if (items.length === 0) return null;
  
  const counts = items.reduce((acc, item) => {
    acc[String(item)] = (acc[String(item)] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);
  
  return items.reduce((a, b) => counts[String(a)] > counts[String(b)] ? a : b);
}
