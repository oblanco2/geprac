package com.udi.geprac.academico.service;

import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Lo que el token de la petición en curso dice de quien la hace. El alcance de
 * cada operación sale de aquí y nunca de un dato que el usuario envíe en la
 * petición (RNF-06).
 *
 * @author Oscar Iván Blanco Díaz
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

    /** El correo institucional con el que ingresó. */
    public static String correo() {
        return token().getClaimAsString("email");
    }

    /** El nombre que registró en Supabase Auth o, si no hay, el que se deduce del correo. */
    public static String nombre() {
        Map<String, Object> meta = token().getClaimAsMap("user_metadata");
        if (meta != null) {
            for (String clave : new String[] { "full_name", "name", "nombre" }) {
                Object v = meta.get(clave);
                if (v instanceof String s && !s.isBlank()) return s.trim();
            }
        }
        String correo = correo();
        String local = correo == null ? "Usuario" : correo.substring(0, correo.indexOf('@') < 0 ? correo.length() : correo.indexOf('@'));
        StringBuilder sb = new StringBuilder();
        for (String parte : local.split("[._-]+")) {
            parte = parte.replaceAll("\\d", "");
            if (parte.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(parte.charAt(0))).append(parte.substring(1));
        }
        return sb.isEmpty() ? local : sb.toString();
    }
}
