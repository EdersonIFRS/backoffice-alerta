// US#39 - Lista de recomendações executivas
import React from 'react';
import {
  Paper,
  Typography,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Box,
  Chip
} from '@mui/material';
import {
  CheckCircle as CheckIcon,
  PriorityHigh as PriorityIcon,
  Info as InfoIcon
} from '@mui/icons-material';

interface ExecutiveImpactRecommendationsProps {
  recommendation: string;
  riskLevel: string;
}

export const ExecutiveImpactRecommendations: React.FC<ExecutiveImpactRecommendationsProps> = ({ 
  recommendation, 
  riskLevel 
}) => {
  // Divide as recomendações por ponto e vírgula ou ponto
  const recommendations = recommendation
    .split(/[;.]/)
    .map(item => item.trim())
    .filter(item => item.length > 10); // Filtra itens muito curtos

  const getIcon = (text: string) => {
    const lowerText = text.toLowerCase();
    if (lowerText.includes('crítico') || lowerText.includes('urgente') || lowerText.includes('imediata')) {
      return <PriorityIcon color="error" />;
    }
    if (lowerText.includes('produção') || lowerText.includes('hotfix') || lowerText.includes('cascata')) {
      return <PriorityIcon color="warning" />;
    }
    return <CheckIcon color="success" />;
  };

  const getPriority = (text: string) => {
    const lowerText = text.toLowerCase();
    if (lowerText.includes('crítico') || lowerText.includes('urgente') || lowerText.includes('imediata')) {
      return { label: 'Alta Prioridade', color: 'error' as const };
    }
    if (lowerText.includes('produção') || lowerText.includes('hotfix') || lowerText.includes('cascata')) {
      return { label: 'Atenção', color: 'warning' as const };
    }
    return null;
  };

  return (
    <Paper sx={{ p: 3, mb: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
        <InfoIcon color="primary" />
        <Typography variant="h6">Recomendações</Typography>
        {riskLevel === 'CRITICAL' && (
          <Chip label="Ação Urgente" color="error" size="small" />
        )}
      </Box>

      <List>
        {recommendations.map((rec, index) => {
          const priority = getPriority(rec);
          return (
            <ListItem key={index} sx={{ alignItems: 'flex-start' }}>
              <ListItemIcon sx={{ mt: 1 }}>
                {getIcon(rec)}
              </ListItemIcon>
              <ListItemText
                primary={
                  <Box>
                    {rec}
                    {priority && (
                      <Chip
                        label={priority.label}
                        color={priority.color}
                        size="small"
                        sx={{ ml: 1 }}
                      />
                    )}
                  </Box>
                }
              />
            </ListItem>
          );
        })}
      </List>
    </Paper>
  );
};
