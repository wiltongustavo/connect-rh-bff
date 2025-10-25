package com.connectrh.bff.dto.response;

import lombok.Data;

import java.util.Set;

/**
 * Data Transfer Object (DTO) que o BFF espera receber do Core Service
 * após a validação bem-sucedida das credenciais de login.
 * <p>
 * NOTA: O campo ID é chamado 'id' para coincidir com a entidade User do Core Service.
 */
@Data
public class CoreAuthResponse {
    private Long userId; // ID do usuário (usado no AuthController como .getId() para gerar o JWT)
    private String name;
    private String email;
    private Set<String> roles; // Roles (ADMIN, MANAGER, EMPLOYEE)
}
