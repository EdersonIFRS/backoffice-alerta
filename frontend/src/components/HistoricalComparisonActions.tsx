// US#42 - Ações Executivas
import React from 'react';
import { Paper, Button, Box, Typography } from '@mui/material';
import { ContentCopy, PictureAsPdf, Email, Refresh } from '@mui/icons-material';
import type { DecisionHistoricalComparisonResponse } from '../types/historicalComparison';

interface Props {
  data: DecisionHistoricalComparisonResponse | null;
  onReset: () => void;
}

export const HistoricalComparisonActions: React.FC<Props> = ({ data, onReset }) => {
  const handleCopyExecutiveSummary = () => {
    if (!data) return;

    const { currentContextSummary, historicalComparisons, executiveInsights } = data;
    
    const summary = `
COMPARAÇÃO HISTÓRICA DE DECISÕES DE RISCO

DECISÃO ATUAL:
- Nível de Risco: ${currentContextSummary.riskLevel}
- Decisão Final: ${currentContextSummary.finalDecision}
- Regras Críticas: ${currentContextSummary.criticalRules}
- Domínios: ${currentContextSummary.businessDomains.join(', ')}

HISTÓRICO SIMILAR:
- Comparações Encontradas: ${historicalComparisons.length}
- Taxa de Bloqueio: ${Math.round((historicalComparisons.filter(h => h.decision === 'BLOQUEADO').length / historicalComparisons.length) * 100)}%

INSIGHTS:
${executiveInsights.patternDetected ? '⚠️ PADRÃO DETECTADO' : '✅ SEM PADRÕES CRÍTICOS'}
${executiveInsights.patternDescription}

RECOMENDAÇÃO:
${executiveInsights.recommendation}
    `.trim();

    navigator.clipboard.writeText(summary);
    alert('Resumo executivo copiado para a área de transferência!');
  };

  const handleExportPDF = () => {
    alert('📄 Exportação para PDF será implementada em versão futura');
  };

  const handleCopyEmail = () => {
    if (!data) return;

    const { executiveInsights } = data;
    
    const emailBody = `
Prezados,

Segue análise comparativa da decisão de risco com histórico similar:

${executiveInsights.patternDetected 
  ? '⚠️ Um padrão recorrente foi detectado e requer atenção.'
  : '✅ A decisão está alinhada com o histórico.'
}

${executiveInsights.patternDescription}

Recomendação: ${executiveInsights.recommendation}

Atenciosamente,
Sistema de Gestão de Risco
    `.trim();

    const mailtoLink = `mailto:?subject=Comparação Histórica de Decisões de Risco&body=${encodeURIComponent(emailBody)}`;
    window.location.href = mailtoLink;
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>
        Ações Executivas
      </Typography>
      
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
        <Button
          variant="outlined"
          startIcon={<ContentCopy />}
          onClick={handleCopyExecutiveSummary}
          disabled={!data}
        >
          Copiar Resumo Executivo
        </Button>

        <Button
          variant="outlined"
          startIcon={<PictureAsPdf />}
          onClick={handleExportPDF}
          disabled={!data}
        >
          Exportar para PDF
        </Button>

        <Button
          variant="outlined"
          startIcon={<Email />}
          onClick={handleCopyEmail}
          disabled={!data}
        >
          Copiar para E-mail
        </Button>

        <Button
          variant="contained"
          startIcon={<Refresh />}
          onClick={onReset}
          color="secondary"
        >
          Nova Comparação
        </Button>
      </Box>
    </Paper>
  );
};
