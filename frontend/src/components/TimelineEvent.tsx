// US#40 - Componente de evento individual na timeline
import React from 'react';
import {
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineOppositeContent
} from '@mui/lab';
import {
  Card,
  CardContent,
  Typography,
  Chip,
  Box,
  Collapse,
  IconButton
} from '@mui/material';
import {
  CheckCircle as DecisionIcon,
  Assignment as AuditIcon,
  Notifications as NotificationIcon,
  Timer as SlaIcon,
  Warning as EscalatedIcon,
  Feedback as FeedbackIcon,
  Analytics as AnalysisIcon,
  Insights as ExecutiveIcon,
  Science as SimulationIcon,
  Article as ProposalIcon,
  ExpandMore as ExpandMoreIcon
} from '@mui/icons-material';
import type { TimelineEventResponse, TimelineEventType } from '../types/timeline';

interface TimelineEventProps {
  event: TimelineEventResponse;
}

export const TimelineEvent: React.FC<TimelineEventProps> = ({ event }) => {
  const [expanded, setExpanded] = React.useState(false);

  const getIcon = (type: TimelineEventType) => {
    switch (type) {
      case 'PROPOSAL': return <ProposalIcon />;
      case 'IMPACT_ANALYSIS': return <AnalysisIcon />;
      case 'EXECUTIVE_EXPLANATION': return <ExecutiveIcon />;
      case 'SIMULATION': return <SimulationIcon />;
      case 'DECISION': return <DecisionIcon />;
      case 'AUDIT': return <AuditIcon />;
      case 'NOTIFICATION': return <NotificationIcon />;
      case 'SLA_CREATED': return <SlaIcon />;
      case 'SLA_ESCALATED': return <EscalatedIcon />;
      case 'FEEDBACK': return <FeedbackIcon />;
      default: return <AuditIcon />;
    }
  };

  const getColor = (severity: string) => {
    switch (severity) {
      case 'CRITICAL': return 'error';
      case 'WARNING': return 'warning';
      case 'INFO': return 'info';
      default: return 'grey';
    }
  };

  const getSeverityLabel = (severity: string) => {
    switch (severity) {
      case 'CRITICAL': return 'Crítico';
      case 'WARNING': return 'Atenção';
      case 'INFO': return 'Informativo';
      default: return severity;
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <TimelineItem>
      <TimelineOppositeContent color="text.secondary" sx={{ flex: 0.2 }}>
        <Typography variant="caption">{formatDate(event.createdAt)}</Typography>
        <Typography variant="caption" display="block">
          {event.actor}
        </Typography>
      </TimelineOppositeContent>

      <TimelineSeparator>
        <TimelineDot color={getColor(event.severity)}>
          {getIcon(event.eventType)}
        </TimelineDot>
        <TimelineConnector />
      </TimelineSeparator>

      <TimelineContent>
        <Card 
          sx={{ 
            mb: 2,
            border: event.severity === 'CRITICAL' ? 2 : 0,
            borderColor: 'error.main'
          }}
        >
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <Box sx={{ flex: 1 }}>
                <Typography variant="h6" component="div">
                  {event.title}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  {event.description}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', gap: 1, flexDirection: 'column', alignItems: 'flex-end' }}>
                <Chip 
                  label={getSeverityLabel(event.severity)} 
                  color={getColor(event.severity)} 
                  size="small" 
                />
                {Object.keys(event.metadata).length > 0 && (
                  <IconButton
                    size="small"
                    onClick={() => setExpanded(!expanded)}
                    sx={{
                      transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)',
                      transition: 'transform 0.3s'
                    }}
                  >
                    <ExpandMoreIcon />
                  </IconButton>
                )}
              </Box>
            </Box>

            <Collapse in={expanded}>
              <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Metadados:
                </Typography>
                {Object.entries(event.metadata).map(([key, value]) => (
                  <Typography key={key} variant="body2" color="text.secondary">
                    <strong>{key}:</strong> {value}
                  </Typography>
                ))}
              </Box>
            </Collapse>
          </CardContent>
        </Card>
      </TimelineContent>
    </TimelineItem>
  );
};
