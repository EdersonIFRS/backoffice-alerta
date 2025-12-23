// US#47 - Página principal do Chat Unificado de Análise de Impacto (Risk Chat)
import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Paper, 
  Typography,
  CircularProgress,
  Backdrop,
  Alert,
  Container
} from '@mui/material';
import { RiskChatInput } from '../components/RiskChatInput';
import { RiskChatConversation } from '../components/RiskChatConversation';
import { riskChatApi } from '../services/riskChat';
import api from '../services/api';
import type { ExplainFocus, ChatHistoryItem } from '../types/riskChat';

/**
 * US#47 - Frontend do Chat Unificado de Análise de Impacto
 * 
 * Funcionalidades:
 * - Interface conversacional para análise de impacto
 * - Suporte a mensagens estruturadas (INFO/WARNING/ACTION)
 * - Indicadores de confiança
 * - Fallback amigável em caso de erro
 * - Read-only (não persiste histórico)
 * 
 * Integração:
 * - Backend: POST /risk/chat/query (US#46)
 * - RBAC: ADMIN, RISK_MANAGER, ENGINEER
 * - JWT obrigatório
 */
export const RiskChat: React.FC = () => {
  const [history, setHistory] = useState<ChatHistoryItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [projects, setProjects] = useState<Array<{ id: string; name: string }>>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<string>('');

  // Buscar projetos ao montar o componente
  useEffect(() => {
    const fetchProjects = async () => {
      try {
        const response = await api.get('/projects');
        setProjects(response.data.map((p: any) => ({ id: p.id, name: p.name })));
      } catch (err) {
        console.error('Erro ao buscar projetos:', err);
      }
    };
    fetchProjects();
  }, []);

  const handleSubmitQuestion = async (question: string, focus?: ExplainFocus, projectId?: string) => {
    setLoading(true);
    setError(null);

    try {
      const response = await riskChatApi.queryChat({
        question,
        focus,
        projectId
      });

      // Adiciona ao histórico (apenas memória, não persiste)
      const newItem: ChatHistoryItem = {
        question,
        response,
        timestamp: new Date().toISOString()
      };

      setHistory(prev => [...prev, newItem]);
    } catch (err: any) {
      console.error('Erro ao processar pergunta:', err);
      setError(
        err.response?.data?.message || 
        'Não foi possível processar sua pergunta. Verifique sua conexão e tente novamente.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="lg" sx={{ height: 'calc(100vh - 100px)', display: 'flex', flexDirection: 'column', py: 3 }}>
      <Paper 
        elevation={3} 
        sx={{ 
          flexGrow: 1, 
          display: 'flex', 
          flexDirection: 'column',
          overflow: 'hidden'
        }}
      >
        {/* Header */}
        <Box sx={{ p: 2, backgroundColor: 'primary.main', color: 'white' }}>
          <Typography variant="h5" fontWeight="bold">
            💬 Chat de Análise de Impacto
          </Typography>
          <Typography variant="body2">
            Pergunte sobre regras de negócio, impacto de código, responsáveis e histórico de incidentes
          </Typography>
        </Box>

        {/* Erro global (se houver) */}
        {error && (
          <Alert severity="error" onClose={() => setError(null)} sx={{ m: 2 }}>
            {error}
          </Alert>
        )}

        {/* Área de conversação */}
        <Box sx={{ flexGrow: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          <RiskChatConversation history={history} />
        </Box>

        {/* Input de pergunta */}
        <RiskChatInput 
          onSubmit={handleSubmitQuestion} 
          disabled={loading}
          projects={projects}
          selectedProjectId={selectedProjectId}
          onProjectChange={setSelectedProjectId}
        />
      </Paper>

      {/* Loading overlay */}
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <Box sx={{ textAlign: 'center' }}>
          <CircularProgress color="inherit" />
          <Typography variant="h6" sx={{ mt: 2 }}>
            Analisando sua pergunta...
          </Typography>
          <Typography variant="body2">
            Consultando regras, código e histórico
          </Typography>
        </Box>
      </Backdrop>
    </Container>
  );
};
