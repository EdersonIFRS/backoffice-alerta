import React, { useState } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Alert,
  Box,
  Typography,
  Grid,
  Chip,
  Paper,
  SelectChangeEvent
} from '@mui/material';
import {
  PlayArrow as SimulateIcon,
  CheckCircle as SuccessIcon,
  Block as BlockIcon,
  Science as ScienceIcon
} from '@mui/icons-material';
import {
  AlertType,
  AlertSeverity,
  NotificationChannel,
  AlertSimulationResult,
  PreferenceSource
} from '../../types/alertPreferences';
import { getEffectivePreferences } from '../../services/alertPreferences';

interface AlertPreferenceSimulatorProps {
  projects: Array<{ id: string; name: string }>;
  businessRules: Array<{ id: string; name: string }>;
}

const SEVERITY_COLORS: Record<AlertSeverity, string> = {
  INFO: '#2196f3',
  WARNING: '#ff9800',
  CRITICAL: '#f44336'
};

const CHANNEL_ICONS: Record<NotificationChannel, string> = {
  SLACK: '💬',
  TEAMS: '👥',
  EMAIL: '📧',
  SMS: '📱'
};

const SOURCE_CONFIG = {
  RULE: { label: 'Regra de Negócio', color: '#9c27b0' },
  PROJECT: { label: 'Projeto', color: '#2196f3' },
  DEFAULT: { label: 'Padrão do Sistema', color: '#757575' }
};

export const AlertPreferenceSimulator: React.FC<AlertPreferenceSimulatorProps> = ({
  projects,
  businessRules
}) => {
  const [projectId, setProjectId] = useState<string>('');
  const [businessRuleId, setBusinessRuleId] = useState<string>('');
  const [alertType, setAlertType] = useState<AlertType>(AlertType.RULE_EXECUTION);
  const [severity, setSeverity] = useState<AlertSeverity>(AlertSeverity.INFO);
  const [channel, setChannel] = useState<NotificationChannel>(NotificationChannel.SLACK);
  const [result, setResult] = useState<AlertSimulationResult | null>(null);
  const [simulating, setSimulating] = useState(false);

  const handleSimulate = async () => {
    if (!projectId && !businessRuleId) {
      alert('Selecione ao menos um projeto ou regra de negócio');
      return;
    }

    setSimulating(true);
    try {
      // Busca a preferência efetiva
      const effective = await getEffectivePreferences(
        projectId || undefined,
        businessRuleId || undefined
      );

      // Simula a lógica de shouldSendAlert do backend
      let wouldSend = true;
      let reason = '';

      // 1. Verifica severidade mínima
      const severityOrder = { INFO: 1, WARNING: 2, CRITICAL: 3 };
      if (severityOrder[severity] < severityOrder[effective.minimumSeverity]) {
        wouldSend = false;
        reason = `Severidade ${severity} está abaixo do mínimo configurado (${effective.minimumSeverity})`;
      }

      // 2. Verifica tipo de alerta permitido
      if (wouldSend && effective.allowedAlertTypes.length > 0 && !effective.allowedAlertTypes.includes(alertType)) {
        wouldSend = false;
        reason = `Tipo de alerta ${alertType} não está na lista de permitidos`;
      }

      // 3. Verifica canal ativo
      if (wouldSend && !effective.channels.includes(channel)) {
        wouldSend = false;
        reason = `Canal ${channel} não está habilitado`;
      }

      // 4. Se passou todas as verificações
      if (wouldSend) {
        reason = `Alerta atende todos os critérios da preferência (${SOURCE_CONFIG[effective.source].label})`;
      }

      setResult({
        wouldSend,
        channel,
        reason,
        effectivePreference: effective
      });
    } catch (error) {
      console.error('Erro ao simular:', error);
      alert('Erro ao executar simulação');
    } finally {
      setSimulating(false);
    }
  };

  return (
    <Card elevation={3}>
      <CardHeader
        avatar={<ScienceIcon color="info" />}
        title="Simulação de Alerta (Dry-run)"
        subheader="Teste se um alerta seria enviado ou bloqueado pelas preferências configuradas"
      />
      <CardContent>
        <Grid container spacing={3}>
          {/* Projeto */}
          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Projeto (opcional)</InputLabel>
              <Select
                value={projectId}
                onChange={(e: SelectChangeEvent) => setProjectId(e.target.value)}
                disabled={simulating}
              >
                <MenuItem value="">
                  <em>Nenhum</em>
                </MenuItem>
                {projects.map((project) => (
                  <MenuItem key={project.id} value={project.id}>
                    {project.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* Regra de Negócio */}
          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Regra de Negócio (opcional)</InputLabel>
              <Select
                value={businessRuleId}
                onChange={(e: SelectChangeEvent) => setBusinessRuleId(e.target.value)}
                disabled={simulating}
              >
                <MenuItem value="">
                  <em>Nenhuma</em>
                </MenuItem>
                {businessRules.map((rule) => (
                  <MenuItem key={rule.id} value={rule.id}>
                    {rule.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* Tipo de Alerta */}
          <Grid item xs={12} md={4}>
            <FormControl fullWidth>
              <InputLabel>Tipo de Alerta</InputLabel>
              <Select
                value={alertType}
                onChange={(e: SelectChangeEvent) => setAlertType(e.target.value as AlertType)}
                disabled={simulating}
              >
                {Object.values(AlertType).map((type) => (
                  <MenuItem key={type} value={type}>
                    {type.replace(/_/g, ' ')}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* Severidade */}
          <Grid item xs={12} md={4}>
            <FormControl fullWidth>
              <InputLabel>Severidade</InputLabel>
              <Select
                value={severity}
                onChange={(e: SelectChangeEvent) => setSeverity(e.target.value as AlertSeverity)}
                disabled={simulating}
              >
                {Object.values(AlertSeverity).map((sev) => (
                  <MenuItem key={sev} value={sev}>
                    <Chip
                      label={sev}
                      size="small"
                      style={{ backgroundColor: SEVERITY_COLORS[sev], color: 'white' }}
                    />
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* Canal */}
          <Grid item xs={12} md={4}>
            <FormControl fullWidth>
              <InputLabel>Canal</InputLabel>
              <Select
                value={channel}
                onChange={(e: SelectChangeEvent) => setChannel(e.target.value as NotificationChannel)}
                disabled={simulating}
              >
                {Object.values(NotificationChannel).map((ch) => (
                  <MenuItem key={ch} value={ch}>
                    {CHANNEL_ICONS[ch]} {ch}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* Botão Simular */}
          <Grid item xs={12}>
            <Button
              variant="contained"
              color="info"
              size="large"
              startIcon={<SimulateIcon />}
              onClick={handleSimulate}
              disabled={simulating || (!projectId && !businessRuleId)}
              fullWidth
            >
              {simulating ? 'Simulando...' : 'Executar Simulação'}
            </Button>
          </Grid>

          {/* Resultado */}
          {result && (
            <Grid item xs={12}>
              <Paper
                elevation={4}
                sx={{
                  p: 3,
                  backgroundColor: result.wouldSend ? '#e8f5e9' : '#ffebee',
                  border: `2px solid ${result.wouldSend ? '#4caf50' : '#f44336'}`
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                  {result.wouldSend ? (
                    <SuccessIcon fontSize="large" sx={{ color: '#4caf50' }} />
                  ) : (
                    <BlockIcon fontSize="large" sx={{ color: '#f44336' }} />
                  )}
                  <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                    {result.wouldSend ? '✅ Alerta SERIA enviado' : '❌ Alerta BLOQUEADO'}
                  </Typography>
                </Box>

                <Alert severity={result.wouldSend ? 'success' : 'warning'} sx={{ mb: 2 }}>
                  <Typography variant="body2">
                    <strong>Razão:</strong> {result.reason}
                  </Typography>
                </Alert>

                <Typography variant="subtitle2" gutterBottom>
                  Preferência aplicada:
                </Typography>
                <Chip
                  label={SOURCE_CONFIG[result.effectivePreference.source].label}
                  size="small"
                  style={{
                    backgroundColor: SOURCE_CONFIG[result.effectivePreference.source].color,
                    color: 'white',
                    marginRight: '8px'
                  }}
                />
                <Chip
                  label={`Canal: ${CHANNEL_ICONS[result.channel]} ${result.channel}`}
                  size="small"
                  color={result.wouldSend ? 'success' : 'default'}
                />

                <Box sx={{ mt: 2 }}>
                  <Typography variant="caption" color="textSecondary">
                    📋 Severidade mínima: {result.effectivePreference.minimumSeverity} |
                    🕐 Janela: {result.effectivePreference.deliveryWindow} |
                    📡 Canais: {result.effectivePreference.channels.join(', ')}
                  </Typography>
                </Box>
              </Paper>
            </Grid>
          )}

          <Grid item xs={12}>
            <Alert severity="info" icon={<ScienceIcon />}>
              <strong>Simulação apenas visual:</strong> Nenhum alerta real será enviado. Esta ferramenta apenas
              verifica se as preferências configuradas permitiriam o envio.
            </Alert>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};
