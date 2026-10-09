package com.udi.geprac.legalizacion.service;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Lo que el token de la petición en curso dice de quien la hace: su cuenta, su
 * rol, el programa que dirige o en el que estudia y el identificador de su
 * registro de estudiante. Los añade al token el hook de MS-01, de modo que
 * MS-02 autoriza sin consultar a MS-01 y el alcance nunca sale de un dato que
 * el usuario envíe en la petición (RNF-06).
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

    /** El rol de la cuenta: DIRECTOR, TUTOR o ESTUDIANTE; nulo mientras la Dirección no lo asigne. */
    public static String rol() {
        return token().getClaimAsString("rol");
    }

    /** El programa del director o del estudiante. */
    public static String programa() {
        String p = token().getClaimAsString("programa");
        if (p == null || p.isBlank())
            throw new IllegalStateException("Su cuenta todavía no tiene programa: la Dirección del Programa debe asignárselo.");
        return p;
    }

    /** El identificador del registro del estudiante en MS-01. */
    public static Long estudianteId() {
        Object v = token().getClaims().get("estudiante_id");
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s && s.matches("\\d+")) return Long.parseLong(s);
        throw new IllegalStateException("Todavía no ha registrado su perfil: su plan de prácticas depende del "
            + "programa y del semestre que cursa. Regístrelos en «Mi perfil».");
    }
}
