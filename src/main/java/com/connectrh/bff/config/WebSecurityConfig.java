package com.connectrh.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança do BFF (Backend for Frontend).
 * Esta classe é responsável por:
 * 1. Definir que a sessão é Stateless (baseada em JWT).
 * 2. Permitir acesso público ao endpoint de login (/auth/login).
 * 3. Proteger todas as demais rotas, exigindo um JWT válido.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Define a cadeia de filtros de segurança para o BFF.
     *
     * @param http Configuração HTTP.
     * @return O SecurityFilterChain configurado.
     * @throws Exception Se houver erro na configuração.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Configurações básicas
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs
                .cors(cors -> {
                }) // Permite configuração de CORS (se necessário futuramente)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // FUTURAMENTE: Adicionar o filtro JWT aqui
                // .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)

                // 2. Regras de autorização
                .authorizeHttpRequests(auth -> auth
                        // CORREÇÃO: Usando a rota completa /api/v1/auth/** para garantir que o login
                        // seja permitido, mesmo com o prefixo do controller.
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Exemplo: Rotas de saúde também públicas
                        .requestMatchers("/actuator/**").permitAll()

                        // Todas as outras requisições DEVEM ser autenticadas (requerem um JWT)
                        .anyRequest().authenticated()
                );

        // FUTURAMENTE: Adicionar tratamento de exceção para 401 (JWT inválido ou ausente)

        return http.build();
    }

    // Você não precisa de um PasswordEncoder aqui, mas ele já foi configurado em PasswordEncoderConfig.java
}
