package com.udi.geprac.academico.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.net.URL;

/**
 * Decodificador construido a mano para aceptar ES256.
 *
 * Supabase firma con claves de curva elíptica. La propiedad
 * jws-algorithms no siempre se aplica, así que se declara el
 * selector de claves de forma explícita.
 */
@Configuration
public class JwtConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        JWKSource<SecurityContext> fuente = new RemoteJWKSet<>(new URL(jwkSetUri));

        DefaultJWTProcessor<SecurityContext> procesador = new DefaultJWTProcessor<>();
        procesador.setJWSKeySelector(
            new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, fuente));

        return new NimbusJwtDecoder(procesador);
    }
}