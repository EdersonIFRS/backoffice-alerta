// US#31 - Página de Métricas de Confiabilidade
import React, { useEffect, useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  CircularProgress,
  Alert,
  List,
  ListItem,
  ListItemText,
  Chip
} from '@mui/material';
import { TrendingUp, TrendingDown, TrendingFlat } from '@mui/icons-material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { metricsApi } from '../services/api';
import type { RiskMetrics } from '../types';

export const Metrics: React.FC = () => {
  const [metrics, setMetrics] = useState<RiskMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadMetrics = async () => {
    try {
      setLoading(true);
      const data = await metricsApi.get();
      setMetrics(data);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao carregar métricas');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMetrics();
  }, []);

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

  if (!metrics) {
    return <Alert severity="info">Nenhuma métrica disponível</Alert>;
  }

  const chartData = [
    { name: 'Acurácia', valor: (metrics.accuracyRate || 0) * 100 },
    { name: 'Falsos Positivos', valor: (metrics.falsePositiveRate || 0) * 100 },
    { name: 'Falsos Negativos', valor: (metrics.falseNegativeRate || 0) * 100 },
    { name: 'Incidentes Pós-Aprovação', valor: (metrics.incidentAfterApprovalRate || 0) * 100 }
  ];

  const getTrendIcon = (direction: string) => {
    switch (direction) {
      case 'UP':
        return <TrendingUp color="success" />;
      case 'DOWN':
        return <TrendingDown color="error" />;
      default:
        return <TrendingFlat color="disabled" />;
    }
  };

  const getTrendColor = (severity: string) => {
    switch (severity) {
      case 'POSITIVE':
        return 'success';
      case 'NEGATIVE':
        return 'error';
      default:
        return 'default';
    }
  };

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Métricas de Confiabilidade
      </Typography>

      {/* Score Geral */}
      <Paper sx={{ p: 3, mb: 3, bgcolor: 'primary.main', color: 'white' }}>
        <Typography variant="h6" gutterBottom>
          Score de Confiança do Sistema
        </Typography>
        <Typography variant="h2">
          {((metrics.systemConfidenceScore || 0) * 100).toFixed(1)}%
        </Typography>
      </Paper>

      {/* KPIs */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="text.secondary" gutterBottom>
                Taxa de Acurácia
              </Typography>
              <Typography variant="h4" color="success.main">
                {((metrics.accuracyRate || 0) * 100).toFixed(2)}%
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                {metrics.correctDecisions || 0} de {metrics.totalDecisions || 0} corretas
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
                {((metrics.falsePositiveRate || 0) * 100).toFixed(2)}%
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                {metrics.falsePositives || 0} ocorrências
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
                {((metrics.falseNegativeRate || 0) * 100).toFixed(2)}%
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                {metrics.falseNegatives || 0} ocorrências
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
                {((metrics.incidentAfterApprovalRate || 0) * 100).toFixed(2)}%
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                Total de decisões: {metrics.totalDecisions || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* Gráfico de Barras */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Métricas de Performance (%)
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="valor" fill="#1976d2" name="Percentual %" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Tendências */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Tendências Detectadas
            </Typography>
            {!metrics.trends || metrics.trends.length === 0 ? (
              <Typography color="text.secondary">Nenhuma tendência detectada</Typography>
            ) : (
              <List dense>
                {metrics.trends?.map((trend, index) => (
                  <ListItem key={index}>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          {getTrendIcon(trend.direction)}
                          <Typography variant="body2">{trend.indicator}</Typography>
                        </Box>
                      }
                      secondary={
                        <Box sx={{ mt: 0.5 }}>
                          <Typography variant="caption">{trend.description}</Typography>
                          <Box sx={{ mt: 0.5 }}>
                            <Chip
                              label={trend.severity}
                              size="small"
                              color={getTrendColor(trend.severity) as any}
                            />
                          </Box>
                        </Box>
                      }
                    />
                  </ListItem>
                ))}
              </List>
            )}
          </Paper>
        </Grid>
      </Grid>

      <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 2 }}>
        Período: {new Date(metrics.reportPeriodStart).toLocaleDateString('pt-BR')} até{' '}
        {new Date(metrics.reportPeriodEnd).toLocaleDateString('pt-BR')}
      </Typography>
    </Box>
  );
};
