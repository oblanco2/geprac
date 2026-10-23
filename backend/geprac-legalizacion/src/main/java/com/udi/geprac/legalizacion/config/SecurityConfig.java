package com.udi.geprac.legalizacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de MS-02 Legalización de Prácticas.
 *
 * Los tokens los emite Supabase Auth, el mismo proveedor de MS-01; aquí solo
 * se validan contra su JWKS (ver {@link JwtConfig}). El rol del usuario viaja
 * en el token, en el claim «rol», y se convierte en la autoridad
 * ROLE_DIRECTOR, ROLE_TUTOR o ROLE_ESTUDIANTE, sin consultar a MS-01. CORS lo
 * resuelve {@link CorsConfig} antes de esta cadena.
 *
 * @author Darien Asdrwal Pesca Ojeda
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
                // CU-02 y CU-03: el director mantiene los catálogos, y el estudiante consulta el de
                // instituciones para escoger la suya (CU-05); CU-04: el director prepara el semestre;
                // CU-05: el estudiante inscribe su práctica; CU-06: el tutor revisa
                .requestMatchers("/practicas/**").hasRole("DIRECTOR")
                .requestMatchers("/semestres/**").hasRole("DIRECTOR")
                .requestMatchers(HttpMethod.GET, "/instituciones").hasAnyRole("DIRECTOR", "ESTUDIANTE")
                .requestMatchers("/instituciones/**").hasRole("DIRECTOR")
                .requestMatchers("/inscripciones/**").hasRole("ESTUDIANTE")
                .requestMatchers("/revisiones/**").hasRole("TUTOR")
                // CU-07: el director avala; CU-08: el estudiante emite sus formatos;
                // CU-09: los tres roles consultan el historial, cada uno con su alcance
                .requestMatchers("/avales/**").hasRole("DIRECTOR")
                .requestMatchers("/formatos/**").hasRole("ESTUDIANTE")
                .requestMatchers("/historial/**").hasAnyRole("ESTUDIANTE", "TUTOR", "DIRECTOR")
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
