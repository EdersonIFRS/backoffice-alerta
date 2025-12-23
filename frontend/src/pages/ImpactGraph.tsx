// Página de Visualização de Impacto Sistêmico (Grafo de Dependências)
// US#37 - Visualização de Impacto Sistêmico (Mapa de Dependências)

import React, { useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Box,
  Chip,
  Alert,
  CircularProgress,
  FormControlLabel,
  Checkbox,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Grid,
  Card,
  CardContent,
} from '@mui/material';
import {
  Timeline as TimelineIcon,
  Warning as WarningIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
} from '@mui/icons-material';
import { generateImpactGraph } from '../services/impactGraph';
import type {
  BusinessImpactGraphResponse,
  ImpactGraphNode,
  ImpactLevel,
  Domain,
} from '../types/impactGraph';

const ImpactGraph: React.FC = () => {
  const [pullRequestId, setPullRequestId] = useState('');
  const [changedFiles, setChangedFiles] = useState('');
  const [graphData, setGraphData] = useState<BusinessImpactGraphResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Filtros
  const [showOnlyCritical, setShowOnlyCritical] = useState(false);
  const [showOnlyCascade, setShowOnlyCascade] = useState(false);
  const [domainFilter, setDomainFilter] = useState<Domain | ''>('');

  const handleGenerateGraph = async () => {
    if (!pullRequestId || !changedFiles) {
      setError('Por favor, preencha PR ID e arquivos alterados');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const filesArray = changedFiles
        .split('\n')
        .map((f) => f.trim())
        .filter((f) => f.length > 0);

      const response = await generateImpactGraph({
        pullRequestId,
        changedFiles: filesArray,
      });

      setGraphData(response);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao gerar grafo de impacto');
      console.error('Erro:', err);
    } finally {
      setLoading(false);
    }
  };

  const getFilteredNodes = (): ImpactGraphNode[] => {
    if (!graphData) return [];

    let filtered = graphData.nodes;

    if (showOnlyCritical) {
      filtered = filtered.filter((n) => n.criticality === 'CRITICA');
    }

    if (showOnlyCascade) {
      filtered = filtered.filter((n) => n.impactLevel === 'CASCADE');
    }

    if (domainFilter) {
      filtered = filtered.filter((n) => n.domain === domainFilter);
    }

    return filtered;
  };

  const getImpactLevelColor = (level: ImpactLevel): string => {
    switch (level) {
      case 'DIRECT':
        return '#2196f3'; // Azul
      case 'INDIRECT':
        return '#ff9800'; // Amarelo/Laranja
      case 'CASCADE':
        return '#f44336'; // Vermelho
      default:
        return '#9e9e9e';
    }
  };

  const getCriticalityColor = (criticality: string): string => {
    switch (criticality) {
      case 'CRITICA':
        return '#d32f2f';
      case 'ALTA':
        return '#f57c00';
      case 'MEDIA':
        return '#fbc02d';
      case 'BAIXA':
        return '#388e3c';
      default:
        return '#757575';
    }
  };

  const filteredNodes = getFilteredNodes();

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        {/* Header */}
        <Box display="flex" alignItems="center" gap={2} mb={3}>
          <TimelineIcon fontSize="large" color="primary" />
          <Box>
            <Typography variant="h4" component="h1" gutterBottom>
              🗺️ Visualização de Impacto Sistêmico
            </Typography>
            <Typography variant="body2" color="text.secondary">
              US#37 - Mapa de Dependências entre Regras de Negócio
            </Typography>
          </Box>
        </Box>

        <Alert severity="info" sx={{ mb: 3 }}>
          ⚠️ <strong>Visualização explicativa</strong> - Esta ferramenta apenas organiza e exibe dados existentes.
          Não altera decisões, não recalcula riscos e não gera notificações.
        </Alert>

        {/* Form */}
        <Box component="form" sx={{ mb: 4 }}>
          <TextField
            fullWidth
            label="Pull Request ID"
            value={pullRequestId}
            onChange={(e) => setPullRequestId(e.target.value)}
            placeholder="PR-789"
            sx={{ mb: 2 }}
          />

          <TextField
            fullWidth
            multiline
            rows={4}
            label="Arquivos Alterados (um por linha)"
            value={changedFiles}
            onChange={(e) => setChangedFiles(e.target.value)}
            placeholder="src/main/java/com/app/payment/PaymentService.java"
            sx={{ mb: 2 }}
          />

          <Button
            variant="contained"
            color="primary"
            onClick={handleGenerateGraph}
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} /> : <TimelineIcon />}
          >
            {loading ? 'Gerando Grafo...' : 'Gerar Grafo de Impacto'}
          </Button>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        {/* Summary */}
        {graphData && (
          <>
            <Typography variant="h6" gutterBottom sx={{ mt: 4 }}>
              📊 Sumário Executivo
            </Typography>

            <Grid container spacing={2} sx={{ mb: 4 }}>
              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="text.secondary" gutterBottom>
                      Total de Regras
                    </Typography>
                    <Typography variant="h4">{graphData.summary.totalRules}</Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="text.secondary" gutterBottom>
                      Impacto Direto
                    </Typography>
                    <Typography variant="h4" color="#2196f3">
                      {graphData.summary.direct}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="text.secondary" gutterBottom>
                      Impacto Indireto
                    </Typography>
                    <Typography variant="h4" color="#ff9800">
                      {graphData.summary.indirect}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="text.secondary" gutterBottom>
                      Impacto Cascata
                    </Typography>
                    <Typography variant="h4" color="#f44336">
                      {graphData.summary.cascade}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>

            {graphData.summary.requiresExecutiveAttention && (
              <Alert severity="warning" icon={<WarningIcon />} sx={{ mb: 3 }}>
                <strong>⚠️ Atenção Executiva Requerida!</strong><br />
                Existem regras <strong>CRÍTICAS</strong> com impacto indireto ou em cascata.
              </Alert>
            )}

            {/* Filters */}
            <Typography variant="h6" gutterBottom>
              🔍 Filtros
            </Typography>

            <Box sx={{ mb: 3, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={showOnlyCritical}
                    onChange={(e) => setShowOnlyCritical(e.target.checked)}
                  />
                }
                label="Apenas Críticas"
              />

              <FormControlLabel
                control={
                  <Checkbox
                    checked={showOnlyCascade}
                    onChange={(e) => setShowOnlyCascade(e.target.checked)}
                  />
                }
                label="Apenas Cascata"
              />

              <FormControl sx={{ minWidth: 200 }}>
                <InputLabel>Domínio</InputLabel>
                <Select
                  value={domainFilter}
                  onChange={(e) => setDomainFilter(e.target.value as Domain | '')}
                  label="Domínio"
                >
                  <MenuItem value="">Todos</MenuItem>
                  <MenuItem value="PAYMENT">Pagamento</MenuItem>
                  <MenuItem value="BILLING">Faturamento</MenuItem>
                  <MenuItem value="ORDER">Pedido</MenuItem>
                  <MenuItem value="USER">Usuário</MenuItem>
                  <MenuItem value="GENERIC">Genérico</MenuItem>
                </Select>
              </FormControl>
            </Box>

            {/* Nodes List */}
            <Typography variant="h6" gutterBottom>
              📍 Regras Impactadas ({filteredNodes.length})
            </Typography>

            {filteredNodes.length === 0 ? (
              <Alert severity="info">Nenhuma regra encontrada com os filtros aplicados.</Alert>
            ) : (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                {filteredNodes.map((node) => (
                  <Card
                    key={node.ruleId}
                    sx={{
                      borderLeft: `6px solid ${getImpactLevelColor(node.impactLevel)}`,
                      borderColor:
                        node.criticality === 'CRITICA'
                          ? '#d32f2f'
                          : getImpactLevelColor(node.impactLevel),
                      borderWidth: node.criticality === 'CRITICA' ? 3 : 1,
                    }}
                  >
                    <CardContent>
                      <Box display="flex" alignItems="center" justifyContent="space-between" mb={1}>
                        <Typography variant="h6">
                          {node.hasIncidents && (
                            <ErrorIcon
                              fontSize="small"
                              color="error"
                              sx={{ mr: 1, verticalAlign: 'middle' }}
                            />
                          )}
                          {node.ruleName}
                        </Typography>

                        <Box display="flex" gap={1}>
                          <Chip
                            label={node.impactLevel}
                            size="small"
                            sx={{
                              backgroundColor: getImpactLevelColor(node.impactLevel),
                              color: 'white',
                            }}
                          />
                          <Chip
                            label={node.criticality}
                            size="small"
                            sx={{
                              backgroundColor: getCriticalityColor(node.criticality),
                              color: 'white',
                            }}
                          />
                        </Box>
                      </Box>

                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        <strong>ID:</strong> {node.ruleId} | <strong>Domínio:</strong> {node.domain}
                      </Typography>

                      {node.ownerships.length > 0 && (
                        <Box mt={1}>
                          <Typography variant="caption" color="text.secondary">
                            Ownership:
                          </Typography>
                          {node.ownerships.map((owner, idx) => (
                            <Chip
                              key={idx}
                              label={`${owner.teamName} (${owner.role})`}
                              size="small"
                              variant="outlined"
                              sx={{ ml: 1, mt: 0.5 }}
                            />
                          ))}
                        </Box>
                      )}

                      {node.hasIncidents && (
                        <Alert severity="warning" sx={{ mt: 1 }} icon={<WarningIcon fontSize="small" />}>
                          Esta regra possui histórico de incidentes
                        </Alert>
                      )}
                    </CardContent>
                  </Card>
                ))}
              </Box>
            )}

            {/* Edges Info */}
            {graphData.edges.length > 0 && (
              <>
                <Typography variant="h6" gutterBottom sx={{ mt: 4 }}>
                  ➡️ Dependências ({graphData.edges.length})
                </Typography>

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  {graphData.edges.map((edge, idx) => (
                    <Paper key={idx} sx={{ p: 2 }}>
                      <Typography variant="body2">
                        <strong>{edge.sourceRuleId}</strong> → <strong>{edge.targetRuleId}</strong>
                        <Chip
                          label={edge.dependencyType}
                          size="small"
                          variant="outlined"
                          sx={{ ml: 2 }}
                        />
                      </Typography>
                    </Paper>
                  ))}
                </Box>
              </>
            )}
          </>
        )}
      </Paper>
    </Container>
  );
};

export default ImpactGraph;
