package com.udi.geprac.legalizacion.service;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Lo que el token de la petición en curso dice de quien la hace. El rol y los
 * datos de la cuenta los añade al token el hook de MS-01, de modo que MS-02
 * autoriza sin consultar a MS-01 y el alcance nunca sale de un dato que el
 * usuario envíe en la petición (RNF-06).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public final class UsuarioActual {

    private UsuarioActual() { }

    static Jwt token() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a instanceof JwtAuthenticationToken t) return t.getToken();
        throw new IllegalStateException("La petición no trae un token de acceso.");
    }

    /** El identificador de la cuenta, el mismo de Supabase Auth. */
    public static UUID id() {
        return UUID.fromString(token().getSubject());
    }
}
