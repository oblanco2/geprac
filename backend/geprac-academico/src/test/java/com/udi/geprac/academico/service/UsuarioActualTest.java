package com.udi.geprac.academico.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El nombre con que se registra la cuenta en su primer ingreso.
 *
 * @author Oscar Iván Blanco Díaz
 */
class UsuarioActualTest {

    @AfterEach
    void cerrarSesion() {
        SecurityContextHolder.clearContext();
    }

    private static void ingresa(Jwt.Builder token) {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token.build()));
    }

    @Test
    void sinNombreRegistradoLoDeduceDelCorreo() {
        ingresa(Jwt.withTokenValue("t").header("alg", "ES256").subject("x").claim("email", "laura.gomez04@udi.edu.co"));
        assertThat(UsuarioActual.nombre()).isEqualTo("Laura Gomez");
    }

    @Test
    void prefiereElNombreQueRegistroEnElProveedor() {
        ingresa(Jwt.withTokenValue("t").header("alg", "ES256").subject("x").claim("email", "laura.gomez@udi.edu.co")
            .claim("user_metadata", java.util.Map.of("full_name", "Laura Valentina Gómez")));
        assertThat(UsuarioActual.nombre()).isEqualTo("Laura Valentina Gómez");
    }
}
