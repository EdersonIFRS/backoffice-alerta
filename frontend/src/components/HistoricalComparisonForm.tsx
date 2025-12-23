// US#42 - Formulário de Comparação Histórica
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
  Chip,
  Typography,
  Grid
} from '@mui/material';
import { Search } from '@mui/icons-material';
import type { DecisionHistoricalComparisonRequest, Environment, ChangeType } from '../types/historicalComparison';

interface Props {
  onSubmit: (request: DecisionHistoricalComparisonRequest) => void;
  loading: boolean;
}

export const HistoricalComparisonForm: React.FC<Props> = ({ onSubmit, loading }) => {
  const [pullRequestId, setPullRequestId] = useState('');
  const [environment, setEnvironment] = useState<Environment | ''>('');
  const [changeType, setChangeType] = useState<ChangeType | ''>('');
  const [files, setFiles] = useState<string[]>([]);
  const [fileInput, setFileInput] = useState('');

  const handleAddFile = () => {
    if (fileInput.trim() && !files.includes(fileInput.trim())) {
      setFiles([...files, fileInput.trim()]);
      setFileInput('');
    }
  };

  const handleRemoveFile = (file: string) => {
    setFiles(files.filter(f => f !== file));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!pullRequestId || files.length === 0) return;

    const request: DecisionHistoricalComparisonRequest = {
      currentPullRequestId: pullRequestId,
      changedFiles: files,
      ...(environment && { environment }),
      ...(changeType && { changeType })
    };

    onSubmit(request);
  };

  return (
    <Paper sx={{ p: 3, mb: 3 }}>
      <Typography variant="h6" gutterBottom>
        Comparar Decisão com Histórico
      </Typography>
      
      <Box component="form" onSubmit={handleSubmit}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              required
              label="Pull Request ID"
              value={pullRequestId}
              onChange={(e) => setPullRequestId(e.target.value)}
              placeholder="PR-2024-001"
              disabled={loading}
            />
          </Grid>

          <Grid item xs={12} md={3}>
            <FormControl fullWidth>
              <InputLabel>Ambiente</InputLabel>
              <Select
                value={environment}
                label="Ambiente"
                onChange={(e) => setEnvironment(e.target.value as Environment)}
                disabled={loading}
              >
                <MenuItem value="">Todos</MenuItem>
                <MenuItem value="DEV">DEV</MenuItem>
                <MenuItem value="STAGING">STAGING</MenuItem>
                <MenuItem value="PRODUCTION">PRODUCTION</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} md={3}>
            <FormControl fullWidth>
              <InputLabel>Tipo de Mudança</InputLabel>
              <Select
                value={changeType}
                label="Tipo de Mudança"
                onChange={(e) => setChangeType(e.target.value as ChangeType)}
                disabled={loading}
              >
                <MenuItem value="">Todos</MenuItem>
                <MenuItem value="FEATURE">FEATURE</MenuItem>
                <MenuItem value="HOTFIX">HOTFIX</MenuItem>
                <MenuItem value="REFACTOR">REFACTOR</MenuItem>
                <MenuItem value="CONFIG">CONFIG</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12}>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <TextField
                fullWidth
                label="Arquivos Alterados"
                value={fileInput}
                onChange={(e) => setFileInput(e.target.value)}
                placeholder="src/payment/PaymentService.java"
                onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), handleAddFile())}
                disabled={loading}
              />
              <Button 
                variant="outlined" 
                onClick={handleAddFile}
                disabled={loading || !fileInput.trim()}
              >
                Adicionar
              </Button>
            </Box>
          </Grid>

          {files.length > 0 && (
            <Grid item xs={12}>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {files.map((file) => (
                  <Chip
                    key={file}
                    label={file}
                    onDelete={() => handleRemoveFile(file)}
                    disabled={loading}
                  />
                ))}
              </Box>
            </Grid>
          )}

          <Grid item xs={12}>
            <Button
              type="submit"
              variant="contained"
              size="large"
              startIcon={<Search />}
              disabled={loading || !pullRequestId || files.length === 0}
              fullWidth
            >
              {loading ? 'Comparando...' : 'Comparar com Histórico'}
            </Button>
          </Grid>
        </Grid>
      </Box>
    </Paper>
  );
};
