// US#39 - Ações executivas (copiar, exportar, etc)
import React from 'react';
import {
  Paper,
  Box,
  Button,
  Alert,
  Snackbar
} from '@mui/material';
import {
  ContentCopy as CopyIcon,
  PictureAsPdf as PdfIcon,
  Email as EmailIcon,
  Refresh as RefreshIcon
} from '@mui/icons-material';
import type { ExecutiveImpactExplainResponse } from '../types/executiveImpact';

interface ExecutiveImpactActionsProps {
  data: ExecutiveImpactExplainResponse;
  onNewAnalysis: () => void;
}

export const ExecutiveImpactActions: React.FC<ExecutiveImpactActionsProps> = ({ data, onNewAnalysis }) => {
  const [snackbar, setSnackbar] = React.useState({ open: false, message: '' });

  const handleCopySummary = async () => {
    const summary = `
ANÁLISE EXECUTIVA DE IMPACTO SISTÊMICO

Pull Request: ${data.pullRequestId}
Risco Geral: ${data.overallRiskLevel}
Confiança: ${data.confidenceLevel}
Gerado em: ${new Date(data.generatedAt).toLocaleString('pt-BR')}

${data.executiveSummary.headline}

IMPACTO NO NEGÓCIO:
${data.executiveSummary.businessImpact}

ÁREAS AFETADAS:
${data.executiveSummary.areasAffected.join(', ')}

INTERPRETAÇÃO DE RISCO:
${data.executiveSummary.riskInterpretation}

RECOMENDAÇÕES:
${data.executiveSummary.recommendation}

---
Esta análise é consultiva e não substitui a decisão formal de risco.
    `.trim();

    try {
      await navigator.clipboard.writeText(summary);
      setSnackbar({ open: true, message: 'Resumo copiado!' });
    } catch (err) {
      setSnackbar({ open: true, message: 'Erro ao copiar' });
    }
  };

  const handleExportPdf = () => {
    // Implementação simplificada - usar window.print()
    // Em produção, usar jsPDF ou html2pdf.js
    window.print();
    setSnackbar({ open: true, message: 'Use Ctrl+P para salvar como PDF' });
  };

  const handleCopyToEmail = async () => {
    const emailBody = `
Olá,

Segue análise executiva de impacto sistêmico:

PR: ${data.pullRequestId}
Risco: ${data.overallRiskLevel}

${data.executiveSummary.headline}

${data.executiveSummary.businessImpact}

Áreas: ${data.executiveSummary.areasAffected.join(', ')}

Recomendações:
${data.executiveSummary.recommendation}

---
Gerado em ${new Date(data.generatedAt).toLocaleString('pt-BR')}
Esta análise é consultiva e não substitui a decisão formal de risco.
    `.trim();

    try {
      await navigator.clipboard.writeText(emailBody);
      setSnackbar({ open: true, message: 'Texto copiado para email!' });
    } catch (err) {
      setSnackbar({ open: true, message: 'Erro ao copiar' });
    }
  };

  return (
    <>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Alert severity="info" sx={{ mb: 2 }}>
          <strong>Aviso Legal:</strong> Esta análise é consultiva e não substitui a decisão formal de risco.
        </Alert>

        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          <Button
            variant="outlined"
            startIcon={<CopyIcon />}
            onClick={handleCopySummary}
          >
            Copiar Resumo
          </Button>

          <Button
            variant="outlined"
            startIcon={<PdfIcon />}
            onClick={handleExportPdf}
          >
            Exportar PDF
          </Button>

          <Button
            variant="outlined"
            startIcon={<EmailIcon />}
            onClick={handleCopyToEmail}
          >
            Copiar p/ Email
          </Button>

          <Button
            variant="contained"
            startIcon={<RefreshIcon />}
            onClick={onNewAnalysis}
          >
            Nova Análise
          </Button>
        </Box>
      </Paper>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        message={snackbar.message}
      />
    </>
  );
};
