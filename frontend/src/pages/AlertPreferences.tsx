import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  Grid,
  Alert,
  CircularProgress
} from '@mui/material';
import { NotificationsActive as NotificationIcon } from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { ProjectAlertPreferencesCard } from '../components/alert-preferences/ProjectAlertPreferencesCard';
import { RuleAlertPreferencesCard } from '../components/alert-preferences/RuleAlertPreferencesCard';
import { EffectiveAlertPreferencePanel } from '../components/alert-preferences/EffectiveAlertPreferencePanel';
import { AlertPreferenceSimulator } from '../components/alert-preferences/AlertPreferenceSimulator';
import api from '../services/api';

interface Project {
  id: string;
  name: string;
  status: string;
}

interface BusinessRule {
  id: string;
  name: string;
}

export const AlertPreferences: React.FC = () => {
  const { hasRole } = useAuth();
  const [projects, setProjects] = useState<Project[]>([]);
  const [businessRules, setBusinessRules] = useState<BusinessRule[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<string>('');
  const [selectedRuleId, setSelectedRuleId] = useState<string>('');
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const canEdit = hasRole('ADMIN');
  const canView = hasRole('ADMIN') || hasRole('RISK_MANAGER');

  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Busca projetos ativos
      const projectsResponse = await api.get<Project[]>('/projects/active');
      setProjects(projectsResponse.data);

      // Busca todas as regras de negócio
      const rulesResponse = await api.get<BusinessRule[]>('/business-rules');
      setBusinessRules(rulesResponse.data);
    } catch (err: any) {
      console.error('Erro ao carregar dados iniciais:', err);
      setError(err.response?.data?.message || 'Erro ao carregar projetos e regras');
    } finally {
      setLoading(false);
    }
  };

  const handlePreferenceChange = () => {
    setRefreshTrigger((prev) => prev + 1);
  };

  if (!canView) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Alert severity="error">
          Você não tem permissão para acessar esta página. Apenas ADMIN e RISK_MANAGER podem visualizar preferências de alertas.
        </Alert>
      </Container>
    );
  }

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4, textAlign: 'center' }}>
        <CircularProgress size={60} />
        <Typography variant="h6" sx={{ mt: 2 }}>
          Carregando preferências de alertas...
        </Typography>
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* Cabeçalho */}
      <Box sx={{ mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
          <NotificationIcon fontSize="large" color="primary" />
          <Typography variant="h4" component="h1" fontWeight="bold">
            Preferências de Alertas
          </Typography>
        </Box>
        <Typography variant="body1" color="textSecondary">
          Configure quem recebe alertas, quando e por quais canais. Hierarquia: <strong>Regra &gt; Projeto &gt; Padrão</strong>
        </Typography>
        {!canEdit && (
          <Alert severity="info" sx={{ mt: 2 }}>
            Você está no modo <strong>leitura</strong>. Apenas ADMIN pode editar preferências.
          </Alert>
        )}
      </Box>

      <Grid container spacing={4}>
        {/* Bloco 1: Preferências por Projeto */}
        <Grid item xs={12} lg={6}>
          <ProjectAlertPreferencesCard
            projects={projects}
            canEdit={canEdit}
            onPreferenceChange={handlePreferenceChange}
          />
        </Grid>

        {/* Bloco 2: Preferências por Regra de Negócio */}
        <Grid item xs={12} lg={6}>
          <RuleAlertPreferencesCard
            businessRules={businessRules}
            canEdit={canEdit}
            onPreferenceChange={handlePreferenceChange}
          />
        </Grid>

        {/* Bloco 3: Preferência Efetiva (READ-ONLY) */}
        <Grid item xs={12}>
          <EffectiveAlertPreferencePanel
            projectId={selectedProjectId || undefined}
            businessRuleId={selectedRuleId || undefined}
            refreshTrigger={refreshTrigger}
          />
        </Grid>

        {/* Bloco 4: Simulação de Alerta */}
        <Grid item xs={12}>
          <AlertPreferenceSimulator
            projects={projects}
            businessRules={businessRules}
          />
        </Grid>
      </Grid>

      {/* Informações Adicionais */}
      <Box sx={{ mt: 4 }}>
        <Alert severity="info">
          <Typography variant="subtitle2" gutterBottom>
            📚 Como funciona a hierarquia de preferências:
          </Typography>
          <Typography variant="body2" component="ul" sx={{ pl: 2 }}>
            <li><strong>1. Regra de Negócio:</strong> Configuração mais específica, sobrescreve tudo.</li>
            <li><strong>2. Projeto:</strong> Configuração para todos os alertas do projeto.</li>
            <li><strong>3. Padrão do Sistema:</strong> Valores default quando nada está configurado.</li>
          </Typography>
        </Alert>
      </Box>
    </Container>
  );
};
