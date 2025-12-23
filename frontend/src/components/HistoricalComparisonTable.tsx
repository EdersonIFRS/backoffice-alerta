// US#42 - Tabela de Decisões Históricas
import React from 'react';
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Chip,
  Box,
  LinearProgress
} from '@mui/material';
import type { 
  HistoricalDecisionComparisonResponse,
  RiskLevel,
  FinalDecision,
  Environment
} from '../types/historicalComparison';

interface Props {
  comparisons: HistoricalDecisionComparisonResponse[];
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
    APROVADO_COM_RESTRICOES: 'Aprovado c/ Restr.',
    BLOQUEADO: 'Bloqueado'
  } as const;

  return <Chip label={labels[decision]} color={colors[decision]} size="small" />;
};

const EnvironmentBadge: React.FC<{ env: Environment }> = ({ env }) => {
  const colors = {
    DEV: 'default',
    STAGING: 'primary',
    PRODUCTION: 'error'
  } as const;

  return <Chip label={env} color={colors[env]} size="small" variant="outlined" />;
};

const SimilarityScore: React.FC<{ score: number }> = ({ score }) => {
  const getColor = (score: number) => {
    if (score >= 80) return 'success';
    if (score >= 60) return 'warning';
    return 'error';
  };

  return (
    <Box sx={{ minWidth: 80 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <LinearProgress 
          variant="determinate" 
          value={score} 
          color={getColor(score)}
          sx={{ flexGrow: 1, height: 8, borderRadius: 1 }}
        />
        <Typography variant="body2" fontWeight="bold">
          {score}%
        </Typography>
      </Box>
    </Box>
  );
};

export const HistoricalComparisonTable: React.FC<Props> = ({ comparisons }) => {
  if (comparisons.length === 0) {
    return (
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography color="text.secondary" align="center">
          Nenhuma decisão histórica similar encontrada
        </Typography>
      </Paper>
    );
  }

  return (
    <Paper sx={{ mb: 3 }}>
      <Box sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Decisões Históricas Similares ({comparisons.length})
        </Typography>
      </Box>
      
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Pull Request</TableCell>
              <TableCell>Ambiente</TableCell>
              <TableCell>Risk Level</TableCell>
              <TableCell>Decisão</TableCell>
              <TableCell>Similaridade</TableCell>
              <TableCell>Resumo</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {comparisons.map((comparison) => (
              <TableRow key={comparison.pullRequestId} hover>
                <TableCell>
                  <Typography variant="body2" fontWeight="medium">
                    {comparison.pullRequestId}
                  </Typography>
                </TableCell>
                <TableCell>
                  <EnvironmentBadge env={comparison.environment} />
                </TableCell>
                <TableCell>
                  <RiskLevelBadge level={comparison.riskLevel} />
                </TableCell>
                <TableCell>
                  <DecisionBadge decision={comparison.decision} />
                </TableCell>
                <TableCell>
                  <SimilarityScore score={comparison.similarityScore} />
                </TableCell>
                <TableCell>
                  <Typography variant="body2" color="text.secondary">
                    {comparison.summary}
                  </Typography>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
};
