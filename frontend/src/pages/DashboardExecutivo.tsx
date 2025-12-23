import React, { useEffect, useState } from 'react';
import { Container, Grid, Typography, Box, CircularProgress, Alert, Paper } from '@mui/material';
import { Dashboard as DashboardIcon } from '@mui/icons-material';
import { getExecutiveDashboard, ExecutiveDashboardResponse } from '../services/executiveDashboard';
import { ExecutiveSummaryCards } from '../components/executive-dashboard/ExecutiveSummaryCards';
import { ProjectRiskRanking } from '../components/executive-dashboard/ProjectRiskRanking';
import { RuleHotspots } from '../components/executive-dashboard/RuleHotspots';
import { AlertTimelineChart } from '../components/executive-dashboard/AlertTimelineChart';
import { ActiveAlertsPanel } from '../components/executive-dashboard/ActiveAlertsPanel';
import { useAuth } from '../context/AuthContext';

/**
 * Página do Dashboard Executivo
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 * 
 * Visão consolidada para ADMIN e RISK_MANAGER
 */
export const DashboardExecutivo: React.FC = () => {
  const { hasRole } = useAuth();
  const [dashboard, setDashboard] = useState<ExecutiveDashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Verificar permissões
  const canView = hasRole('ADMIN') || hasRole('RISK_MANAGER');

  useEffect(() => {
    if (!canView) {
      setLoading(false);
      return;
    }

    loadDashboard();
  }, [canView]);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getExecutiveDashboard();
      setDashboard(data);
    } catch (err: any) {
      console.error('Erro ao carregar dashboard:', err);
      setError(err.response?.data?.message || 'Erro ao carregar dashboard executivo');
    } finally {
      setLoading(false);
    }
  };

  // Verificação de permissão
  if (!canView) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Alert severity="error">
          Você não tem permissão para acessar o Dashboard Executivo.
          Apenas ADMIN e RISK_MANAGER podem visualizar esta página.
        </Alert>
      </Container>
    );
  }

  // Loading
  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
          <CircularProgress size={60} />
        </Box>
      </Container>
    );
  }

  // Error
  if (error) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      </Container>
    );
  }

  // Sem dados
  if (!dashboard) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Alert severity="info">
          Nenhum dado disponível para exibição
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* Header */}
      <Paper elevation={0} sx={{ p: 3, mb: 4, bgcolor: 'primary.main', color: 'white' }}>
        <Box display="flex" alignItems="center">
          <DashboardIcon sx={{ fontSize: 40, mr: 2 }} />
          <Box>
            <Typography variant="h4" fontWeight="bold">
              Dashboard Executivo
            </Typography>
            <Typography variant="body2" sx={{ opacity: 0.9 }}>
              Visão consolidada de alertas, risco e métricas de CI/CD
            </Typography>
          </Box>
        </Box>
      </Paper>

      {/* Summary Cards */}
      <Box mb={4}>
        <ExecutiveSummaryCards summary={dashboard.summary} />
      </Box>

      {/* Middle Section: Projects and Rules */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} md={6}>
          <ProjectRiskRanking projects={dashboard.topProjects} />
        </Grid>
        <Grid item xs={12} md={6}>
          <RuleHotspots rules={dashboard.topRules} />
        </Grid>
      </Grid>

      {/* Bottom Section: Timeline and Active Alerts */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={7}>
          <AlertTimelineChart trends={dashboard.alertTrends} />
        </Grid>
        <Grid item xs={12} md={5}>
          <ActiveAlertsPanel alerts={dashboard.activeAlerts} />
        </Grid>
      </Grid>

      {/* Alert Fatigue Warning */}
      {dashboard.summary.alertFatigueDetected && (
        <Alert severity="warning" sx={{ mt: 4 }}>
          <Typography variant="subtitle2" fontWeight="bold">
            ⚠️ Fadiga de Alertas Detectada
          </Typography>
          <Typography variant="body2">
            Sistema detectou alta taxa de avisos com poucos incidentes resolvidos.
            Revise as configurações de preferências de alertas (US#57/US#58).
          </Typography>
        </Alert>
      )}
    </Container>
  );
};
