import React from 'react';
import { Card, CardContent, Typography, Box, List, ListItem, ListItemText, Chip } from '@mui/material';
import { NotificationsActive as ActiveIcon, PriorityHigh as CriticalIcon } from '@mui/icons-material';
import { ActiveAlertSummary } from '../../services/executiveDashboard';

interface ActiveAlertsPanelProps {
  alerts: ActiveAlertSummary[];
}

/**
 * Painel de alertas ativos (CRITICAL e WARNING)
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
export const ActiveAlertsPanel: React.FC<ActiveAlertsPanelProps> = ({ alerts }) => {
  const getSeverityColor = (severity: string): 'error' | 'warning' | 'info' => {
    if (severity === 'CRITICAL') return 'error';
    if (severity === 'WARNING') return 'warning';
    return 'info';
  };

  const getSeverityIcon = (severity: string) => {
    if (severity === 'CRITICAL') {
      return <CriticalIcon sx={{ color: 'error.main', fontSize: 20 }} />;
    }
    return <ActiveIcon sx={{ color: 'warning.main', fontSize: 20 }} />;
  };

  return (
    <Card>
      <CardContent>
        <Box display="flex" alignItems="center" mb={2}>
          <ActiveIcon sx={{ mr: 1, color: 'error.main' }} />
          <Typography variant="h6" fontWeight="bold">
            Alertas Ativos
          </Typography>
          <Chip 
            label={alerts.length} 
            color="error" 
            size="small" 
            sx={{ ml: 'auto' }}
          />
        </Box>

        {alerts.length === 0 ? (
          <Box textAlign="center" py={4}>
            <Typography variant="body2" color="text.secondary">
              ✅ Nenhum alerta ativo no momento
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Sistema operando dentro dos parâmetros esperados
            </Typography>
          </Box>
        ) : (
          <List sx={{ maxHeight: 400, overflow: 'auto' }}>
            {alerts.map((alert, index) => (
              <ListItem 
                key={index}
                sx={{ 
                  mb: 1,
                  bgcolor: alert.severity === 'CRITICAL' ? 'error.lighter' : 'warning.lighter',
                  borderRadius: 1,
                  borderLeft: 3,
                  borderColor: alert.severity === 'CRITICAL' ? 'error.main' : 'warning.main'
                }}
              >
                <Box display="flex" alignItems="flex-start" width="100%">
                  <Box mr={2} mt={0.5}>
                    {getSeverityIcon(alert.severity)}
                  </Box>
                  <ListItemText 
                    primary={
                      <Box display="flex" alignItems="center" gap={1} mb={0.5}>
                        <Chip 
                          label={alert.severity} 
                          color={getSeverityColor(alert.severity)}
                          size="small"
                        />
                        <Typography variant="caption" color="text.secondary">
                          {alert.alertType.replace(/_/g, ' ')}
                        </Typography>
                      </Box>
                    }
                    secondary={
                      <Typography variant="body2" color="text.primary">
                        {alert.message}
                      </Typography>
                    }
                  />
                </Box>
              </ListItem>
            ))}
          </List>
        )}
      </CardContent>
    </Card>
  );
};
