// US#31 - Página de Notificações Organizacionais
import React, { useEffect, useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Alert,
  Chip
} from '@mui/material';
import { notificationApi } from '../services/api';
import { SeverityBadge } from '../components/StatusBadge';
import type { RiskNotification } from '../types';

export const Notifications: React.FC = () => {
  const [notifications, setNotifications] = useState<RiskNotification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadNotifications = async () => {
    try {
      setLoading(true);
      const data = await notificationApi.getAll();
      setNotifications(data);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao carregar notificações');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Notificações Organizacionais
      </Typography>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Severidade</strong></TableCell>
              <TableCell><strong>Canal</strong></TableCell>
              <TableCell><strong>Time</strong></TableCell>
              <TableCell><strong>Mensagem</strong></TableCell>
              <TableCell><strong>Data/Hora</strong></TableCell>
              <TableCell><strong>Auditoria ID</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {notifications.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <Typography color="text.secondary">Nenhuma notificação encontrada</Typography>
                </TableCell>
              </TableRow>
            ) : (
              notifications.map((notification) => (
                <TableRow key={notification.id} hover>
                  <TableCell>
                    <SeverityBadge severity={notification.severity} />
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={notification.channel} 
                      size="small" 
                      variant="outlined"
                      color="primary"
                    />
                  </TableCell>
                  <TableCell>{notification.teamName}</TableCell>
                  <TableCell>
                    <Typography variant="body2" noWrap sx={{ maxWidth: 400 }}>
                      {notification.message}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    {new Date(notification.createdAt).toLocaleString('pt-BR')}
                  </TableCell>
                  <TableCell>
                    <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                      {notification.auditId.substring(0, 8)}...
                    </Typography>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};
