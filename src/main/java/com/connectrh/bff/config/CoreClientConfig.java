package com.connectrh.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CoreClientConfig {

    // 1. Injeção da URL Base do Core Service com um valor default seguro (fallback)
    // O valor default garante que 'coreServiceUrl' nunca seja nulo ou vazio, prevenindo o erro "Host is not specified".
    @Value("${connectrh.core-service.url:http://localhost:8080/api/v1}")
    private String coreServiceUrl;

    // 2. Injeção da Chave de Segurança Interna
    @Value("${connectrh.security.internal-api-key}")
    private String internalApiKey;

    // 3. Injeção do Nome do Cabeçalho
    @Value("${core.security.api-key.header}")
    private String apiKeyHeader;

    /**
     * Cria um WebClient Bean (coreWebClient) configurado com:
     * - A URL Base COMPLETA (BaseUrl) do Core Service.
     * - O cabeçalho de autenticação interna (X-INTERNAL-API-KEY).
     *
     * @param webClientBuilder Builder padrão injetado pelo Spring.
     * @return WebClient configurado para o Core Service.
     */
    @Bean
    public WebClient coreWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                // Define a URL Base para todas as chamadas deste cliente
                .baseUrl(this.coreServiceUrl)

                // Adiciona o cabeçalho de segurança interna para todas as requisições
                .defaultHeader(this.apiKeyHeader, this.internalApiKey)
                .build();
    }
}
