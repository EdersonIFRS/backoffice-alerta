// US#39 - Formulário para gerar explicação executiva
import React, { useState } from 'react';
import {
  Box,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Paper,
  Typography,
  Alert
} from '@mui/material';
import { Insights as InsightsIcon } from '@mui/icons-material';
import type { ExecutiveImpactExplainRequest, Environment, ChangeType, ExplainFocus } from '../types/executiveImpact';

interface ExecutiveImpactFormProps {
  onSubmit: (request: ExecutiveImpactExplainRequest) => void;
  loading: boolean;
}

export const ExecutiveImpactForm: React.FC<ExecutiveImpactFormProps> = ({ onSubmit, loading }) => {
  const [pullRequestId, setPullRequestId] = useState('');
  const [environment, setEnvironment] = useState<Environment>('PRODUCTION');
  const [changeType, setChangeType] = useState<ChangeType>('FEATURE');
  const [focus, setFocus] = useState<ExplainFocus>('EXECUTIVE');
  const [error, setError] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!pullRequestId.trim()) {
      setError('Pull Request ID é obrigatório');
      return;
    }
    
    setError('');
    onSubmit({
      pullRequestId: pullRequestId.trim(),
      environment,
      changeType,
      focus
    });
  };

  return (
    <Paper sx={{ p: 3, mb: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
        <InsightsIcon sx={{ mr: 1, color: 'primary.main' }} />
        <Typography variant="h6">
          Gerar Explicação Executiva
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <form onSubmit={handleSubmit}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="Pull Request ID"
            value={pullRequestId}
            onChange={(e) => setPullRequestId(e.target.value)}
            required
            fullWidth
            placeholder="Ex: PR-2024-PAYMENT-HOTFIX"
            disabled={loading}
          />

          <FormControl fullWidth required>
            <InputLabel>Ambiente</InputLabel>
            <Select
              value={environment}
              label="Ambiente"
              onChange={(e) => setEnvironment(e.target.value as Environment)}
              disabled={loading}
            >
              <MenuItem value="DEVELOPMENT">Desenvolvimento</MenuItem>
              <MenuItem value="STAGING">Staging</MenuItem>
              <MenuItem value="PRODUCTION">Produção</MenuItem>
            </Select>
          </FormControl>

          <FormControl fullWidth required>
            <InputLabel>Tipo de Mudança</InputLabel>
            <Select
              value={changeType}
              label="Tipo de Mudança"
              onChange={(e) => setChangeType(e.target.value as ChangeType)}
              disabled={loading}
            >
              <MenuItem value="FEATURE">Feature (Nova Funcionalidade)</MenuItem>
              <MenuItem value="HOTFIX">Hotfix (Correção Urgente)</MenuItem>
              <MenuItem value="REFACTOR">Refactor (Refatoração)</MenuItem>
              <MenuItem value="CONFIG">Config (Configuração)</MenuItem>
            </Select>
          </FormControl>

          <FormControl fullWidth>
            <InputLabel>Foco da Explicação</InputLabel>
            <Select
              value={focus}
              label="Foco da Explicação"
              onChange={(e) => setFocus(e.target.value as ExplainFocus)}
              disabled={loading}
            >
              <MenuItem value="EXECUTIVE">Executivo (Alto Nível)</MenuItem>
              <MenuItem value="BUSINESS">Negócio (Processos)</MenuItem>
              <MenuItem value="TECHNICAL">Técnico (Implementação)</MenuItem>
            </Select>
          </FormControl>

          <Button
            type="submit"
            variant="contained"
            size="large"
            disabled={loading}
            fullWidth
          >
            {loading ? 'Gerando...' : 'Gerar Explicação Executiva'}
          </Button>
        </Box>
      </form>
    </Paper>
  );
};
