package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.LoginRequest;
import com.backoffice.alerta.dto.LoginResponse;
import com.backoffice.alerta.security.JwtTokenProvider;
import com.backoffice.alerta.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller de autenticação JWT
 * 
 * Endpoint público para autenticação de usuários e geração de token JWT.
 * 
 * US#29 - Autenticação, Autorização e RBAC
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints de autenticação e autorização")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                         JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Operation(
        summary = "Autenticar usuário",
        description = "Autentica um usuário e retorna um token JWT válido por 24 horas. " +
                     "Use o token no header Authorization: Bearer {token}."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Autenticação realizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Admin autenticado",
                        value = """
                            {
                              "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXSwiaWF0IjoxNjQwOTk1MjAwLCJleHAiOjE2NDEwODE2MDB9.abc123...",
                              "type": "Bearer",
                              "username": "admin",
                              "roles": ["ADMIN"],
                              "issuedAt": "2025-12-17T23:30:00Z",
                              "expiresAt": "2025-12-18T23:30:00Z"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Risk Manager autenticado",
                        value = """
                            {
                              "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyaXNrIiwicm9sZXMiOlsiUk9MRV9SSVNLX01BTkFHRVIiXSwiaWF0IjoxNjQwOTk1MjAwLCJleHAiOjE2NDEwODE2MDB9.xyz789...",
                              "type": "Bearer",
                              "username": "risk",
                              "roles": ["RISK_MANAGER"],
                              "issuedAt": "2025-12-17T23:30:00Z",
                              "expiresAt": "2025-12-18T23:30:00Z"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Engineer autenticado",
                        value = """
                            {
                              "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbmdpbmVlciIsInJvbGVzIjpbIlJPTEVfRU5HSU5FRVIiXSwiaWF0IjoxNjQwOTk1MjAwLCJleHAiOjE2NDEwODE2MDB9.def456...",
                              "type": "Bearer",
                              "username": "engineer",
                              "roles": ["ENGINEER"],
                              "issuedAt": "2025-12-17T23:30:00Z",
                              "expiresAt": "2025-12-18T23:30:00Z"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            String jwt = tokenProvider.generateToken(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            List<String> roles = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", "")) // Remove prefixo ROLE_
                    .collect(Collectors.toList());

            Instant now = Instant.now();
            Instant expiresAt = tokenProvider.getExpirationTime();

            LoginResponse response = new LoginResponse(
                    jwt,
                    userPrincipal.getUsername(),
                    roles,
                    now,
                    expiresAt
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas");
        }
    }
}
