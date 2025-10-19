package com.connectrh.bff.client;

import com.connectrh.bff.dto.request.LoginRequest;
import com.connectrh.bff.dto.response.CoreAuthResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Interface para comunicação HTTP com o Core Service.
 * Usa Spring HTTP Interface Client (WebClient por baixo dos panos) para comunicação.
 * Nota: Os DTOs de request/response devem ser os mesmos usados no Core Service.
 */
@HttpExchange(url = "/internal/auth")
public interface CoreClient {

    /**
     * Tenta autenticar um usuário no Core Service.
     * O CoreClientConfig adiciona a API Key secreta a esta requisição.
     *
     * @param request DTO de Login (email e senha).
     * @return CoreAuthResponse com dados do usuário autenticado.
     */
    @PostExchange("/login")
    CoreAuthResponse authenticate(@RequestBody LoginRequest request);
}
