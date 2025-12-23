package com.backoffice.alerta.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço de usuários in-memory para autenticação
 * 
 * Contém usuários mockados para testes de autenticação e autorização.
 * 
 * US#29 - Autenticação, Autorização e RBAC
 */
@Service
public class InMemoryUserDetailsService implements UserDetailsService {

    private final Map<String, UserPrincipal> users = new HashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public InMemoryUserDetailsService() {
        // Inicializa usuários mockados
        initializeUsers();
    }

    private void initializeUsers() {
        // admin / admin123 → ADMIN
        users.put("admin", new UserPrincipal(
                "admin",
                passwordEncoder.encode("admin123"),
                List.of(UserRole.ADMIN)
        ));

        // risk / risk123 → RISK_MANAGER
        users.put("risk", new UserPrincipal(
                "risk",
                passwordEncoder.encode("risk123"),
                List.of(UserRole.RISK_MANAGER)
        ));

        // engineer / eng123 → ENGINEER
        users.put("engineer", new UserPrincipal(
                "engineer",
                passwordEncoder.encode("eng123"),
                List.of(UserRole.ENGINEER)
        ));

        // viewer / view123 → VIEWER
        users.put("viewer", new UserPrincipal(
                "viewer",
                passwordEncoder.encode("view123"),
                List.of(UserRole.VIEWER)
        ));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserPrincipal user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }
        return user;
    }

    /**
     * Retorna o password encoder utilizado
     */
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
