package com.udi.geprac.legalizacion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Decodificador de los tokens de Supabase Auth, el mismo proveedor de MS-01.
 *
 * Supabase firma con claves de curva elíptica (ES256). El decodificador
 * descarga las llaves públicas del proveedor desde su JWKS y solo acepta
 * tokens firmados con ese algoritmo, sin consultar al otro microservicio.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Configuration
public class JwtConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
            .jwsAlgorithm(SignatureAlgorithm.ES256)
            .build();
    }
}
