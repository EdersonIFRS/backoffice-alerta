// US#42 - Insights Executivos
import React from 'react';
import { Paper, Typography, Box, Alert } from '@mui/material';
import { Lightbulb, CheckCircle, Warning } from '@mui/icons-material';
import type { ExecutiveHistoricalInsightResponse } from '../types/historicalComparison';

interface Props {
  insights: ExecutiveHistoricalInsightResponse;
}

export const HistoricalInsights: React.FC<Props> = ({ insights }) => {
  return (
    <Paper sx={{ p: 3, mb: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
        <Lightbulb color="primary" />
        <Typography variant="h6">
          Insights Executivos
        </Typography>
      </Box>

      {insights.patternDetected ? (
        <>
          <Alert 
            severity="warning" 
            icon={<Warning />}
            sx={{ mb: 2 }}
          >
            <Typography variant="subtitle2" gutterBottom>
              Padrão Detectado
            </Typography>
            <Typography variant="body2">
              {insights.patternDescription}
            </Typography>
          </Alert>

          <Paper variant="outlined" sx={{ p: 2, bgcolor: 'warning.50' }}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              📋 Recomendações
            </Typography>
            <Typography variant="body2">
              {insights.recommendation}
            </Typography>
          </Paper>
        </>
      ) : (
        <Alert 
          severity="success" 
          icon={<CheckCircle />}
        >
          <Typography variant="subtitle2" gutterBottom>
            Sem Padrões Críticos
          </Typography>
          <Typography variant="body2">
            {insights.patternDescription}
          </Typography>
          <Typography variant="body2" sx={{ mt: 1 }}>
            <strong>Recomendação:</strong> {insights.recommendation}
          </Typography>
        </Alert>
      )}

      <Box sx={{ mt: 2, p: 2, bgcolor: 'background.default', borderRadius: 1 }}>
        <Typography variant="caption" color="text.secondary">
          💡 <strong>Nota:</strong> Esta análise é determinística e baseada apenas em dados históricos. 
          Não utiliza IA nem realiza previsões. Use como complemento à análise humana.
        </Typography>
      </Box>
    </Paper>
  );
};
