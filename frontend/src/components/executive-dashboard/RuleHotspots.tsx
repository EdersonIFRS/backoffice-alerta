import React from 'react';
import { Card, CardContent, Typography, Box, List, ListItem, ListItemText, Chip, Badge } from '@mui/material';
import { LocalFireDepartment as HotspotIcon } from '@mui/icons-material';
import { RuleRiskSummary } from '../../services/executiveDashboard';

interface RuleHotspotsProps {
  rules: RuleRiskSummary[];
}

/**
 * Regras de negócio com mais bloqueios (hotspots)
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
export const RuleHotspots: React.FC<RuleHotspotsProps> = ({ rules }) => {
  const getSeverityColor = (blockCount: number): 'error' | 'warning' | 'info' => {
    if (blockCount >= 10) return 'error';
    if (blockCount >= 5) return 'warning';
    return 'info';
  };

  return (
    <Card>
      <CardContent>
        <Box display="flex" alignItems="center" mb={2}>
          <HotspotIcon sx={{ mr: 1, color: 'warning.main' }} />
          <Typography variant="h6" fontWeight="bold">
            Regras Mais Bloqueadoras
          </Typography>
        </Box>

        {rules.length === 0 ? (
          <Typography variant="body2" color="text.secondary" align="center" py={4}>
            Nenhuma regra bloqueadora detectada
          </Typography>
        ) : (
          <List>
            {rules.map((rule, index) => (
              <ListItem 
                key={rule.ruleId}
                sx={{ 
                  mb: 1,
                  bgcolor: index === 0 ? 'warning.lighter' : 'background.default',
                  borderRadius: 1,
                  borderLeft: index === 0 ? 3 : 0,
                  borderColor: 'warning.main'
                }}
              >
                <ListItemText 
                  primary={
                    <Box display="flex" alignItems="center" gap={1}>
                      <Typography variant="subtitle2" fontWeight="bold">
                        {rule.ruleName}
                      </Typography>
                      <Chip 
                        label={`${rule.blockCount} bloqueios`}
                        color={getSeverityColor(rule.blockCount)}
                        size="small"
                      />
                    </Box>
                  }
                  secondary={
                    <Typography variant="caption" color="text.secondary">
                      {rule.incidentCount} incidentes registrados
                    </Typography>
                  }
                />
              </ListItem>
            ))}
          </List>
        )}
      </CardContent>
    </Card>
  );
};
