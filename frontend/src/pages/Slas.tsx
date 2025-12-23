// US#31 - Página de SLAs Ativos
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
import { Warning } from '@mui/icons-material';
import { slaApi } from '../services/api';
import { SlaStatusBadge, RiskLevelBadge } from '../components/StatusBadge';
import type { RiskSlaTracking } from '../types';

export const Slas: React.FC = () => {
  const [slas, setSlas] = useState<RiskSlaTracking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadSlas = async () => {
    try {
      setLoading(true);
      const data = await slaApi.getAll();
      setSlas(data);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao carregar SLAs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSlas();
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
        SLAs Ativos
      </Typography>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Status</strong></TableCell>
              <TableCell><strong>Time Responsável</strong></TableCell>
              <TableCell><strong>Nível de Risco</strong></TableCell>
              <TableCell><strong>Deadline</strong></TableCell>
              <TableCell><strong>Nível de Escalonamento</strong></TableCell>
              <TableCell><strong>Criado em</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {slas.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <Typography color="text.secondary">Nenhum SLA encontrado</Typography>
                </TableCell>
              </TableRow>
            ) : (
              slas.map((sla) => (
                <TableRow
                  key={sla.id}
                  sx={{
                    bgcolor: sla.isOverdue ? 'error.light' : 'inherit',
                    '&:hover': { bgcolor: sla.isOverdue ? 'error.main' : 'action.hover' }
                  }}
                >
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <SlaStatusBadge status={sla.status} />
                      {sla.isOverdue && (
                        <Chip
                          icon={<Warning />}
                          label="VENCIDO"
                          color="error"
                          size="small"
                        />
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>{sla.teamName}</TableCell>
                  <TableCell>
                    <RiskLevelBadge level={sla.riskLevel} />
                  </TableCell>
                  <TableCell>
                    <Typography
                      variant="body2"
                      color={sla.isOverdue ? 'error' : 'text.primary'}
                      fontWeight={sla.isOverdue ? 'bold' : 'normal'}
                    >
                      {new Date(sla.slaDeadline).toLocaleString('pt-BR')}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip label={sla.currentLevel} size="small" variant="outlined" />
                  </TableCell>
                  <TableCell>
                    {new Date(sla.createdAt).toLocaleString('pt-BR')}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {slas.filter(s => s.isOverdue).length > 0 && (
        <Alert severity="warning" sx={{ mt: 2 }}>
          {slas.filter(s => s.isOverdue).length} SLA(s) vencido(s) requer atenção imediata!
        </Alert>
      )}
    </Box>
  );
};
