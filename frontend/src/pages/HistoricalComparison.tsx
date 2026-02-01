// US#42 - Página de Comparação Histórica
import React, { useState } from 'react';
import { Box, Typography, Alert, CircularProgress } from '@mui/material';
import { HistoricalComparisonForm } from '../components/HistoricalComparisonForm';
import { HistoricalComparisonSummary } from '../components/HistoricalComparisonSummary';
import { HistoricalDeviationAlerts } from '../components/HistoricalDeviationAlerts';
import { HistoricalComparisonTable } from '../components/HistoricalComparisonTable';
import { HistoricalInsights } from '../components/HistoricalInsights';
import { HistoricalComparisonActions } from '../components/HistoricalComparisonActions';
import { compareWithHistorical } from '../services/historicalComparison';
import type { 
  DecisionHistoricalComparisonRequest,
  DecisionHistoricalComparisonResponse 
} from '../types/historicalComparison';

export const HistoricalComparison: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [data, setData] = useState<DecisionHistoricalComparisonResponse | null>(null);

  const handleSubmit = async (request: DecisionHistoricalComparisonRequest) => {
    try {
      setLoading(true);
      setError('');
      const response = await compareWithHistorical(request);
      setData(response);
    } catch (err: any) {
      setError(
        err.response?.data?.message || 
        'Erro ao comparar com histórico. Verifique se o Pull Request existe.'
      );
      s
  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Comparação Histórica de Decisões
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Compare decisões de risco atuais com histórico similar para identificar padrões e desvios
        </Typography>
      </Box>

      <HistoricalComparisonForm 
        onSubmit={handleSubmit} 
        loading={loading}
      />

      {loading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {data && (
        <>
          <HistoricalDeviationAlerts data={data} />
          
          <HistoricalComparisonSummary data={data} />
          
          <HistoricalInsights insights={data.executiveInsights} />
          
          <HistoricalComparisonTable comparisons={data.historicalComparisons} />
          
          <HistoricalComparisonActions data={data} onReset={handleReset} />
        </>
      )}

      {!loading && !error && !data && (
        <Alert severity="info">
          Preencha o formulário acima para iniciar a comparação com o histórico. 
          Use Pull Requests do ambiente demo (PR-2024-001 a PR-2024-005) para testar.
        </Alert>
      )}
    </Box>
  );
};
