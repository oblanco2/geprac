package com.udi.geprac.legalizacion.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El rol que el proveedor de identidad pone en el token se convierte en la
 * autoridad que usan las reglas de acceso.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class SecurityConfigTest {

    private static Jwt token(String rol) {
        Jwt.Builder b = Jwt.withTokenValue("t").header("alg", "ES256").claim("sub", "usuario-1");
        if (rol != null) {
            b.claim("rol", rol);
        }
        return b.build();
    }

    @Test
    void elRolDelTokenSeVuelveAutoridad() {
        var autenticacion = SecurityConfig.convertidorDeRoles().convert(token("DIRECTOR"));
        assertThat(autenticacion.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .contains("ROLE_DIRECTOR");
    }

    @Test
    void sinRolNoHayAutoridadDeRol() {
        var autenticacion = SecurityConfig.convertidorDeRoles().convert(token(null));
        assertThat(autenticacion.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .noneMatch(a -> a.startsWith("ROLE_"));
    }
}
