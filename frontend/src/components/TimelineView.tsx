// US#40 - Componente de visualização da timeline completa
import React from 'react';
import {
  Timeline
} from '@mui/lab';
import {
  Paper,
  Typography,
  Box,
  Chip,
  Alert
} from '@mui/material';
import {
  Warning as WarningIcon,
  CheckCircle as SuccessIcon
} from '@mui/icons-material';
import { TimelineEvent } from './TimelineEvent';
import type { ChangeTimelineResponse } from '../types/timeline';

interface TimelineViewProps {
  timeline: ChangeTimelineResponse;
}

export const TimelineView: React.FC<TimelineViewProps> = ({ timeline }) => {
  const getRiskColor = (riskLevel: string) => {
    switch (riskLevel) {
      case 'CRITICAL': return 'error';
      case 'HIGH': return 'warning';
      case 'MEDIUM': return 'info';
      case 'LOW': return 'success';
      default: return 'default';
    }
  };

  const getDecisionColor = (decision: string) => {
    switch (decision) {
      case 'APPROVED': return 'success';
      case 'REJECTED': return 'error';
      case 'APPROVED_WITH_CONDITIONS': return 'warning';
      case 'PENDING': return 'default';
      default: return 'default';
    }
  };

  const getDecisionLabel = (decision: string) => {
    switch (decision) {
      case 'APPROVED': return 'Aprovada';
      case 'REJECTED': return 'Rejeitada';
      case 'APPROVED_WITH_CONDITIONS': return 'Aprovada com Condições';
      case 'PENDING': return 'Pendente';
      default: return decision;
    }
  };

  return (
    <Box>
      {/* Cabeçalho com resumo */}
      <Paper sx={{ p: 3, mb: 3 }}>
        {timeline.requiresExecutiveAttention && (
          <Alert severity="error" icon={<WarningIcon />} sx={{ mb: 2 }}>
            <strong>Atenção Executiva Requerida</strong>
            <Typography variant="body2">
              Esta mudança requer atenção especial devido a risco crítico, rejeição ou SLA vencido.
            </Typography>
          </Alert>
        )}

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
          <Typography variant="h5" component="h2">
            Pull Request: {timeline.pullRequestId}
          </Typography>
          {!timeline.requiresExecutiveAttention && (
            <SuccessIcon color="success" />
          )}
        </Box>

        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
          <Chip 
            label={`Decisão: ${getDecisionLabel(timeline.finalDecision)}`}
            color={getDecisionColor(timeline.finalDecision)}
          />
          <Chip 
            label={`Risco: ${timeline.overallRiskLevel}`}
            color={getRiskColor(timeline.overallRiskLevel)}
          />
          <Chip 
            label={`Ambiente: ${timeline.environment}`}
            variant="outlined"
          />
          <Chip 
            label={`${timeline.events.length} eventos`}
            variant="outlined"
          />
        </Box>
      </Paper>

      {/* Timeline de eventos */}
      {timeline.events.length === 0 ? (
        <Alert severity="info">
          Nenhum evento encontrado para este Pull Request.
        </Alert>
      ) : (
        <Timeline position="right">
          {timeline.events.map((event) => (
            <TimelineEvent key={event.id} event={event} />
          ))}
        </Timeline>
      )}
    </Box>
  );
};
