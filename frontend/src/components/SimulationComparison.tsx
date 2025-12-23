// US#31.1 - Componente de Comparação de Simulações
import React from 'react';
import { 
  Box, 
  Paper, 
  Typography, 
  Alert,
  Grid,
  Chip
} from '@mui/material';
import { 
  TrendingDown as ImprovedIcon,
  TrendingFlat as NoChangeIcon,
  TrendingUp as WorseIcon,
  CheckCircle as PositiveIcon
} from '@mui/icons-material';
import type { SimulationResponse } from '../types/simulation';

interface SimulationComparisonProps {
  comparison: SimulationResponse;
}

export const SimulationComparison: React.FC<SimulationComparisonProps> = ({ comparison }) => {
  const { baseline, simulated, changes } = comparison;

  const hasImprovements = 
    changes.decisionChanged || 
    changes.riskReduced || 
    changes.slaAvoided || 
    changes.lessNotifications ||
    changes.rulesRemoved.length > 0 ||
    changes.restrictionsRemoved.length > 0;

  const getRiskChangeIcon = () => {
    if (changes.riskReduced) return <ImprovedIcon color="success" />;
    if (baseline.riskLevel !== simulated.riskLevel) return <WorseIcon color="error" />;
    return <NoChangeIcon color="action" />;
  };

  const getRiskChangeText = () => {
    if (changes.riskReduced) {
      return `${baseline.riskLevel} → ${simulated.riskLevel} (Redução de risco!)`;
    }
    if (baseline.riskLevel !== simulated.riskLevel) {
      return `${baseline.riskLevel} → ${simulated.riskLevel} (Aumento de risco)`;
    }
    return `${baseline.riskLevel} (Sem alteração)`;
  };

  return (
    <Paper elevation={3} sx={{ p: 3, bgcolor: 'primary.light', color: 'primary.contrastText' }}>
      <Typography variant="h6" gutterBottom>
        📊 Análise Comparativa
      </Typography>

      {!hasImprovements && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Nenhuma melhoria significativa detectada com as opções de simulação selecionadas.
        </Alert>
      )}

      {hasImprovements && (
        <Alert severity="success" sx={{ mb: 2 }} icon={<PositiveIcon />}>
          <Typography variant="body2" fontWeight="bold">
            A simulação mostra melhorias potenciais!
          </Typography>
        </Alert>
      )}

      <Grid container spacing={2}>
        <Grid item xs={12}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            {getRiskChangeIcon()}
            <Typography variant="body1" fontWeight="bold">
              Alteração de Risco:
            </Typography>
          </Box>
          <Typography variant="body2" sx={{ ml: 4 }}>
            {getRiskChangeText()}
          </Typography>
        </Grid>

        {changes.decisionChanged && (
          <Grid item xs={12}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
              <PositiveIcon color="success" />
              <Typography variant="body1" fontWeight="bold">
                Mudança de Decisão:
              </Typography>
            </Box>
            <Typography variant="body2" sx={{ ml: 4 }}>
              {baseline.decision.replace(/_/g, ' ')} → {simulated.decision.replace(/_/g, ' ')}
            </Typography>
          </Grid>
        )}

        {changes.rulesRemoved.length > 0 && (
          <Grid item xs={12}>
            <Typography variant="body1" fontWeight="bold" gutterBottom>
              ✓ Regras Não Mais Impactadas ({changes.rulesRemoved.length}):
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, ml: 2 }}>
              {changes.rulesRemoved.map((rule, index) => (
                <Chip
                  key={index}
                  label={rule}
                  size="small"
                  color="success"
                  variant="outlined"
                />
              ))}
            </Box>
          </Grid>
        )}

        {changes.restrictionsRemoved.length > 0 && (
          <Grid item xs={12}>
            <Typography variant="body1" fontWeight="bold" gutterBottom>
              ✓ Restrições Removidas ({changes.restrictionsRemoved.length}):
            </Typography>
            <ul style={{ margin: '4px 0 0 0', paddingLeft: '40px' }}>
              {changes.restrictionsRemoved.map((restriction, index) => (
                <li key={index}>
                  <Typography variant="body2">{restriction}</Typography>
                </li>
              ))}
            </ul>
          </Grid>
        )}

        {changes.slaAvoided && (
          <Grid item xs={12}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <PositiveIcon color="success" />
              <Typography variant="body1" fontWeight="bold">
                SLA Evitado
              </Typography>
            </Box>
            <Typography variant="body2" sx={{ ml: 4 }}>
              Com as alterações simuladas, o SLA não seria acionado.
            </Typography>
          </Grid>
        )}

        {changes.lessNotifications && (
          <Grid item xs={12}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <PositiveIcon color="success" />
              <Typography variant="body1" fontWeight="bold">
                Menos Notificações
              </Typography>
            </Box>
            <Typography variant="body2" sx={{ ml: 4 }}>
              {baseline.notifiedTeams.length} times → {simulated.notifiedTeams.length} times 
              ({baseline.notifiedTeams.length - simulated.notifiedTeams.length} times a menos)
            </Typography>
          </Grid>
        )}

        <Grid item xs={12}>
          <Alert severity="info" sx={{ mt: 2 }}>
            <Typography variant="body2">
              💡 <strong>Lembre-se:</strong> Esta é apenas uma simulação. Nenhuma decisão real foi alterada ou registrada.
            </Typography>
          </Alert>
        </Grid>
      </Grid>
    </Paper>
  );
};
