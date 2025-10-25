package com.connectrh.bff.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Lida com exceções de autenticação (401 Unauthorized) durante o processamento do JWT.
 * Acionado quando o usuário tenta acessar um recurso protegido sem credenciais válidas.
 * Esta implementação agora retorna uma resposta formatada em JSON.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Este método é invocado quando um usuário não autenticado tenta acessar um recurso protegido.
     * Agora configura o Content-Type como JSON e escreve o corpo da resposta de erro.
     *
     * @param request       A requisição HTTP.
     * @param response      A resposta HTTP.
     * @param authException A exceção de autenticação lançada.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 1. Configurar o cabeçalho para JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 2. Definir o status 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 3. Escrever o corpo da resposta em formato JSON
        final String jsonResponse = "{"
                + "\"status\": 401,"
                + "\"error\": \"Unauthorized\","
                + "\"message\": \"Acesso negado. Token JWT ausente ou inválido.\""
                + "}";

        response.getWriter().write(jsonResponse);
    }
}
