package com.udi.geprac.academico.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS registrado como filtro de máxima prioridad.
 *
 * En Spring Security 7 el .cors() de la cadena no siempre alcanza la
 * petición previa que envía el navegador: llega antes de que ese filtro
 * actúe y la respuesta sale sin las cabeceras necesarias. Registrarlo
 * con orden HIGHEST_PRECEDENCE garantiza que se atienda primero.
 */
@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> filtroCors() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "https://geprac-web.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> registro =
            new FilterRegistrationBean<>(new CorsFilter(fuente));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }
}