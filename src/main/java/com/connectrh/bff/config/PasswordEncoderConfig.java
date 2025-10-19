package com.connectrh.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração para definir o bean PasswordEncoder no contexto do BFF.
 * Isso resolve o erro de inicialização que ocorre quando o Spring Security
 * está ativado, mas o codificador de senha não está explicitamente definido.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
