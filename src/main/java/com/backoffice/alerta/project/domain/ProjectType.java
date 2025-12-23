// US#48 - Tipo de projeto organizacional
package com.backoffice.alerta.project.domain;

/**
 * Tipos de projeto organizacional suportados pelo sistema.
 * 
 * Define a natureza arquitetural do projeto para fins de
 * análise de impacto e governança.
 */
public enum ProjectType {
    /**
     * Aplicação monolítica única
     */
    MONOLITH,
    
    /**
     * Serviço independente (arquitetura microservices)
     */
    MICROSERVICE,
    
    /**
     * Aplicação frontend (SPA, mobile, etc.)
     */
    FRONTEND,
    
    /**
     * Aplicação backend/API
     */
    BACKEND,
    
    /**
     * Biblioteca compartilhada (pacote, módulo)
     */
    LIBRARY
}
