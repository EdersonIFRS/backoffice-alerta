// US#47 - Componente de input do chat (pergunta + foco)
import React, { useState } from 'react';
import { 
  Box, 
  TextField, 
  Button, 
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Paper
} from '@mui/material';
import { Send as SendIcon } from '@mui/icons-material';
import type { ExplainFocus } from '../types/riskChat';

interface RiskChatInputProps {
  onSubmit: (question: string, focus?: ExplainFocus, projectId?: string) => void;
  disabled?: boolean;
  projects?: Array<{ id: string; name: string }>;
  selectedProjectId?: string;
  onProjectChange?: (projectId: string) => void;
}

export const RiskChatInput: React.FC<RiskChatInputProps> = ({ 
  onSubmit, 
  disabled = false,
  projects = [],
  selectedProjectId = '',
  onProjectChange
}) => {
  const [question, setQuestion] = useState('');
  const [focus, setFocus] = useState<ExplainFocus | ''>('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (question.trim()) {
      onSubmit(question, focus || undefined, selectedProjectId || undefined);
      setQuestion('');
    }
  };

  return (
    <Paper 
      component="form" 
      onSubmit={handleSubmit}
      elevation={2}
      sx={{ 
        p: 2,
        position: 'sticky',
        bottom: 0,
        backgroundColor: 'background.paper',
        zIndex: 10
      }}
    >
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-end' }}>
        <TextField
          fullWidth
          multiline
          maxRows={4}
          label="Sua pergunta sobre impacto, regras ou código"
          placeholder="Ex: Onde alterar o cálculo de horas para Pessoa Jurídica?"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          disabled={disabled}
          variant="outlined"
          sx={{ flexGrow: 1 }}
        />
        
        {projects.length > 0 && (
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel>Projeto (Opcional)</InputLabel>
            <Select
              value={selectedProjectId}
              onChange={(e) => onProjectChange?.(e.target.value)}
              disabled={disabled}
              label="Projeto (Opcional)"
            >
              <MenuItem value="">
                <em>Todos os projetos</em>
              </MenuItem>
              {projects.map((project) => (
                <MenuItem key={project.id} value={project.id}>
                  {project.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        )}
        
        <FormControl sx={{ minWidth: 180 }}>
          <InputLabel>Foco (Opcional)</InputLabel>
          <Select
            value={focus}
            onChange={(e) => setFocus(e.target.value as ExplainFocus | '')}
            disabled={disabled}
            label="Foco (Opcional)"
          >
            <MenuItem value="">
              <em>Automático</em>
            </MenuItem>
            <MenuItem value="BUSINESS">Negócio</MenuItem>
            <MenuItem value="TECHNICAL">Técnico</MenuItem>
            <MenuItem value="EXECUTIVE">Executivo</MenuItem>
          </Select>
        </FormControl>

        <Button 
          type="submit"
          variant="contained" 
          color="primary"
          disabled={disabled || !question.trim()}
          startIcon={<SendIcon />}
          sx={{ height: 56 }}
        >
          Perguntar
        </Button>
      </Box>
    </Paper>
  );
};
