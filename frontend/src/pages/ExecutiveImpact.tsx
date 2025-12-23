// US#39 - Página principal de Impacto Executivo
import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Alert,
  CircularProgress
} from '@mui/material';
import { Insights as InsightsIcon } from '@mui/icons-material';
import { ExecutiveImpactForm } from '../components/ExecutiveImpactForm';
import { ExecutiveImpactSummary } from '../components/ExecutiveImpactSummary';
import { ExecutiveImpactDetails } from '../components/ExecutiveImpactDetails';
import { ExecutiveImpactRecommendations } from '../components/ExecutiveImpactRecommendations';
import { ExecutiveImpactActions } from '../components/ExecutiveImpactActions';
import { executiveImpactApi } from '../services/executiveImpact';
import type { ExecutiveImpactExplainRequest, ExecutiveImpactExplainResponse } from '../types/executiveImpact';

export const ExecutiveImpact: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ExecutiveImpactExplainResponse | null>(null);
  const [currentRequest, setCurrentRequest] = useState<ExecutiveImpactExplainRequest | null>(null);

  const handleSubmit = async (request: ExecutiveImpactExplainRequest) => {
    setLoading(true);
    setError(null);
    setResult(null);
    setCurrentRequest(request);

    try {
      const response = await executiveImpactApi.generateExplanation(request);
      setResult(response);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao gerar análise executiva');
    } finally {
      setLoading(false);
    }
  };

  const handleNewAnalysis = () => {
    setResult(null);
    setError(null);
    setCurrentRequest(null);
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 4 }}>
        <InsightsIcon fontSize="large" color="primary" />
        <Box>
          <Typography variant="h4" component="h1">
            Impacto Executivo
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Visualização executiva do impacto sistêmico de mudanças
          </Typography>
        </Box>
      </Box>

      {!result && (
        <ExecutiveImpactForm onSubmit={handleSubmit} loading={loading} />
      )}

      {loading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 200 }}>
          <CircularProgress />
          <Typography sx={{ ml: 2 }}>Gerando análise executiva...</Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {result && currentRequest && (
        <>
          <ExecutiveImpactSummary 
            data={result} 
            environment={currentRequest.environment}
          />
          
          <ExecutiveImpactDetails 
            summary={result.executiveSummary}
          />
          
          <ExecutiveImpactRecommendations 
            recommendation={result.executiveSummary.recommendation}
            riskLevel={result.overallRiskLevel}
          />
          
          <ExecutiveImpactActions 
            data={result}
            onNewAnalysis={handleNewAnalysis}
          />
        </>
      )}
    </Container>
  );
};
