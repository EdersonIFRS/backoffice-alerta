package com.backoffice.alerta.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * US#73 - Correlation ID Filter
 * 
 * Implementa rastreamento de requisições via X-Correlation-ID header.
 * 
 * Funcionalidades:
 * - Extrai correlation ID do header X-Correlation-ID
 * - Gera UUID se não fornecido
 * - Adiciona ao MDC para logging
 * - Adiciona ao response header
 * - Limpa MDC após request
 * 
 * Observabilidade:
 * - Permite rastrear requisição end-to-end
 * - Facilita troubleshooting em logs
 * - Suporta distributed tracing
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlation_id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        String correlationId = extractOrGenerateCorrelationId(request);
        
        try {
            // Adicionar ao MDC para logs
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            
            // Adicionar ao response header
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            log.debug("US#73 - Request iniciada com correlation_id={}", correlationId);
            
            // Continuar chain
            filterChain.doFilter(request, response);
            
        } finally {
            // Limpar MDC (critical para thread pool)
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    /**
     * Extrai correlation ID do header ou gera novo UUID
     */
    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            log.trace("US#73 - Correlation ID gerado: {}", correlationId);
        } else {
            log.trace("US#73 - Correlation ID recebido: {}", correlationId);
        }
        
        return correlationId;
    }
}
