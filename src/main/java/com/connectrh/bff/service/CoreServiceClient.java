package com.connectrh.bff.service;

import com.connectrh.bff.dto.request.CreateUserRequest;
import com.connectrh.bff.dto.request.LoginRequest;
import com.connectrh.bff.dto.response.CoreAuthResponse;
import com.connectrh.bff.dto.response.UserCreateResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Cliente de serviço para comunicação com o Connect RH Core Service.
 * Responsável por rotear chamadas do BFF para o Core, incluindo a chave de API interna.
 * (O nome da classe foi alterado para CoreServiceClient para seguir a nomenclatura de serviço HTTP).
 */
@Service
public class CoreServiceClient { // Nome da classe ajustado

    // O WebClient injetado já tem a baseUrl (http://localhost:8080) e o Header de API Key
    private final WebClient coreWebClient;

    /**
     * Injeta o WebClient configurado no CoreClientConfig (bean 'coreWebClient').
     *
     * @param coreWebClient O WebClient configurado para o Core Service.
     */
    public CoreServiceClient(@Qualifier("coreWebClient") WebClient coreWebClient) {
        this.coreWebClient = coreWebClient;
    }

    /**
     * Chama o endpoint interno de login do Core Service para validar credenciais.
     * Este é o método que o AuthController deve chamar no BFF.
     *
     * @param request DTO com email e senha.
     * @return Mono contendo CoreAuthResponse se o login for bem-sucedido.
     */
    public Mono<CoreAuthResponse> internalLogin(LoginRequest request) {
        return coreWebClient.post()
                // Endpoint completo, já que a baseUrl do WebClient é apenas http://localhost:8080
                .uri("/api/v1/internal/auth/login")
                .bodyValue(request) // Método moderno do WebClient
                .retrieve()

                // Mapeamento de erros
                // 1. Tratamento específico para 404 (Not Found) - Mapeamento errado no Core
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        response -> Mono.error(new RuntimeException("Core Service URL Not Found (404). Verifique o @RequestMapping no Core: /api/v1/internal/auth/login")))

                // 2. Tratamento do erro 401 (Unauthorized)
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        response -> Mono.error(new WebClientResponseException(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Credenciais inválidas no Core Service",
                                response.headers().asHttpHeaders(),
                                null,
                                null)))

                // 3. Conversão e tratamento de outros erros HTTP (Ex: 500)
                .bodyToMono(CoreAuthResponse.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    // Propaga o erro 401 ou 404
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(e);
                    }
                    // Trata outros erros de comunicação/servidor (5xx)
                    System.err.println("Erro ao chamar Core Service: " + e.getMessage());
                    return Mono.error(new RuntimeException("Erro interno de comunicação com Core Service. Status: " + e.getStatusCode()));
                });
    }

    public Mono<UserCreateResponse> internalCreateUser(CreateUserRequest request) {
        return coreWebClient.post()
                // Endpoint do Core para cadastro
                .uri("/api/v1/internal/auth/signup")
                .bodyValue(request)
                .retrieve()

                // 1. Tratamento específico para 404 (Endpoint não encontrado no Core)
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        response -> Mono.error(new RuntimeException("Core Service URL Not Found (404). Endpoint /signup não encontrado.")))

                // 2. Tratamento do erro 400 (Bad Request) - E-mail duplicado, por exemplo
                // O Core lança 400 com a mensagem de erro (do IllegalArgumentException)
                .onStatus(HttpStatus.BAD_REQUEST::equals,
                        response -> response.bodyToMono(String.class) // Tenta pegar a mensagem de erro do body
                                .flatMap(errorMessage -> Mono.error(new WebClientResponseException(
                                        HttpStatus.BAD_REQUEST.value(),
                                        errorMessage, // Usa a mensagem de erro do Core (ex: "Email já cadastrado")
                                        response.headers().asHttpHeaders(),
                                        null,
                                        null))))

                // 3. Conversão para o DTO de Resposta (esperamos 201 Created)
                .bodyToMono(UserCreateResponse.class)

                // 4. Tratamento de outros erros de comunicação (5xx)
                .onErrorResume(WebClientResponseException.class, e -> {
                    // Propaga erros de Bad Request ou Not Found, ou lança erro genérico para outros 5xx
                    if (e.getStatusCode() == HttpStatus.BAD_REQUEST || e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(e); // Propaga o erro do Core
                    }
                    System.err.println("Erro ao chamar Core Service (Signup): " + e.getMessage());
                    return Mono.error(new RuntimeException("Erro interno de comunicação com Core Service. Status: " + e.getStatusCode()));
                });
    }
}
