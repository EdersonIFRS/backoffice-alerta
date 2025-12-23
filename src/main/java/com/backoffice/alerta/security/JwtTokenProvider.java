package com.backoffice.alerta.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provider para geração e validação de tokens JWT
 * 
 * US#29 - Autenticação, Autorização e RBAC
 */
@Component
public class JwtTokenProvider {

    // Chave secreta para assinar o JWT (em produção, usar variável de ambiente)
    private final SecretKey jwtSecret;
    
    // Token expira em 24 horas
    private final long jwtExpirationMs = 86400000L;

    public JwtTokenProvider() {
        String secret = "backoffice-alerta-secret-key-change-in-production-minimum-512-bits-required-for-hs512-algorithm";
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gera um token JWT a partir da autenticação
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Instant now = Instant.now();
        Instant expiryDate = now.plus(jwtExpirationMs, ChronoUnit.MILLIS);
        
        // Remove o prefixo "ROLE_" das roles para o token JWT
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(jwtSecret)
                .compact();
    }

    /**
     * Extrai o username do token JWT
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }

    /**
     * Valida o token JWT
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(jwtSecret)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (Exception ex) {
            // Token inválido, expirado ou malformado
            return false;
        }
    }

    /**
     * Calcula o timestamp de expiração do token
     */
    public Instant getExpirationTime() {
        return Instant.now().plus(jwtExpirationMs, ChronoUnit.MILLIS);
    }
}
