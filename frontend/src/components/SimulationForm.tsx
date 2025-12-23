// US#31.1 - Formulário de Simulação de Risco
import React, { useState } from 'react';
import { 
  Box, 
  Paper, 
  TextField, 
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography,
  Alert
} from '@mui/material';
import { PlayArrow as SimulateIcon } from '@mui/icons-material';
import type { SimulationRequest, SimulationScenario } from '../types/simulation';
import { ScenarioToggle } from './ScenarioToggle';

interface SimulationFormProps {
  onSimulate: (request: SimulationRequest) => void;
  loading: boolean;
}

export const SimulationForm: React.FC<SimulationFormProps> = ({ onSimulate, loading }) => {
  const [pullRequestId, setPullRequestId] = useState('PR-12345');
  const [environment, setEnvironment] = useState<'DEV' | 'STAGING' | 'PRODUCTION'>('PRODUCTION');
  const [changeType, setChangeType] = useState<'FEATURE' | 'HOTFIX' | 'REFACTOR'>('FEATURE');
  const [scenario, setScenario] = useState<SimulationScenario>({
    removeHighRiskFiles: false,
    reduceFileCount: false,
    markAsHotfix: false,
    changeEnvironment: false,
    targetEnvironment: 'DEV'
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSimulate({
      pullRequestId,
      environment,
      changeType,
      scenario
    });
  };

  const hasAnyScenario = 
    scenario.removeHighRiskFiles || 
    scenario.reduceFileCount || 
    scenario.markAsHotfix || 
    scenario.changeEnvironment;

  return (
    <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
      <Typography variant="h5" gutterBottom>
        🧪 Simulação de Decisão de Risco (What-If)
      </Typography>
      
      <Alert severity="info" sx={{ mb: 3 }}>
        <Typography variant="body2">
          <strong>Atenção:</strong> Esta é uma ferramenta de simulação. 
          Nenhuma decisão real será criada, alterada ou registrada. 
          Use para avaliar impactos antes de tomar decisões reais.
        </Typography>
      </Alert>

      <Box component="form" onSubmit={handleSubmit}>
        <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
          Dados do Pull Request
        </Typography>

        <TextField
          fullWidth
          label="Pull Request ID"
          value={pullRequestId}
          onChange={(e) => setPullRequestId(e.target.value)}
          required
          sx={{ mb: 2 }}
          placeholder="Ex: PR-12345"
          helperText="ID do Pull Request para simular"
        />

        <FormControl fullWidth sx={{ mb: 2 }}>
          <InputLabel>Ambiente Atual</InputLabel>
          <Select
            value={environment}
            label="Ambiente Atual"
            onChange={(e) => setEnvironment(e.target.value as any)}
          >
            <MenuItem value="DEV">DEV</MenuItem>
            <MenuItem value="STAGING">STAGING</MenuItem>
            <MenuItem value="PRODUCTION">PRODUCTION</MenuItem>
          </Select>
        </FormControl>

        <FormControl fullWidth sx={{ mb: 3 }}>
          <InputLabel>Tipo de Mudança</InputLabel>
          <Select
            value={changeType}
            label="Tipo de Mudança"
            onChange={(e) => setChangeType(e.target.value as any)}
          >
            <MenuItem value="FEATURE">FEATURE (Nova funcionalidade)</MenuItem>
            <MenuItem value="HOTFIX">HOTFIX (Correção urgente)</MenuItem>
            <MenuItem value="REFACTOR">REFACTOR (Refatoração)</MenuItem>
          </Select>
        </FormControl>

        <ScenarioToggle scenario={scenario} onChange={setScenario} />

        {!hasAnyScenario && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            Selecione pelo menos uma opção de simulação para comparar com o cenário atual.
          </Alert>
        )}

        <Button
          type="submit"
          variant="contained"
          size="large"
          fullWidth
          startIcon={<SimulateIcon />}
          disabled={loading || !hasAnyScenario}
          sx={{ mt: 2 }}
        >
          {loading ? 'Simulando...' : 'Executar Simulação'}
        </Button>
      </Box>
    </Paper>
  );
};
