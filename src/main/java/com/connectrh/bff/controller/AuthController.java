package com.connectrh.bff.controller;

import com.connectrh.bff.dto.request.CreateUserRequest;
import com.connectrh.bff.dto.request.LoginRequest;
import com.connectrh.bff.dto.response.AuthResponse;
import com.connectrh.bff.dto.response.CoreAuthResponse;
import com.connectrh.bff.dto.response.UserCreateResponse;
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
        authResponse.setUserId(coreResponse.getUserId());
        authResponse.setName(coreResponse.getName());
        authResponse.setRoles(coreResponse.getRoles());

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<UserCreateResponse>> register(@RequestBody CreateUserRequest request) {

        // 1. Chama o método reativo do Service/Client
        return coreServiceClient.internalCreateUser(request)

                // 2. Mapeia a resposta de sucesso para 201 Created
                .map(userResponse -> ResponseEntity.status(HttpStatus.CREATED).body(userResponse))

                // 3. Mapeia os erros reativos (exceções) para respostas HTTP
                .onErrorResume(WebClientResponseException.class, e -> {

                    // Mapeia 400 Bad Request (ex: e-mail duplicado vindo do Core)
                    if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        // Retorna a mensagem de erro que o Core enviou no body do 400
                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null));
                        // Nota: Idealmente, o body conteria uma mensagem de erro, mas para simplificar, retorna null.

                    }

                    // Mapeia outros erros do Core ou problemas de comunicação
                    else {
                        return Mono.just(ResponseEntity.status(e.getStatusCode()).body(null));
                    }
                })
                // Se houver exceções não-WebClient (ex: validação interna), use onErrorResume(Exception.class, ...)
                .onErrorResume(e -> {
                    System.err.println("Erro inesperado no BFF ao registrar: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
                });
    }
}
