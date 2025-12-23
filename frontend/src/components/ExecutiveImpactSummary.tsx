// US#39 - Card sumário executivo
import React from 'react';
import {
  Paper,
  Typography,
  Box,
  Chip,
  Alert
} from '@mui/material';
import {
  Error as CriticalIcon,
  Warning as WarningIcon,
  CheckCircle as SuccessIcon,
  TrendingUp as HighConfidenceIcon,
  TrendingFlat as MediumConfidenceIcon,
  TrendingDown as LowConfidenceIcon
} from '@mui/icons-material';
import type { ExecutiveImpactExplainResponse } from '../types/executiveImpact';

interface ExecutiveImpactSummaryProps {
  data: ExecutiveImpactExplainResponse;
  environment: string;
}

export const ExecutiveImpactSummary: React.FC<ExecutiveImpactSummaryProps> = ({ data, environment }) => {
  const getRiskColor = () => {
    switch (data.overallRiskLevel) {
      case 'CRITICAL': return 'error';
      case 'HIGH': return 'warning';
      case 'MEDIUM': return 'info';
      case 'LOW': return 'success';
      default: return 'default';
    }
  };

  const getRiskIcon = () => {
    switch (data.overallRiskLevel) {
      case 'CRITICAL': return <CriticalIcon />;
      case 'HIGH': return <WarningIcon />;
      case 'MEDIUM': return <WarningIcon />;
      case 'LOW': return <SuccessIcon />;
      default: return null;
    }
  };

  const getConfidenceIcon = () => {
    switch (data.confidenceLevel) {
      case 'HIGH': return <HighConfidenceIcon />;
      case 'MEDIUM': return <MediumConfidenceIcon />;
      case 'LOW': return <LowConfidenceIcon />;
      default: return null;
    }
  };

  const getConfidenceLabel = () => {
    switch (data.confidenceLevel) {
      case 'HIGH': return 'Alta Confiança';
      case 'MEDIUM': return 'Confiança Média';
      case 'LOW': return 'Baixa Confiança';
      default: return 'Desconhecido';
    }
  };

  const getRiskLabel = () => {
    switch (data.overallRiskLevel) {
      case 'CRITICAL': return 'CRÍTICO';
      case 'HIGH': return 'ALTO';
      case 'MEDIUM': return 'MÉDIO';
      case 'LOW': return 'BAIXO';
      default: return 'DESCONHECIDO';
    }
  };

  const showProductionAlert = environment === 'PRODUCTION';
  const showCascadeAlert = data.executiveSummary.headline.toLowerCase().includes('cascata');

  return (
    <Paper sx={{ p: 3, mb: 3 }}>
      {showProductionAlert && (
        <Alert severity="error" sx={{ mb: 2 }}>
          <strong>ATENÇÃO:</strong> Esta análise é para ambiente de PRODUÇÃO
        </Alert>
      )}

      {showCascadeAlert && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          <strong>IMPACTO EM CASCATA DETECTADO</strong>
        </Alert>
      )}

      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom sx={{ fontWeight: 'bold' }}>
          {data.executiveSummary.headline}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          Pull Request: {data.pullRequestId} | Gerado em: {new Date(data.generatedAt).toLocaleString('pt-BR')}
        </Typography>
      </Box>

      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
        <Chip
          icon={getRiskIcon()}
          label={`Risco ${getRiskLabel()}`}
          color={getRiskColor()}
          size="medium"
        />
        <Chip
          icon={getConfidenceIcon()}
          label={getConfidenceLabel()}
          variant="outlined"
          size="medium"
        />
      </Box>
    </Paper>
  );
};
