import React from 'react';
import { Grid, Card, CardContent, Typography, Box, Chip } from '@mui/material';
import { 
  TrendingUp as TrendingUpIcon,
  Block as BlockIcon,
  Warning as WarningIcon,
  NotificationImportant as AlertIcon
} from '@mui/icons-material';
import { ExecutiveDashboardSummary } from '../../services/executiveDashboard';

interface ExecutiveSummaryCardsProps {
  summary: ExecutiveDashboardSummary;
}

/**
 * Cards de resumo executivo
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
export const ExecutiveSummaryCards: React.FC<ExecutiveSummaryCardsProps> = ({ summary }) => {
  const cards = [
    {
      title: 'PRs Analisados',
      value: summary.totalGates.toLocaleString(),
      icon: <TrendingUpIcon sx={{ fontSize: 40, color: 'primary.main' }} />,
      color: 'primary.light',
      tooltip: 'Total de Pull Requests processados pelo gate de risco'
    },
    {
      title: 'Taxa de Bloqueio',
      value: `${summary.blockRate.toFixed(1)}%`,
      icon: <BlockIcon sx={{ fontSize: 40, color: 'error.main' }} />,
      color: 'error.light',
      tooltip: 'Percentual de PRs bloqueados por violação de regras'
    },
    {
      title: 'Taxa de Avisos',
      value: `${summary.warningRate.toFixed(1)}%`,
      icon: <WarningIcon sx={{ fontSize: 40, color: 'warning.main' }} />,
      color: 'warning.light',
      tooltip: 'Percentual de PRs com avisos não bloqueantes'
    },
    {
      title: 'Alertas Críticos (7 dias)',
      value: summary.criticalAlertsLast7Days.toString(),
      icon: <AlertIcon sx={{ fontSize: 40, color: 'error.dark' }} />,
      color: 'error.dark',
      tooltip: 'Alertas de severidade crítica enviados na última semana'
    }
  ];

  return (
    <Grid container spacing={3}>
      {cards.map((card, index) => (
        <Grid item xs={12} sm={6} md={3} key={index}>
          <Card 
            sx={{ 
              height: '100%',
              transition: 'transform 0.2s, box-shadow 0.2s',
              '&:hover': {
                transform: 'translateY(-4px)',
                boxShadow: 4
              }
            }}
          >
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                <Box>
                  <Typography variant="caption" color="text.secondary" gutterBottom>
                    {card.title}
                  </Typography>
                  <Typography variant="h4" fontWeight="bold">
                    {card.value}
                  </Typography>
                </Box>
                <Box 
                  sx={{ 
                    backgroundColor: card.color, 
                    borderRadius: 2, 
                    p: 1,
                    opacity: 0.15 
                  }}
                >
                  {card.icon}
                </Box>
              </Box>
              
              {index === 3 && summary.alertFatigueDetected && (
                <Chip 
                  label="Fadiga de Alertas Detectada" 
                  color="warning" 
                  size="small"
                  sx={{ mt: 1 }}
                />
              )}
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};
