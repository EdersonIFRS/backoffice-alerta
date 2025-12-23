package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Response de autenticação contendo o token JWT
 * 
 * US#29 - Autenticação, Autorização e RBAC
 */
@Schema(description = "Response de autenticação com JWT token")
public class LoginResponse {

    @Schema(description = "Token JWT para autenticação", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String token;
    
    @Schema(description = "Tipo do token", example = "Bearer")
    private final String type;
    
    @Schema(description = "Username do usuário autenticado", example = "admin")
    private final String username;
    
    @Schema(description = "Roles do usuário", example = "[\"ADMIN\"]")
    private final List<String> roles;
    
    @Schema(description = "Timestamp de emissão do token")
    private final Instant issuedAt;
    
    @Schema(description = "Timestamp de expiração do token")
    private final Instant expiresAt;

    public LoginResponse(String token, String username, List<String> roles, 
                         Instant issuedAt, Instant expiresAt) {
        this.token = token;
        this.type = "Bearer";
        this.username = username;
        this.roles = roles;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
