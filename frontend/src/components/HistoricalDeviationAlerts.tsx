// US#42 - Alertas de Desvio Histórico
import React from 'react';
import { Alert, Box } from '@mui/material';
import { Warning, CheckCircle, TrendingUp } from '@mui/icons-material';
import type { DecisionHistoricalComparisonResponse } from '../types/historicalComparison';

interface Props {
  data: DecisionHistoricalComparisonResponse;
}

export const HistoricalDeviationAlerts: React.FC<Props> = ({ data }) => {
  const { currentContextSummary, historicalComparisons, executiveInsights } = data;

  const alerts: Array<{ severity: 'error' | 'warning' | 'success' | 'info', message: string, icon: React.ReactNode }> = [];

  // Alerta de padrão detectado
  if (executiveInsights.patternDetected) {
    alerts.push({
      severity: 'error',
      message: `⚠️ Padrão recorrente detectado: ${executiveInsights.patternDescription}`,
      icon: <Warning />
    });
  }

  // Comparar risco atual vs histórico
  const riskScores = { CRITICO: 4, ALTO: 3, MEDIO: 2, BAIXO: 1 };
  const currentRiskScore = riskScores[currentContextSummary.riskLevel];
  const avgHistoricalScore = historicalComparisons.length > 0
    ? historicalComparisons.reduce((sum, h) => sum + riskScores[h.riskLevel], 0) / historicalComparisons.length
    : 0;

  if (currentRiskScore > avgHistoricalScore + 1) {
    alerts.push({
      severity: 'warning',
      message: '📊 Risco atual significativamente acima da média histórica - Requer atenção especial',
      icon: <TrendingUp />
    });
  }

  // Taxa de bloqueio histórico
  const blockedCount = historicalComparisons.filter(h => h.decision === 'BLOQUEADO').length;
  const blockedRate = historicalComparisons.length > 0 ? blockedCount / historicalComparisons.length : 0;

  if (blockedRate >= 0.5) {
    alerts.push({
      severity: 'error',
      message: `🚫 Alto índice de bloqueios no histórico (${Math.round(blockedRate * 100)}%) - Mudanças similares têm sido bloqueadas frequentemente`,
      icon: <Warning />
    });
  }

  // Decisão alinhada
  if (!executiveInsights.patternDetected && currentRiskScore <= avgHistoricalScore + 0.5) {
    alerts.push({
      severity: 'success',
      message: '✅ Decisão alinhada ao histórico - Sem desvios significativos detectados',
      icon: <CheckCircle />
    });
  }

  // Poucos dados históricos
  if (historicalComparisons.length < 2) {
    alerts.push({
      severity: 'info',
      message: '📝 Poucos dados históricos similares encontrados - Análise comparativa limitada',
      icon: <Warning />
    });
  }

  if (alerts.length === 0) {
    return null;
  }

  return (
    <Box sx={{ mb: 3 }}>
      {alerts.map((alert, index) => (
        <Alert 
          key={index} 
          severity={alert.severity} 
          icon={alert.icon}
          sx={{ mb: 1 }}
        >
          {alert.message}
        </Alert>
      ))}
    </Box>
  );
};
