// US#47 - Componente de exibição da conversa do chat
import React, { useRef, useEffect } from 'react';
import { 
  Box, 
  Paper, 
  Typography, 
  Chip,
  Divider,
  Alert
} from '@mui/material';
import { RiskChatMessage } from './RiskChatMessage';
import type { ChatHistoryItem, ConfidenceLevel } from '../types/riskChat';

interface RiskChatConversationProps {
  history: ChatHistoryItem[];
}

export const RiskChatConversation: React.FC<RiskChatConversationProps> = ({ history }) => {
  const scrollRef = useRef<HTMLDivElement>(null);

  // Auto-scroll para última mensagem
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [history]);

  const getConfidenceColor = (confidence: ConfidenceLevel) => {
    switch (confidence) {
      case 'HIGH':
        return 'success';
      case 'MEDIUM':
        return 'warning';
      case 'LOW':
        return 'error';
      default:
        return 'default';
    }
  };

  if (history.length === 0) {
    return (
      <Box 
        sx={{ 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          height: '100%',
          p: 3
        }}
      >
        <Alert severity="info" sx={{ maxWidth: 600 }}>
          <Typography variant="h6" gutterBottom>
            💬 Chat de Análise de Impacto
          </Typography>
          <Typography variant="body2" paragraph>
            Pergunte sobre impacto de mudanças, regras de negócio, código ou responsáveis.
          </Typography>
          <Typography variant="body2" fontWeight="bold" gutterBottom>
            Exemplos de perguntas:
          </Typography>
          <Typography variant="body2" component="ul" sx={{ pl: 2 }}>
            <li>Onde alterar o cálculo de horas para Pessoa Jurídica?</li>
            <li>Quais riscos existem ao mudar regras de pagamento?</li>
            <li>Quem preciso avisar antes de alterar validação de CPF?</li>
            <li>Alterar validação de CPF já causou incidente em produção?</li>
          </Typography>
        </Alert>
      </Box>
    );
  }

  return (
    <Box 
      ref={scrollRef}
      sx={{ 
        flexGrow: 1, 
        overflowY: 'auto', 
        p: 3,
        height: '100%'
      }}
    >
      {history.map((item, index) => (
        <Box key={index} sx={{ mb: 4 }}>
          {/* Pergunta do usuário */}
          <Paper 
            elevation={1} 
            sx={{ 
              p: 2, 
              mb: 2, 
              backgroundColor: '#e3f2fd',
              borderLeft: '4px solid #2196f3'
            }}
          >
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Você perguntou:
            </Typography>
            <Typography variant="body1" fontWeight="500">
              {item.question}
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
              {new Date(item.timestamp).toLocaleString('pt-BR')}
            </Typography>
          </Paper>

          {/* Resposta do sistema */}
          <Paper elevation={1} sx={{ p: 2, backgroundColor: '#fafafa' }}>
            {/* Resumo textual */}
            {item.response.answer && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                  📋 Resumo:
                </Typography>
                <Typography variant="body2" sx={{ whiteSpace: 'pre-line' }}>
                  {item.response.answer}
                </Typography>
              </Box>
            )}

            <Divider sx={{ my: 2 }} />

            {/* Mensagens estruturadas */}
            <Box>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                📌 Detalhes:
              </Typography>
              {item.response.messages.map((message, msgIndex) => (
                <RiskChatMessage key={msgIndex} message={message} />
              ))}
            </Box>

            {/* Confiança e disclaimer */}
            <Box sx={{ mt: 2, display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
              <Chip 
                label={`Confiança: ${item.response.confidence}`}
                color={getConfidenceColor(item.response.confidence)}
                size="small"
              />
              {item.response.usedFallback && (
                <Chip 
                  label="Resposta de fallback"
                  color="warning"
                  size="small"
                  variant="outlined"
                />
              )}
            </Box>

            {item.response.disclaimer && (
              <Alert severity="info" sx={{ mt: 2 }}>
                <Typography variant="caption">
                  {item.response.disclaimer}
                </Typography>
              </Alert>
            )}
          </Paper>
        </Box>
      ))}
    </Box>
  );
};
