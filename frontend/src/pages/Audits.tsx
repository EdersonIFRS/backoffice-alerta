// US#31 - Página de Auditorias & Decisões
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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Chip
} from '@mui/material';
import { auditApi, notificationApi } from '../services/api';
import { DecisionBadge, RiskLevelBadge } from '../components/StatusBadge';
import type { RiskDecisionAudit } from '../types';

export const Audits: React.FC = () => {
  const [audits, setAudits] = useState<RiskDecisionAudit[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedAudit, setSelectedAudit] = useState<RiskDecisionAudit | null>(null);

  const loadAudits = async () => {
    try {
      setLoading(true);
      const data = await auditApi.getAll();
      setAudits(data);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao carregar auditorias');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAudits();
  }, []);

  const handleRowClick = (audit: RiskDecisionAudit) => {
    setSelectedAudit(audit);
  };

  const handleCloseDialog = () => {
    setSelectedAudit(null);
  };

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
        Auditorias & Decisões
      </Typography>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Pull Request</strong></TableCell>
              <TableCell><strong>Decisão Final</strong></TableCell>
              <TableCell><strong>Nível de Risco</strong></TableCell>
              <TableCell><strong>Ambiente</strong></TableCell>
              <TableCell><strong>Regras Impactadas</strong></TableCell>
              <TableCell><strong>Data</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {audits.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <Typography color="text.secondary">Nenhuma auditoria encontrada</Typography>
                </TableCell>
              </TableRow>
            ) : (
              audits.map((audit) => (
                <TableRow
                  key={audit.id}
                  hover
                  sx={{ cursor: 'pointer' }}
                  onClick={() => handleRowClick(audit)}
                >
                  <TableCell>{audit.pullRequestId}</TableCell>
                  <TableCell>
                    <DecisionBadge decision={audit.finalDecision} />
                  </TableCell>
                  <TableCell>
                    <RiskLevelBadge level={audit.riskLevel} />
                  </TableCell>
                  <TableCell>{audit.environment}</TableCell>
                  <TableCell>{audit.impactedBusinessRules.length}</TableCell>
                  <TableCell>
                    {new Date(audit.createdAt).toLocaleString('pt-BR')}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Dialog de detalhes */}
      <Dialog open={!!selectedAudit} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        {selectedAudit && (
          <>
            <DialogTitle>
              Detalhes da Auditoria - {selectedAudit.pullRequestId}
            </DialogTitle>
            <DialogContent>
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">ID da Auditoria</Typography>
                <Typography variant="body1">{selectedAudit.id}</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">Decisão Final</Typography>
                <Box sx={{ mt: 0.5 }}>
                  <DecisionBadge decision={selectedAudit.finalDecision} />
                </Box>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">Nível de Risco</Typography>
                <Box sx={{ mt: 0.5 }}>
                  <RiskLevelBadge level={selectedAudit.riskLevel} />
                </Box>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Regras de Negócio Impactadas ({selectedAudit.impactedBusinessRules.length})
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mt: 0.5 }}>
                  {selectedAudit.impactedBusinessRules.map((rule, index) => (
                    <Chip key={index} label={rule} size="small" />
                  ))}
                </Box>
              </Box>

              {selectedAudit.restrictions && selectedAudit.restrictions.length > 0 && (
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">Restrições</Typography>
                  <Box sx={{ mt: 0.5 }}>
                    {selectedAudit.restrictions.map((restriction, index) => (
                      <Chip key={index} label={restriction} size="small" color="warning" sx={{ mr: 0.5, mb: 0.5 }} />
                    ))}
                  </Box>
                </Box>
              )}

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">Sumário de Incidentes</Typography>
                <Box sx={{ mt: 0.5 }}>
                  {Object.entries(selectedAudit.incidentSummary).map(([severity, count]) => (
                    <Chip key={severity} label={`${severity}: ${count}`} size="small" sx={{ mr: 0.5 }} />
                  ))}
                </Box>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">Criado por</Typography>
                <Typography variant="body1">{selectedAudit.createdBy}</Typography>
              </Box>

              <Box>
                <Typography variant="body2" color="text.secondary">Data de Criação</Typography>
                <Typography variant="body1">
                  {new Date(selectedAudit.createdAt).toLocaleString('pt-BR')}
                </Typography>
              </Box>
            </DialogContent>
            <DialogActions>
              <Button onClick={handleCloseDialog}>Fechar</Button>
            </DialogActions>
          </>
        )}
      </Dialog>
    </Box>
  );
};
