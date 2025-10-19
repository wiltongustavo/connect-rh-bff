package com.connectrh.bff.controller;

import com.connectrh.bff.dto.request.LoginRequest;
import com.connectrh.bff.dto.response.AuthResponse;
import com.connectrh.bff.dto.response.CoreAuthResponse;
import com.connectrh.bff.service.CoreServiceClient;
import com.connectrh.bff.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Controller de Autenticação do BFF.
 * Responsável por receber a requisição de login do Frontend, delegar a validação
 * ao Core Service e gerar o JWT para o Frontend.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final CoreServiceClient coreServiceClient;
    private final JwtUtil jwtUtil;

    public AuthController(CoreServiceClient coreServiceClient, JwtUtil jwtUtil) {
        this.coreServiceClient = coreServiceClient;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Endpoint público para login.
     *
     * @param request Credenciais de login.
     * @return Um JWT se o login for bem-sucedido, ou 401.
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody LoginRequest request) {
        // CORREÇÃO: Usando o método 'internalLogin' do CoreServiceClient.
        return coreServiceClient.internalLogin(request)
                .map(this::createJwtAndResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                    }
                    // Trata outros erros de comunicação/servidor com 500
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Helper method para criar a resposta de sucesso e o JWT.
     */
    private ResponseEntity<AuthResponse> createJwtAndResponse(CoreAuthResponse coreResponse) {
        // Gerar o JWT usando os dados do usuário validados pelo Core
        String token = jwtUtil.generateToken(coreResponse);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setEmail(coreResponse.getEmail());
        authResponse.setName(coreResponse.getName());
        authResponse.setRoles(coreResponse.getRoles());

        return ResponseEntity.ok(authResponse);
    }
}
