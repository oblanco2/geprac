package com.udi.geprac.academico.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de MS-01 Identidad y Perfil Académico.
 *
 * Los tokens los emite Supabase Auth; aquí solo se validan contra su JWKS
 * (ver {@link JwtConfig}). El rol del usuario viaja en el token, en el claim
 * «rol», y se convierte en la autoridad ROLE_DIRECTOR, ROLE_TUTOR o
 * ROLE_ESTUDIANTE. CORS lo resuelve {@link CorsConfig} antes de esta cadena.
 *
 * @author Oscar Iván Blanco Díaz
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain cadenaFiltros(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                jwt.jwtAuthenticationConverter(convertidorDeRoles())));

        return http.build();
    }

    /**
     * Convierte el claim «rol» del token en la autoridad ROLE_ que usan las
     * reglas de acceso. Un token sin rol queda autenticado, sin autoridad de rol.
     */
    public static JwtAuthenticationConverter convertidorDeRoles() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }
}
