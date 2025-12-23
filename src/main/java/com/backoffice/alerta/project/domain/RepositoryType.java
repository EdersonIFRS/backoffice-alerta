// US#48 - Tipo de repositório de código
package com.backoffice.alerta.project.domain;

/**
 * Tipos de repositório de código suportados.
 * 
 * Define o provedor de controle de versão do projeto.
 * Nesta US não há integração real, apenas metadados.
 */
public enum RepositoryType {
    /**
     * Repositório Git genérico (self-hosted ou outro provedor)
     */
    GIT,
    
    /**
     * GitHub (github.com ou GitHub Enterprise)
     */
    GITHUB,
    
    /**
     * GitLab (gitlab.com ou GitLab self-hosted)
     */
    GITLAB,
    
    /**
     * Atlassian Bitbucket (Cloud ou Server)
     */
    BITBUCKET
}
