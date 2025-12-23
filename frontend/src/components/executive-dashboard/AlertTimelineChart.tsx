import React from 'react';
import { Card, CardContent, Typography, Box } from '@mui/material';
import { TrendingUp as TrendIcon } from '@mui/icons-material';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { AlertTrendPoint } from '../../services/executiveDashboard';

interface AlertTimelineChartProps {
  trends: AlertTrendPoint[];
}

/**
 * Gráfico de timeline de alertas
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
export const AlertTimelineChart: React.FC<AlertTimelineChartProps> = ({ trends }) => {
  // Formatar data para exibição
  const formattedData = trends.map(point => ({
    date: new Date(point.date).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' }),
    Enviados: point.sent,
    Bloqueados: point.skipped,
    Falhados: point.failed
  }));

  return (
    <Card>
      <CardContent>
        <Box display="flex" alignItems="center" mb={3}>
          <TrendIcon sx={{ mr: 1, color: 'primary.main' }} />
          <Typography variant="h6" fontWeight="bold">
            Tendência de Alertas (30 dias)
          </Typography>
        </Box>

        {trends.length === 0 ? (
          <Typography variant="body2" color="text.secondary" align="center" py={4}>
            Nenhum dado de tendência disponível
          </Typography>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={formattedData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis 
                dataKey="date" 
                tick={{ fontSize: 12 }}
                interval="preserveStartEnd"
              />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line 
                type="monotone" 
                dataKey="Enviados" 
                stroke="#4caf50" 
                strokeWidth={2}
                dot={{ r: 3 }}
              />
              <Line 
                type="monotone" 
                dataKey="Bloqueados" 
                stroke="#ff9800" 
                strokeWidth={2}
                dot={{ r: 3 }}
              />
              <Line 
                type="monotone" 
                dataKey="Falhados" 
                stroke="#f44336" 
                strokeWidth={2}
                dot={{ r: 3 }}
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  );
};
