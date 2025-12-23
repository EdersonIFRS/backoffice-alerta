package com.backoffice.alerta.timeline;

import java.util.List;

/**
 * US#40 - Resposta completa da linha do tempo de decisão
 * 
 * Agrega todos os eventos relacionados a um Pull Request
 * em ordem cronológica.
 */
public record ChangeTimelineResponse(
    String pullRequestId,
    String environment,
    String finalDecision,
    String overallRiskLevel,
    boolean requiresExecutiveAttention,
    List<TimelineEventResponse> events
) {}
