// US#47 - Componente para renderizar mensagem individual do chat
import React from 'react';
import { 
  Box, 
  Typography, 
  Chip,
  Alert
} from '@mui/material';
import { 
  InfoOutlined, 
  WarningAmberOutlined, 
  CheckCircleOutlined 
} from '@mui/icons-material';
import type { ChatMessage } from '../types/riskChat';

interface RiskChatMessageProps {
  message: ChatMessage;
}

export const RiskChatMessage: React.FC<RiskChatMessageProps> = ({ message }) => {
  const getMessageStyle = () => {
    switch (message.type) {
      case 'INFO':
        return {
          severity: 'info' as const,
          icon: <InfoOutlined />,
          color: '#0288d1'
        };
      case 'WARNING':
        return {
          severity: 'warning' as const,
          icon: <WarningAmberOutlined />,
          color: '#ed6c02'
        };
      case 'ACTION':
        return {
          severity: 'success' as const,
          icon: <CheckCircleOutlined />,
          color: '#2e7d32'
        };
      default:
        return {
          severity: 'info' as const,
          icon: <InfoOutlined />,
          color: '#0288d1'
        };
    }
  };

  const style = getMessageStyle();

  return (
    <Alert 
      severity={style.severity} 
      icon={style.icon}
      sx={{ 
        mb: 2,
        '& .MuiAlert-message': { width: '100%' }
      }}
    >
      <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
        {message.title}
      </Typography>
      <Typography variant="body2" sx={{ whiteSpace: 'pre-line', mb: 1 }}>
        {message.content}
      </Typography>
      
      {message.sources && message.sources.length > 0 && (
        <Box sx={{ mt: 1 }}>
          <Typography variant="caption" color="text.secondary" display="block" gutterBottom>
            Fontes:
          </Typography>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
            {message.sources.map((source, index) => (
              <Chip 
                key={index} 
                label={source} 
                size="small" 
                variant="outlined"
              />
            ))}
          </Box>
        </Box>
      )}

      {message.confidence && (
        <Box sx={{ mt: 1 }}>
          <Chip 
            label={`Confiança: ${message.confidence}`} 
            size="small"
            color={
              message.confidence === 'HIGH' ? 'success' : 
              message.confidence === 'MEDIUM' ? 'warning' : 
              'error'
            }
            variant="outlined"
          />
        </Box>
      )}
    </Alert>
  );
};
