package com.backoffice.alerta.timeline;

import java.time.Instant;
import java.util.Map;

/**
 * US#40 - Evento na linha do tempo de decisão
 * 
 * Representa um único evento no histórico de uma mudança.
 */
public record TimelineEventResponse(
    String id,
    TimelineEventType eventType,
    String title,
    String description,
    Instant createdAt,
    String actor,  // SYSTEM, USER, AI
    String severity,  // INFO, WARNING, CRITICAL
    String relatedEntityId,
    Map<String, String> metadata
) {}
