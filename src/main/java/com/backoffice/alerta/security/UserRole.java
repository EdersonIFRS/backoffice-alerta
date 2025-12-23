package com.backoffice.alerta.security;

/**
 * Papéis de usuário para controle de acesso baseado em roles (RBAC)
 * 
 * US#29 - Autenticação, Autorização e RBAC
 */
public enum UserRole {
    
    /**
     * Administrador - acesso total ao sistema
     */
    ADMIN("Administrador"),
    
    /**
     * Gerente de Risco - acesso a decisões, métricas, auditorias e SLA
     */
    RISK_MANAGER("Gerente de Risco"),
    
    /**
     * Engenheiro - acesso a análises e recomendações
     */
    ENGINEER("Engenheiro"),
    
    /**
     * Visualizador - acesso apenas leitura a dashboards e métricas
     */
    VIEWER("Visualizador");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Retorna a role com prefixo ROLE_ para Spring Security
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
