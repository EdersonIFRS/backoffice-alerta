// US#31.1 - Componente de Toggle para Cenários de Simulação
import React from 'react';
import { 
  Box, 
  FormControlLabel, 
  Switch, 
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography,
  Paper
} from '@mui/material';
import type { SimulationScenario } from '../types/simulation';

interface ScenarioToggleProps {
  scenario: SimulationScenario;
  onChange: (scenario: SimulationScenario) => void;
}

export const ScenarioToggle: React.FC<ScenarioToggleProps> = ({ scenario, onChange }) => {
  const handleToggle = (field: keyof SimulationScenario) => (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({
      ...scenario,
      [field]: event.target.checked
    });
  };

  const handleEnvironmentChange = (event: any) => {
    onChange({
      ...scenario,
      targetEnvironment: event.target.value
    });
  };

  return (
    <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
      <Typography variant="h6" gutterBottom>
        Opções de Simulação
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Selecione as alterações que deseja simular (nenhuma decisão real será criada)
      </Typography>

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        <FormControlLabel
          control={
            <Switch
              checked={scenario.removeHighRiskFiles}
              onChange={handleToggle('removeHighRiskFiles')}
            />
          }
          label="Remover arquivos de alto risco da análise"
        />

        <FormControlLabel
          control={
            <Switch
              checked={scenario.reduceFileCount}
              onChange={handleToggle('reduceFileCount')}
            />
          }
          label="Reduzir quantidade de arquivos modificados (simular quebra de PR)"
        />

        <FormControlLabel
          control={
            <Switch
              checked={scenario.markAsHotfix}
              onChange={handleToggle('markAsHotfix')}
            />
          }
          label="Marcar como HOTFIX (prioridade alta)"
        />

        <FormControlLabel
          control={
            <Switch
              checked={scenario.changeEnvironment}
              onChange={handleToggle('changeEnvironment')}
            />
          }
          label="Alterar ambiente de deployment"
        />

        {scenario.changeEnvironment && (
          <FormControl fullWidth sx={{ ml: 4 }}>
            <InputLabel>Ambiente Simulado</InputLabel>
            <Select
              value={scenario.targetEnvironment || 'DEV'}
              label="Ambiente Simulado"
              onChange={handleEnvironmentChange}
            >
              <MenuItem value="DEV">DEV</MenuItem>
              <MenuItem value="STAGING">STAGING</MenuItem>
              <MenuItem value="PRODUCTION">PRODUCTION</MenuItem>
            </Select>
          </FormControl>
        )}
      </Box>
    </Paper>
  );
};
