// US#39 - Detalhes da análise executiva
import React from 'react';
import {
  Paper,
  Typography,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Box,
  Chip,
  List,
  ListItem,
  ListItemIcon,
  ListItemText
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Business as BusinessIcon,
  History as HistoryIcon,
  Assessment as AssessmentIcon,
  Group as GroupIcon
} from '@mui/icons-material';
import type { ExecutiveSummary } from '../types/executiveImpact';

interface ExecutiveImpactDetailsProps {
  summary: ExecutiveSummary;
}

export const ExecutiveImpactDetails: React.FC<ExecutiveImpactDetailsProps> = ({ summary }) => {
  return (
    <Paper sx={{ mb: 3 }}>
      <Accordion defaultExpanded>
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <BusinessIcon color="primary" />
            <Typography variant="h6">Impacto no Negócio</Typography>
          </Box>
        </AccordionSummary>
        <AccordionDetails>
          <Typography paragraph>
            {summary.businessImpact}
          </Typography>
        </AccordionDetails>
      </Accordion>

      <Accordion>
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <GroupIcon color="primary" />
            <Typography variant="h6">Áreas Afetadas</Typography>
          </Box>
        </AccordionSummary>
        <AccordionDetails>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {summary.areasAffected.map((area, index) => (
              <Chip
                key={index}
                label={area}
                color="primary"
                variant="outlined"
              />
            ))}
          </Box>
        </AccordionDetails>
      </Accordion>

      {summary.historicalContext && (
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <HistoryIcon color="primary" />
              <Typography variant="h6">Contexto Histórico</Typography>
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <Typography paragraph>
              {summary.historicalContext}
            </Typography>
          </AccordionDetails>
        </Accordion>
      )}

      <Accordion>
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <AssessmentIcon color="primary" />
            <Typography variant="h6">Interpretação de Risco</Typography>
          </Box>
        </AccordionSummary>
        <AccordionDetails>
          <List>
            {summary.riskInterpretation.split(/[;.]/).filter(item => item.trim()).map((item, index) => (
              <ListItem key={index}>
                <ListItemIcon>
                  {item.toLowerCase().includes('financeiro') && '💰'}
                  {item.toLowerCase().includes('operacional') && '⚙️'}
                  {item.toLowerCase().includes('reputacional') && '🏆'}
                  {!item.toLowerCase().match(/(financeiro|operacional|reputacional)/) && '•'}
                </ListItemIcon>
                <ListItemText primary={item.trim()} />
              </ListItem>
            ))}
          </List>
        </AccordionDetails>
      </Accordion>
    </Paper>
  );
};
