package com.connectrh.bff.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO que representa a resposta final do BFF para o Frontend Angular após o login.
 * Inclui o token JWT e dados básicos do usuário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token; // O JWT gerado pelo BFF
    private Long userId;
    private String email;
    private String name;
    private Set<String> roles;
}
