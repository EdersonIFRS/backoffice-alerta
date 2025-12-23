package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request para autenticação de usuário
 * 
 * US#29 - Autenticação, Autorização e RBAC
 */
@Schema(description = "Request de autenticação com username e password")
public class LoginRequest {

    @Schema(description = "Nome de usuário", example = "admin")
    private String username;
    
    @Schema(description = "Senha do usuário", example = "admin123")
    private String password;

    // Constructors
    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
