import React, { useEffect, useState } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  Chip,
  Box,
  Typography,
  CircularProgress,
  Alert,
  Grid,
  Divider
} from '@mui/material';
import {
  CheckCircle as CheckIcon,
  AccountTree as HierarchyIcon,
  NotificationsActive as NotificationIcon
} from '@mui/icons-material';
import {
  EffectiveAlertPreferenceResponse,
  PreferenceSource,
  AlertSeverity,
  NotificationChannel
} from '../../types/alertPreferences';
import { getEffectivePreferences } from '../../services/alertPreferences';

interface EffectiveAlertPreferencePanelProps {
  projectId?: string;
  businessRuleId?: string;
  refreshTrigger?: number;
}

const SEVERITY_COLORS: Record<AlertSeverity, string> = {
  INFO: '#2196f3',
  WARNING: '#ff9800',
  CRITICAL: '#f44336'
};

const SOURCE_CONFIG = {
  RULE: { label: 'Regra de Negócio', color: '#9c27b0', icon: '🎯' },
  PROJECT: { label: 'Projeto', color: '#2196f3', icon: '📁' },
  DEFAULT: { label: 'Padrão do Sistema', color: '#757575', icon: '⚙️' }
};

const CHANNEL_ICONS: Record<NotificationChannel, string> = {
  SLACK: '💬',
  TEAMS: '👥',
  EMAIL: '📧',
  SMS: '📱'
};

export const EffectiveAlertPreferencePanel: React.FC<EffectiveAlertPreferencePanelProps> = ({
  projectId,
  businessRuleId,
  refreshTrigger
}) => {
  const [effective, setEffective] = useState<EffectiveAlertPreferenceResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (projectId || businessRuleId) {
      loadEffectivePreferences();
    }
  }, [projectId, businessRuleId, refreshTrigger]);

  const loadEffectivePreferences = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getEffectivePreferences(projectId, businessRuleId);
      setEffective(data);
    } catch (err: any) {
      console.error('Erro ao carregar preferência efetiva:', err);
      setError(err.response?.data?.message || 'Erro ao carregar preferência efetiva');
    } finally {
      setLoading(false);
    }
  };

  if (!projectId && !businessRuleId) {
    return (
      <Card elevation={3}>
        <CardHeader
          avatar={<HierarchyIcon color="action" />}
          title="Preferência Efetiva (Resolução Hierárquica)"
          subheader="Selecione um projeto ou regra para visualizar"
        />
        <CardContent>
          <Alert severity="info">
            A preferência efetiva é resolvida automaticamente seguindo a hierarquia:
            <br />
            <strong>1. Regra de Negócio</strong> → <strong>2. Projeto</strong> → <strong>3. Padrão do Sistema</strong>
          </Alert>
        </CardContent>
      </Card>
    );
  }

  if (loading) {
    return (
      <Card elevation={3}>
        <CardContent sx={{ textAlign: 'center', py: 4 }}>
          <CircularProgress />
          <Typography variant="body2" color="textSecondary" sx={{ mt: 2 }}>
            Resolvendo preferência efetiva...
          </Typography>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card elevation={3}>
        <CardContent>
          <Alert severity="error">{error}</Alert>
        </CardContent>
      </Card>
    );
  }

  if (!effective) return null;

  const sourceConfig = SOURCE_CONFIG[effective.source];

  return (
    <Card elevation={3} sx={{ border: `2px solid ${sourceConfig.color}` }}>
      <CardHeader
        avatar={<CheckIcon style={{ color: sourceConfig.color }} />}
        title={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <span>Preferência Efetiva</span>
            <Chip
              label={`${sourceConfig.icon} ${sourceConfig.label}`}
              size="small"
              style={{ backgroundColor: sourceConfig.color, color: 'white', fontWeight: 'bold' }}
            />
          </Box>
        }
        subheader="Esta configuração será aplicada aos alertas (leitura automática)"
      />
      <CardContent>
        <Grid container spacing={3}>
          {/* Fonte da Preferência */}
          <Grid item xs={12}>
            <Alert severity="success" icon={<HierarchyIcon />}>
              <strong>Resolvido a partir de:</strong> {sourceConfig.label}
              {effective.source === 'RULE' && businessRuleId && (
                <Typography variant="caption" display="block">
                  Regra: {businessRuleId}
                </Typography>
              )}
              {effective.source === 'PROJECT' && projectId && (
                <Typography variant="caption" display="block">
                  Projeto: {projectId}
                </Typography>
              )}
              {effective.source === 'DEFAULT' && (
                <Typography variant="caption" display="block">
                  Nenhuma configuração específica encontrada. Usando valores padrão do sistema.
                </Typography>
              )}
            </Alert>
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          {/* Severidade Mínima */}
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary" gutterBottom>
              Severidade Mínima
            </Typography>
            <Chip
              label={effective.minimumSeverity}
              size="medium"
              style={{
                backgroundColor: SEVERITY_COLORS[effective.minimumSeverity],
                color: 'white',
                fontWeight: 'bold'
              }}
            />
            <Typography variant="caption" display="block" sx={{ mt: 1 }} color="textSecondary">
              Alertas abaixo de {effective.minimumSeverity} serão bloqueados
            </Typography>
          </Grid>

          {/* Janela de Entrega */}
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary" gutterBottom>
              Janela de Entrega
            </Typography>
            <Chip
              label={effective.deliveryWindow === 'BUSINESS_HOURS' ? '🕐 Horário Comercial' : '🌐 A Qualquer Momento'}
              size="medium"
              color="info"
            />
          </Grid>

          {/* Canais Ativos */}
          <Grid item xs={12}>
            <Typography variant="subtitle2" color="textSecondary" gutterBottom>
              Canais de Notificação Ativos
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {effective.channels.map((channel) => (
                <Chip
                  key={channel}
                  label={`${CHANNEL_ICONS[channel]} ${channel}`}
                  color="success"
                  variant="filled"
                  icon={<NotificationIcon />}
                />
              ))}
            </Box>
          </Grid>

          {/* Tipos de Alerta Permitidos */}
          <Grid item xs={12}>
            <Typography variant="subtitle2" color="textSecondary" gutterBottom>
              Tipos de Alerta Permitidos
            </Typography>
            {effective.allowedAlertTypes.length > 0 ? (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {effective.allowedAlertTypes.map((type) => (
                  <Chip
                    key={type}
                    label={type.replace(/_/g, ' ')}
                    color="primary"
                    variant="outlined"
                    size="small"
                  />
                ))}
              </Box>
            ) : (
              <Typography variant="body2" color="textSecondary">
                ✅ Todos os tipos de alerta estão habilitados
              </Typography>
            )}
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};
