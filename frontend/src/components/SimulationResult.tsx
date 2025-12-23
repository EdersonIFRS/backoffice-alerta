// US#31.1 - Componente de Resultado de Simulação
import React from 'react';
import { 
  Box, 
  Paper, 
  Typography, 
  Chip,
  Grid,
  Divider,
  Alert
} from '@mui/material';
import { 
  CheckCircle as ApprovedIcon,
  Cancel as RejectedIcon,
  Warning as ReviewIcon,
  Shield as ConditionalIcon
} from '@mui/icons-material';
import type { SimulationRiskResult } from '../types/simulation';

interface SimulationResultProps {
  title: string;
  result: SimulationRiskResult;
  isBaseline?: boolean;
}

export const SimulationResult: React.FC<SimulationResultProps> = ({ 
  title, 
  result,
  isBaseline = false 
}) => {
  const getDecisionIcon = (decision: string) => {
    switch (decision) {
      case 'APPROVED':
        return <ApprovedIcon color="success" />;
      case 'REJECTED':
        return <RejectedIcon color="error" />;
      case 'REQUIRES_REVIEW':
        return <ReviewIcon color="warning" />;
      case 'CONDITIONAL_APPROVAL':
        return <ConditionalIcon color="info" />;
      default:
        return null;
    }
  };

  const getRiskColor = (riskLevel: string): "error" | "warning" | "info" | "success" => {
    switch (riskLevel) {
      case 'CRITICAL':
        return 'error';
      case 'HIGH':
        return 'warning';
      case 'MEDIUM':
        return 'info';
      case 'LOW':
        return 'success';
      default:
        return 'info';
    }
  };

  const getDecisionColor = (decision: string): "success" | "error" | "warning" | "info" => {
    switch (decision) {
      case 'APPROVED':
        return 'success';
      case 'REJECTED':
        return 'error';
      case 'REQUIRES_REVIEW':
        return 'warning';
      case 'CONDITIONAL_APPROVAL':
        return 'info';
      default:
        return 'info';
    }
  };

  return (
    <Paper 
      elevation={isBaseline ? 1 : 3} 
      sx={{ 
        p: 3, 
        bgcolor: isBaseline ? 'grey.50' : 'background.paper',
        border: isBaseline ? '1px solid' : '2px solid',
        borderColor: isBaseline ? 'grey.300' : 'primary.main'
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
        {getDecisionIcon(result.decision)}
        <Typography variant="h6" sx={{ ml: 1 }}>
          {title}
        </Typography>
      </Box>

      <Grid container spacing={2}>
        <Grid item xs={12} sm={6}>
          <Typography variant="body2" color="text.secondary">
            Decisão Final
          </Typography>
          <Chip
            label={result.decision.replace(/_/g, ' ')}
            color={getDecisionColor(result.decision)}
            sx={{ mt: 1 }}
          />
        </Grid>

        <Grid item xs={12} sm={6}>
          <Typography variant="body2" color="text.secondary">
            Nível de Risco
          </Typography>
          <Chip
            label={result.riskLevel}
            color={getRiskColor(result.riskLevel)}
            sx={{ mt: 1 }}
          />
        </Grid>

        <Grid item xs={12}>
          <Divider sx={{ my: 1 }} />
        </Grid>

        <Grid item xs={12} sm={4}>
          <Typography variant="body2" color="text.secondary">
            Regras Impactadas
          </Typography>
          <Typography variant="h6">
            {result.impactedRules.length}
          </Typography>
        </Grid>

        <Grid item xs={12} sm={4}>
          <Typography variant="body2" color="text.secondary">
            SLA Acionado
          </Typography>
          <Typography variant="h6">
            {result.slaTriggered ? '✓ Sim' : '✗ Não'}
          </Typography>
        </Grid>

        <Grid item xs={12} sm={4}>
          <Typography variant="body2" color="text.secondary">
            Times Notificados
          </Typography>
          <Typography variant="h6">
            {result.notifiedTeams.length}
          </Typography>
        </Grid>

        {result.restrictions.length > 0 && (
          <Grid item xs={12}>
            <Alert severity="warning" sx={{ mt: 1 }}>
              <Typography variant="body2" fontWeight="bold">
                Restrições Aplicadas:
              </Typography>
              <ul style={{ margin: '8px 0 0 0', paddingLeft: '20px' }}>
                {result.restrictions.map((restriction, index) => (
                  <li key={index}>
                    <Typography variant="body2">{restriction}</Typography>
                  </li>
                ))}
              </ul>
            </Alert>
          </Grid>
        )}

        {result.impactedRules.length > 0 && (
          <Grid item xs={12}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Regras de Negócio Impactadas:
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 1 }}>
              {result.impactedRules.map((rule, index) => (
                <Chip
                  key={index}
                  label={rule}
                  size="small"
                  variant="outlined"
                />
              ))}
            </Box>
          </Grid>
        )}
      </Grid>
    </Paper>
  );
};
