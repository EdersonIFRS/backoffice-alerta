// US#31 - Dashboard Executivo (Home)
import React, { useEffect, useState } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Card,
  CardContent,
  Alert,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Divider
} from '@mui/material';
import { Warning, TrendingUp, TrendingDown } from '@mui/icons-material';
import { dashboardApi } from '../services/api';
import { ConfidenceBadge, SeverityBadge } from '../components/StatusBadge';
import type { ExecutiveSummary, Environment } from '../types';

export const Dashboard: React.FC = () => {
  const [data, setData] = useState<ExecutiveSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [environment, setEnvironment] = useState<Environment>('GLOBAL');

  const loadData = async () => {
    try {
      setLoading(true);
      const summary = await dashboardApi.getExecutiveSummary(environment);
      setData(summary);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao carregar dashboard');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [environment]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  if (!data) {
    return <Alert severity="info">Nenhum dado disponível</Alert>;
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Dashboard Executivo
        </Typography>
        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel>Ambiente</InputLabel>
          <Select
            value={environment}
            label="Ambiente"
            onChange={(e) => setEnvironment(e.target.value as Environment)}
          >
            <MenuItem value="GLOBAL">Global</MenuItem>
            <MenuItem value="PRODUCTION_ONLY">Apenas Produção</MenuItem>
          </Select>
        </FormControl>
      </Box>

      {/* Status de Confiança */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Typography variant="h6">Status de Confiança do Sistema:</Typography>
          <ConfidenceBadge status={data.confidenceStatus} />
          <Typography variant="h5" sx={{ ml: 'auto' }}>
            {(data.systemConfidenceScore * 100).toFixed(1)}%
          </Typography>
        </Box>
      </Paper>

      {/* KRIs */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="text.secondary" gutterBottom>
                Taxa de Acurácia
              </Typography>
              <Typography variant="h4">
                {(data.accuracyRate * 100).toFixed(1)}%
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="text.secondary" gutterBottom>
                Falsos Positivos
              </Typography>
              <Typography variant="h4" color="warning.main">
                {(data.falsePositiveRate * 100).toFixed(1)}%
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="text.secondary" gutterBottom>
                Falsos Negativos
              </Typography>
              <Typography variant="h4" color="error.main">
                {(data.falseNegativeRate * 100).toFixed(1)}%
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="text.secondary" gutterBottom>
                Incidentes Pós-Aprovação
              </Typography>
              <Typography variant="h4" color="error.main">
                {(data.incidentAfterApprovalRate * 100).toFixed(1)}%
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* Alertas Ativos */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Warning /> Alertas Ativos ({data.activeAlerts?.length || 0})
            </Typography>
            <Divider sx={{ mb: 2 }} />
            {!data.activeAlerts || data.activeAlerts.length === 0 ? (
              <Typography color="text.secondary">Nenhum alerta ativo</Typography>
            ) : (
              data.activeAlerts.map((alert, index) => (
                <Alert key={index} severity={alert.severity.toLowerCase() as any} sx={{ mb: 1 }}>
                  <Typography variant="body2" fontWeight="bold">
                    {alert.category} - {alert.affectedArea}
                  </Typography>
                  <Typography variant="body2">{alert.message}</Typography>
                </Alert>
              ))
            )}
          </Paper>
        </Grid>

        {/* Top 5 Regras Problemáticas */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Top 5 Regras Problemáticas
            </Typography>
            <Divider sx={{ mb: 2 }} />
            {!data.topProblematicRules || data.topProblematicRules.length === 0 ? (
              <Typography color="text.secondary">Nenhuma regra problemática</Typography>
            ) : (
              data.topProblematicRules.map((rule, index) => (
                <Card key={rule.ruleId} sx={{ mb: 1, bgcolor: 'grey.50' }}>
                  <CardContent sx={{ py: 1.5 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                      <Typography variant="body2" fontWeight="bold">
                        #{index + 1} {rule.ruleName}
                      </Typography>
                      <SeverityBadge severity={rule.severity} />
                    </Box>
                    <Typography variant="caption" color="text.secondary">
                      {rule.incidentCount} incidentes • Último: {new Date(rule.lastIncidentDate).toLocaleDateString('pt-BR')}
                    </Typography>
                  </CardContent>
                </Card>
              ))
            )}
          </Paper>
        </Grid>
      </Grid>

      <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 2, textAlign: 'right' }}>
        Relatório gerado em: {new Date(data.reportGeneratedAt).toLocaleString('pt-BR')}
      </Typography>
    </Box>
  );
};
