// US#40 - Página principal de Timeline de Decisão
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Alert,
  CircularProgress,
  TextField,
  Button,
  Paper
} from '@mui/material';
import { Timeline as TimelineIcon, Search as SearchIcon } from '@mui/icons-material';
import { TimelineView } from '../components/TimelineView';
import { timelineApi } from '../services/timeline';
import type { ChangeTimelineResponse } from '../types/timeline';

export const Timeline: React.FC = () => {
  const { pullRequestId: urlPullRequestId } = useParams<{ pullRequestId?: string }>();
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [timeline, setTimeline] = useState<ChangeTimelineResponse | null>(null);
  const [searchInput, setSearchInput] = useState(urlPullRequestId || '');

  // Carregar timeline se vier da URL
  useEffect(() => {
    if (urlPullRequestId) {
      handleSearch(urlPullRequestId);
    }
  }, [urlPullRequestId]);

  const handleSearch = async (prId?: string) => {
    const targetPrId = prId || searchInput;
    
    if (!targetPrId.trim()) {
      setError('Por favor, informe um Pull Request ID');
      return;
    }

    setLoading(true);
    setError(null);
    setTimeline(null);

    try {
      const data = await timelineApi.getTimeline(targetPrId);
      setTimeline(data);
      
      if (data.events.length === 0) {
        setError('Nenhum evento encontrado para este Pull Request. Verifique se o ID está correto.');
      }
    } catch (err: any) {
      if (err.response?.status === 404) {
        setError('Pull Request não encontrado. Verifique o ID e tente novamente.');
      } else if (err.response?.status === 403) {
        setError('Acesso negado. Esta funcionalidade requer perfil ADMIN ou RISK_MANAGER.');
      } else {
        setError(err.response?.data?.message || 'Erro ao carregar timeline');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 4 }}>
        <TimelineIcon fontSize="large" color="primary" />
        <Box>
          <Typography variant="h4" component="h1">
            Linha do Tempo de Decisão
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Histórico cronológico completo de eventos de um Pull Request
          </Typography>
        </Box>
      </Box>

      {/* Barra de busca */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <TextField
            fullWidth
            label="Pull Request ID"
            placeholder="Ex: PR-2024-001 ou PR-2024-PAYMENT-HOTFIX"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={loading}
          />
          <Button
            variant="contained"
            startIcon={<SearchIcon />}
            onClick={() => handleSearch()}
            disabled={loading || !searchInput.trim()}
          >
            Buscar
          </Button>
        </Box>
      </Paper>

      {/* Loading */}
      {loading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 200 }}>
          <CircularProgress />
          <Typography sx={{ ml: 2 }}>Carregando timeline...</Typography>
        </Box>
      )}

      {/* Erro */}
      {error && !loading && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Timeline */}
      {timeline && !loading && (
        <TimelineView timeline={timeline} />
      )}

      {/* Estado inicial */}
      {!timeline && !loading && !error && (
        <Alert severity="info">
          Digite um Pull Request ID acima e clique em "Buscar" para visualizar sua linha do tempo de decisão.
        </Alert>
      )}
    </Container>
  );
};
