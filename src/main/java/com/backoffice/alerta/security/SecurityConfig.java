package com.backoffice.alerta.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuração de segurança Spring Security com JWT
 * 
 * Define regras de autenticação, autorização e RBAC para todos os endpoints.
 * 
 * US#29 - Autenticação, Autorização e RBAC
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final InMemoryUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(InMemoryUserDetailsService userDetailsService,
                         JwtAuthenticationFilter jwtAuthenticationFilter,
                         CorsConfigurationSource corsConfigurationSource) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/auth/**", "/webhooks/**", "/risk/business-impact/**", "/risk/decision/**", "/risk/rag/**", "/risk/chat/**", "/risk/git/**", "/risk/ci/**", "/risk/alerts/**", "/risk/rules/**", "/risk/llm/**", "/risk/projects/**", "/projects/**", "/api/projects/**", "/api/business-rules/**", "/api/alerts/**") // Desabilita CSRF para endpoints públicos e APIs (US#53, US#55, US#56, US#57, US#61, US#68, US#70, US#72)
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // Permite frames para H2 Console
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (sem JWT) - ORDEM IMPORTA!
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(
                    "/swagger-ui/**", 
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                .requestMatchers("/h2-console/**").permitAll() // H2 Console (apenas dev)
                .requestMatchers("/health").permitAll()
                .requestMatchers("/risk/dashboard/executive/health").permitAll()
                .requestMatchers("/webhooks/pull-request").permitAll()
                
                // RISK_MANAGER - decisão, métricas, auditoria, SLA, dashboard, notificações, RAG, alertas
                // IMPORTANTE: mais específico primeiro!
                .requestMatchers(HttpMethod.POST, "/risk/decision/historical-comparison").hasAnyRole("ADMIN", "RISK_MANAGER")
                .requestMatchers("/risk/decision/**").hasAnyRole("ADMIN", "RISK_MANAGER")
                .requestMatchers("/risk/decision").hasAnyRole("ADMIN", "RISK_MANAGER")
                .requestMatchers("/risk/rag/**").hasAnyRole("ADMIN", "RISK_MANAGER")
                .requestMatchers("/risk/chat/**").hasAnyRole("ADMIN", "RISK_MANAGER", "ENGINEER")
                .requestMatchers("/risk/git/**").hasAnyRole("ADMIN", "RISK_MANAGER", "ENGINEER") // US#51 - Git PR Analysis
                .requestMatchers("/risk/ci/**").hasAnyRole("ADMIN", "RISK_MANAGER", "ENGINEER") // US#53 - CI/CD Gate Integration
                .requestMatchers("/risk/llm/**").hasAnyRole("ADMIN", "RISK_MANAGER", "ENGINEER") // US#70 - LLM Change Detection
                
                // US#72 - Project Onboarding (ADMIN only)
                .requestMatchers(HttpMethod.POST, "/risk/projects/onboarding/start").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/risk/projects/onboarding/status/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/risk/projects/onboarding/health").authenticated()
                
                // US#57 e US#58 - Preferências de Alertas (ANTES de /risk/alerts/**)
                .requestMatchers(HttpMethod.POST, "/api/projects/*/alert-preferences").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/projects/*/alert-preferences").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/business-rules/*/alert-preferences").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/projects/*/alert-preferences").hasAnyRole("ADMIN", "RISK_MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/business-rules/*/alert-preferences").hasAnyRole("ADMIN", "RISK_MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/alerts/preferences/**").hasAnyRole("ADMIN", "RISK_MANAGER")
                
                // US#59 - Histórico de Notificações (ANTES de /risk/alerts/**)
                .requestMatchers(HttpMethod.GET, "/risk/alerts/history/**").hasAnyRole("ADMIN", "RISK_MANAGER")
                
                // US#60 - Dashboard Executivo de Alertas
                .requestMatchers(HttpMethod.GET, "/risk/dashboard/alerts-executive").hasAnyRole("ADMIN", "RISK_MANAGER")
                
                // US#61 - Auditoria Detalhada de Alertas (ANTES de /risk/alerts/**)
                .requestMatchers(HttpMethod.GET, "/risk/alerts/audit/**").hasAnyRole("ADMIN", "RISK_MANAGER")
                
                // US#67 - Avaliação de Qualidade do RAG
                .requestMatchers(HttpMethod.GET, "/risk/rag/quality/**").hasAnyRole("ADMIN", "RISK_MANAGER")
                
                // Lista de projetos para dropdown do chat
                .requestMatchers(HttpMethod.GET, "/risk/projects").hasAnyRole("ADMIN", "RISK_MANAGER")
                
                // US#68 - Importação Automática de Regras de Negócio
                .requestMatchers(HttpMethod.POST, "/risk/rules/import").hasRole("ADMIN")
                
                .requestMatchers("/risk/alerts/**").hasAnyRole("ADMIN", "RISK_MANAGER") // US#55 - Alertas Inteligentes
                .requestMatchers("/risk/metrics/**").hasAnyRole("ADMIN", "RISK_MANAGER", "VIEWER")
                .requestMatchers("/risk/audit/**").hasAnyRole("ADMIN", "RISK_MANAGER", "VIEWER")
                .requestMatchers("/risk/sla/**").hasAnyRole("ADMIN", "RISK_MANAGER")
                .requestMatchers("/risk/dashboard/executive").hasAnyRole("ADMIN", "RISK_MANAGER", "VIEWER")
                .requestMatchers("/risk/notifications/**").hasAnyRole("ADMIN", "RISK_MANAGER", "ENGINEER", "VIEWER")
                
                // ENGINEER - análises, recomendações (exceto executive-explain)
                .requestMatchers("/risk/analyze/**").hasAnyRole("ADMIN", "RISK_MANAGER", "ENGINEER")
                .requestMatchers("/risk/business-impact/graph/**").hasAnyRole("ADMIN", "RISK_MANAGER", "ENGINEER")
                .requestMatchers("/risk/business-impact/executive-explain").hasAnyRole("ADMIN", "RISK_MANAGER")
                .requestMatchers("/risk/recommend").hasAnyRole("ADMIN", "RISK_MANAGER", "ENGINEER")
                
                // ENGINEER NÃO pode criar regras de negócio
                .requestMatchers(HttpMethod.POST, "/business-rules").hasAnyRole("ADMIN", "RISK_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/business-rules/**").hasAnyRole("ADMIN", "RISK_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/business-rules/**").hasAnyRole("ADMIN", "RISK_MANAGER")
                
                // US#48 - Projetos (ADMIN cria, todos leem)
                .requestMatchers(HttpMethod.POST, "/projects").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/projects/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/projects/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/projects").hasAnyRole("ADMIN", "RISK_MANAGER")
                .requestMatchers(HttpMethod.GET, "/projects/active").authenticated() // US#58 - Todos autenticados
                .requestMatchers(HttpMethod.GET, "/projects/**").hasAnyRole("ADMIN", "RISK_MANAGER", "ENGINEER")
                
                // US#58 - Frontend de Preferências de Alertas (endpoints auxiliares)
                .requestMatchers(HttpMethod.GET, "/business-rules").hasAnyRole("ADMIN", "RISK_MANAGER", "ENGINEER")
                
                // VIEWER - apenas leitura de dashboard, métricas, auditoria, notificações
                // (já coberto acima)
                
                // Qualquer outra requisição requer autenticação
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(userDetailsService.getPasswordEncoder());
        
        return authenticationManagerBuilder.build();
    }
}
