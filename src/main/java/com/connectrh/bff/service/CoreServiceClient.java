package com.connectrh.bff.service;

import com.connectrh.bff.dto.request.LoginRequest;
import com.connectrh.bff.dto.response.CoreAuthResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Cliente de serviço para comunicação com o Connect RH Core Service.
 * Responsável por rotear chamadas do BFF para o Core, incluindo a chave de API interna.
 */
@Service
public class CoreServiceClient {

    private final WebClient coreWebClient;

    /**
     * Injeta o WebClient configurado no CoreClientConfig, usando @Qualifier para
     * garantir que o bean 'coreWebClient' seja selecionado.
     */
    public CoreServiceClient(@Qualifier("coreWebClient") WebClient coreWebClient) {
        this.coreWebClient = coreWebClient;
    }

    /**
     * Chama o endpoint interno de login do Core Service para validar credenciais.
     * Este é o método que o AuthController deve chamar.
     *
     * @param request DTO com email e senha.
     * @return Mono contendo CoreAuthResponse se o login for bem-sucedido.
     */
    public Mono<CoreAuthResponse> internalLogin(LoginRequest request) {
        return coreWebClient.post()
                // Endpoint interno no Core Service
                .uri("/api/v1/internal/auth/login")
                .bodyValue(request)
                .retrieve()
                // Trata a resposta 401 do Core Service
                .onStatus(status -> status.equals(HttpStatus.UNAUTHORIZED),
                        response -> Mono.error(new WebClientResponseException(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Credenciais inválidas no Core Service",
                                response.headers().asHttpHeaders(),
                                null,
                                null)))
                .bodyToMono(CoreAuthResponse.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    // Propaga o erro 401 e trata outros erros
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        return Mono.error(e);
                    }
                    System.err.println("Erro ao chamar Core Service: " + e.getMessage());
                    return Mono.error(new RuntimeException("Erro interno de comunicação com Core Service."));
                });
    }
}
