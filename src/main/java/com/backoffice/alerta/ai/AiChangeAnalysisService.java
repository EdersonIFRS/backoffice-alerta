package com.backoffice.alerta.ai;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço de análise inteligente simulada baseada em heurísticas.
 * NÃO utiliza IA generativa real.
 */
@Service
public class AiChangeAnalysisService {

    private static final String[] HIGH_IMPACT_KEYWORDS = {"billing", "payment", "pricing", "order"};
    private static final String[] MEDIUM_IMPACT_KEYWORDS = {"controller", "api"};
    private static final String[] LOW_IMPACT_KEYWORDS = {"util", "config", "test"};

    public AiAnalysisResponse analyzeChange(AiAnalysisRequest request) {
        List<AiSignal> signals = new ArrayList<>();
        
        // 1. Analisar impacto de negócio
        boolean hasHighImpact = false;
        boolean hasLowImpact = false;
        String domain = null;

        for (AiAnalysisRequest.FileChange file : request.getFiles()) {
            String lowerPath = file.getFilePath().toLowerCase();
            
            // Verificar alto impacto
            for (String keyword : HIGH_IMPACT_KEYWORDS) {
                if (lowerPath.contains(keyword)) {
                    hasHighImpact = true;
                    domain = keyword;
                    break;
                }
            }
            
            // Verificar baixo impacto
            for (String keyword : LOW_IMPACT_KEYWORDS) {
                if (lowerPath.contains(keyword)) {
                    hasLowImpact = true;
                    break;
                }
            }
        }

        if (hasHighImpact) {
            signals.add(AiSignal.HIGH_BUSINESS_IMPACT);
        } else if (hasLowImpact) {
            signals.add(AiSignal.LOW_BUSINESS_IMPACT);
        }

        // 2. Analisar tipo de mudança por volume
        int totalLines = request.getFiles().stream()
            .mapToInt(AiAnalysisRequest.FileChange::getLinesChanged)
            .sum();

        boolean isLogicChange = false;
        boolean isStructuralChange = false;
        boolean isCosmeticChange = false;

        if (totalLines > 100) {
            signals.add(AiSignal.LOGIC_CHANGE);
            isLogicChange = true;
        } else if (totalLines >= 30) {
            signals.add(AiSignal.STRUCTURAL_CHANGE);
            isStructuralChange = true;
        } else {
            signals.add(AiSignal.COSMETIC_CHANGE);
            isCosmeticChange = true;
        }

        // 3. Calcular confidence
        double confidence = 0.6;
        if (hasHighImpact) {
            confidence += 0.15;
        }
        if (totalLines > 100) {
            confidence += 0.10;
        }
        confidence = Math.min(0.95, confidence);

        // 4. Gerar summary
        String summary = generateSummary(hasHighImpact, hasLowImpact, isLogicChange, 
                                        isStructuralChange, isCosmeticChange, domain);

        // 5. Determinar atenção recomendada
        AttentionLevel attention = determineAttention(hasHighImpact, isLogicChange);

        AiAssessment assessment = new AiAssessment(summary, confidence, attention, signals);
        return new AiAnalysisResponse(request.getPullRequestId(), assessment);
    }

    private String generateSummary(boolean hasHighImpact, boolean hasLowImpact, 
                                   boolean isLogicChange, boolean isStructuralChange, 
                                   boolean isCosmeticChange, String domain) {
        if (hasHighImpact && isLogicChange) {
            return String.format("Mudança altera lógica sensível de %s", domain);
        } else if (hasHighImpact && isStructuralChange) {
            return String.format("Mudança estrutural em módulo de %s", domain);
        } else if (hasHighImpact) {
            return String.format("Mudança em módulo crítico de %s", domain);
        } else if (hasLowImpact && isCosmeticChange) {
            return "Mudança de baixo impacto com alterações cosméticas";
        } else if (hasLowImpact) {
            return "Mudança em módulo de suporte com baixo impacto";
        } else if (isLogicChange) {
            return "Mudança altera lógica de negócio";
        } else if (isStructuralChange) {
            return "Mudança estrutural moderada";
        } else {
            return "Mudança cosmética de baixo impacto";
        }
    }

    private AttentionLevel determineAttention(boolean hasHighImpact, boolean isLogicChange) {
        if (hasHighImpact && isLogicChange) {
            return AttentionLevel.ALTA;
        } else if (hasHighImpact || isLogicChange) {
            return AttentionLevel.MEDIA;
        } else {
            return AttentionLevel.BAIXA;
        }
    }
}
