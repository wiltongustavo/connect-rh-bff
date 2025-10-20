package com.connectrh.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuração do WebClient, o cliente HTTP moderno do Spring,
 * responsável pela comunicação segura do BFF com o Core Service.
 * * Este arquivo resolve a BeanCreationException, pois a classe 'coreClientConfig'
 * estava faltando ou estava incompleta.
 */
@Configuration
public class CoreClientConfig {

    // 1. Injeção das propriedades do application.properties
    // Deve ser apenas HOST e PORTA: Exemplo: http://localhost:8080
    @Value("${connectrh.core.url}")
    private String coreServiceBaseUrl; // Renomeado para maior clareza

    @Value("${connectrh.security.internal-api-key}")
    private String internalApiKey;

    /**
     * Define o WebClient pré-configurado para chamar o Core Service.
     * O WebClient já terá a URL base (host:port) e o Header de segurança.
     * @return Uma instância de WebClient.
     */
    @Bean
    public WebClient coreWebClient() {
        return WebClient.builder()
                // Define a URL base (host:port)
                .baseUrl(coreServiceBaseUrl)

                // Adiciona o Header de segurança em TODAS as requisições para o Core
                // Isso resolve o 403 Forbidden que você teve anteriormente.
                .defaultHeader("X-Internal-Api-Key", internalApiKey)

                .build();
    }
}
