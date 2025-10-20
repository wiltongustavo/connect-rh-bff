package com.connectrh.bff.util;

import com.connectrh.bff.dto.response.CoreAuthResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utilitário responsável pela geração, validação e manipulação de JSON Web Tokens (JWT) no BFF.
 * Ele usa a chave secreta injetada via @Value para assinar os tokens.
 */
@Component
public class JwtUtil {

    // Chave secreta para assinatura do JWT, injetada do application.properties
    @Value("${connectrh.jwt.secret}") // CHAVE CORRIGIDA
    private String secret;

    // Tempo de expiração do token (Ex: 1 hora)
    @Value("${connectrh.jwt.expiration}") // CHAVE CORRIGIDA
    private long jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gera um JWT para um usuário autenticado.
     *
     * @param userResponse Dados do usuário vindos do Core Service.
     * @return O JWT assinado.
     */
    public String generateToken(CoreAuthResponse userResponse) {
        Map<String, Object> claims = new HashMap<>();

        // 1. Mapear Roles para Claims
        List<String> rolesList = userResponse.getRoles().stream().collect(Collectors.toList());
        claims.put("roles", rolesList);

        // 2. Adicionar o ID do usuário (subject) e o nome
        String subject = userResponse.getUserId().toString();
        claims.put("name", userResponse.getName());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
