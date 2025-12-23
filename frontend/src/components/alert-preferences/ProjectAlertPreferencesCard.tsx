import React, { useState, useEffect } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Box,
  Button,
  CircularProgress,
  Alert,
  Snackbar,
  Typography,
  Tooltip,
  Grid,
  SelectChangeEvent
} from '@mui/material';
import {
  Save as SaveIcon,
  Info as InfoIcon,
  NotificationsActive as NotificationsIcon
} from '@mui/icons-material';
import {
  AlertSeverity,
  AlertType,
  NotificationChannel,
  AlertDeliveryWindow,
  AlertPreferenceRequest,
  AlertPreferenceResponse
} from '../../types/alertPreferences';
import { getProjectPreferences, saveProjectPreferences, getDefaultPreferences } from '../../services/alertPreferences';

interface ProjectAlertPreferencesCardProps {
  projects: Array<{ id: string; name: string }>;
  canEdit: boolean; // ADMIN = true, RISK_MANAGER = false
  onPreferenceChange?: () => void;
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

export const ProjectAlertPreferencesCard: React.FC<ProjectAlertPreferencesCardProps> = ({
  projects,
  canEdit,
  onPreferenceChange
}) => {
  const [selectedProjectId, setSelectedProjectId] = useState<string>('');
  const [preferences, setPreferences] = useState<AlertPreferenceRequest>(getDefaultPreferences());
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success'
  });

  useEffect(() => {
    if (selectedProjectId) {
      loadPreferences();
    }
  }, [selectedProjectId]);

  const loadPreferences = async () => {
    setLoading(true);
    try {
      const data = await getProjectPreferences(selectedProjectId);
      if (data) {
        setPreferences({
          minimumSeverity: data.minimumSeverity,
          allowedAlertTypes: data.allowedAlertTypes,
          channels: data.channels,
          deliveryWindow: data.deliveryWindow
        });
      } else {
        setPreferences(getDefaultPreferences());
      }
    } catch (error) {
      console.error('Erro ao carregar preferências do projeto:', error);
      setSnackbar({
        open: true,
        message: 'Erro ao carregar preferências',
        severity: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!selectedProjectId) return;

    setSaving(true);
    try {
      await saveProjectPreferences(selectedProjectId, preferences);
      setSnackbar({
        open: true,
        message: 'Preferências salvas com sucesso!',
        severity: 'success'
      });
      if (onPreferenceChange) onPreferenceChange();
    } catch (error: any) {
      console.error('Erro ao salvar preferências:', error);
      setSnackbar({
        open: true,
        message: error.response?.data?.message || 'Erro ao salvar preferências',
        severity: 'error'
      });
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <Card elevation={3}>
        <CardHeader
          avatar={<NotificationsIcon color="primary" />}
          title="Preferências de Alertas por Projeto"
          subheader="Configure severidade mínima, tipos de alerta, canais e horários de envio"
        />
        <CardContent>
          <Grid container spacing={3}>
            {/* Seleção de Projeto */}
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Selecione um Projeto</InputLabel>
                <Select
                  value={selectedProjectId}
                  onChange={(e: SelectChangeEvent) => setSelectedProjectId(e.target.value)}
                  disabled={loading || saving}
                >
                  {projects.map((project) => (
                    <MenuItem key={project.id} value={project.id}>
                      {project.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {selectedProjectId && !loading && (
              <>
                {/* Severidade Mínima */}
                <Grid item xs={12} md={6}>
                  <FormControl fullWidth>
                    <InputLabel>Severidade Mínima</InputLabel>
                    <Select
                      value={preferences.minimumSeverity}
                      onChange={(e) => setPreferences({ ...preferences, minimumSeverity: e.target.value as AlertSeverity })}
                      disabled={!canEdit || saving}
                    >
                      {Object.values(AlertSeverity).map((severity) => (
                        <MenuItem key={severity} value={severity}>
                          <Chip
                            label={severity}
                            size="small"
                            style={{ backgroundColor: SEVERITY_COLORS[severity], color: 'white' }}
                          />
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <Typography variant="caption" color="textSecondary" sx={{ mt: 1, display: 'block' }}>
                    Alertas abaixo desta severidade serão ignorados
                  </Typography>
                </Grid>

                {/* Janela de Entrega */}
                <Grid item xs={12} md={6}>
                  <FormControl fullWidth>
                    <InputLabel>Janela de Entrega</InputLabel>
                    <Select
                      value={preferences.deliveryWindow}
                      onChange={(e) => setPreferences({ ...preferences, deliveryWindow: e.target.value as AlertDeliveryWindow })}
                      disabled={!canEdit || saving}
                    >
                      <MenuItem value={AlertDeliveryWindow.ANY_TIME}>
                        A qualquer momento
                      </MenuItem>
                      <MenuItem value={AlertDeliveryWindow.BUSINESS_HOURS}>
                        Apenas horário comercial
                      </MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                {/* Tipos de Alerta Permitidos */}
                <Grid item xs={12}>
                  <Typography variant="subtitle2" gutterBottom>
                    Tipos de Alerta Permitidos
                    <Tooltip title="Deixe vazio para permitir todos os tipos">
                      <InfoIcon fontSize="small" sx={{ ml: 1, verticalAlign: 'middle' }} />
                    </Tooltip>
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {Object.values(AlertType).map((type) => (
                      <Chip
                        key={type}
                        label={type.replace(/_/g, ' ')}
                        onClick={() => {
                          if (!canEdit || saving) return;
                          setPreferences({
                            ...preferences,
                            allowedAlertTypes: preferences.allowedAlertTypes.includes(type)
                              ? preferences.allowedAlertTypes.filter((t) => t !== type)
                              : [...preferences.allowedAlertTypes, type]
                          });
                        }}
                        color={preferences.allowedAlertTypes.includes(type) ? 'primary' : 'default'}
                        variant={preferences.allowedAlertTypes.includes(type) ? 'filled' : 'outlined'}
                        disabled={!canEdit || saving}
                      />
                    ))}
                  </Box>
                  {preferences.allowedAlertTypes.length === 0 && (
                    <Typography variant="caption" color="textSecondary" sx={{ mt: 1, display: 'block' }}>
                      Todos os tipos de alerta estão habilitados
                    </Typography>
                  )}
                </Grid>

                {/* Canais de Notificação */}
                <Grid item xs={12}>
                  <Typography variant="subtitle2" gutterBottom>
                    Canais de Notificação Ativos
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {Object.values(NotificationChannel).map((channel) => (
                      <Chip
                        key={channel}
                        label={`${CHANNEL_ICONS[channel]} ${channel}`}
                        onClick={() => {
                          if (!canEdit || saving) return;
                          setPreferences({
                            ...preferences,
                            channels: preferences.channels.includes(channel)
                              ? preferences.channels.filter((c) => c !== channel)
                              : [...preferences.channels, channel]
                          });
                        }}
                        color={preferences.channels.includes(channel) ? 'success' : 'default'}
                        variant={preferences.channels.includes(channel) ? 'filled' : 'outlined'}
                        disabled={!canEdit || saving}
                      />
                    ))}
                  </Box>
                </Grid>

                {/* Botão Salvar */}
                {canEdit && (
                  <Grid item xs={12}>
                    <Button
                      variant="contained"
                      color="primary"
                      startIcon={saving ? <CircularProgress size={20} /> : <SaveIcon />}
                      onClick={handleSave}
                      disabled={saving || preferences.channels.length === 0}
                      fullWidth
                    >
                      {saving ? 'Salvando...' : 'Salvar Preferências do Projeto'}
                    </Button>
                  </Grid>
                )}

                {!canEdit && (
                  <Grid item xs={12}>
                    <Alert severity="info">
                      Você tem permissão apenas para visualizar. Contate um ADMIN para editar.
                    </Alert>
                  </Grid>
                )}
              </>
            )}

            {loading && (
              <Grid item xs={12} sx={{ textAlign: 'center', py: 4 }}>
                <CircularProgress />
              </Grid>
            )}
          </Grid>
        </CardContent>
      </Card>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({ ...snackbar, open: false })}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </>
  );
};
