package com.udi.geprac.legalizacion;

import com.udi.geprac.legalizacion.domain.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Datos de prueba sin base de datos: arma las entidades como las dejaría
 * guardadas la base —con su identificador y sus copias— y abre la sesión de un
 * usuario con los datos que el hook de MS-01 pone en el token.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public final class Fabrica {

    private Fabrica() { }

    /** Una entidad con su constructor protegido, el que usa JPA. */
    public static <T> T nuevo(Class<T> clase) {
        try {
            Constructor<T> c = clase.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Pone un valor en un campo privado, como lo haría JPA al leer la fila. */
    public static <T> T poner(T objeto, String campo, Object valor) {
        try {
            Field f = objeto.getClass().getDeclaredField(campo);
            f.setAccessible(true);
            f.set(objeto, valor);
            return objeto;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Semestre semestre(long id, String codigo, boolean abierto) {
        Semestre s = nuevo(Semestre.class);
        poner(s, "id", id);
        poner(s, "codigo", codigo);
        poner(s, "fechaInicio", LocalDate.of(2026, 7, 27));
        poner(s, "fechaCierre", LocalDate.of(2026, 12, 4));
        return poner(s, "abierto", abierto);
    }

    public static Practica practica(long id, int orden) {
        Practica p = nuevo(Practica.class);
        poner(p, "id", id);
        poner(p, "codigoPrograma", "LEI");
        poner(p, "orden", orden);
        poner(p, "nombre", "Práctica pedagógica " + orden);
        poner(p, "objetivoGeneral", "Observar y describir el ambiente pedagógico.");
        poner(p, "activa", true);
        p.getObjetivos().add(new ObjetivoPractica(p, 1, "Reconocer el contexto institucional."));
        p.getActividades().add(new ActividadPractica(p, 1, "Observar la jornada escolar."));
        return p;
    }

    public static TutorPractica designacion(UUID tutor, Semestre s, Practica p) {
        TutorPractica d = nuevo(TutorPractica.class);
        poner(d, "semestre", s);
        poner(d, "practica", p);
        return poner(d, "tutorId", tutor);
    }

    /** Una inscripción en el estado pedido, con la copia completa de su expediente. */
    public static Inscripcion inscripcion(long id, long estudianteId, Practica p, Semestre s, EstadoInscripcion estado) {
        Inscripcion i = nuevo(Inscripcion.class);
        poner(i, "id", id);
        poner(i, "estudianteId", estudianteId);
        poner(i, "practica", p);
        poner(i, "semestre", s);
        poner(i, "estado", estado);
        poner(i, "fechaInicio", LocalDate.of(2026, 10, 5));
        poner(i, "fechaFin", LocalDate.of(2026, 11, 13));
        poner(i, "creadaEn", LocalDateTime.of(2026, 9, 15, 10, 0));
        if (estado != EstadoInscripcion.BORRADOR) poner(i, "enviadaEn", LocalDateTime.of(2026, 9, 19, 16, 30));

        InscripcionDatos d = nuevo(InscripcionDatos.class);
        poner(d, "inscripcion", i);
        poner(d, "nombres", "Valeria");
        poner(d, "apellidos", "Cruz Rangel");
        poner(d, "tipoDocumento", "CC");
        poner(d, "numeroDocumento", "1099331208");
        poner(d, "lugarExpedicion", "Bucaramanga");
        poner(d, "fechaNacimiento", LocalDate.of(2004, 3, 12));
        poner(d, "eps", "Nueva EPS");
        poner(d, "direccion", "Calle 45 # 27-18");
        poner(d, "ciudad", "Bucaramanga");
        poner(d, "celular", "3001234567");
        poner(d, "correoPersonal", "valeria.cruz@correo.com");
        poner(d, "nombrePrograma", "Licenciatura en Educación Infantil");
        poner(d, "semestreCursado", p.getOrden());
        poner(d, "perfilProfesional", "Estudiante de licenciatura con interés en la primera infancia.");
        poner(d, "herramientasTrabajo", "Ofimática y recursos didácticos digitales.");
        poner(d, "institucionRazonSocial", "Institución Educativa Las Américas");
        poner(d, "institucionNit", "890201234");
        poner(d, "institucionDireccion", "Carrera 33 # 52-10");
        poner(d, "institucionCiudad", "Bucaramanga");
        poner(d, "institucionTelefono", "6076341200");
        poner(d, "institucionRepresentante", "Rosa Elena Díaz");
        poner(d, "contactoNombre", "María Patricia Flores");
        poner(d, "contactoCargo", "Coordinadora académica");
        poner(d, "contactoTelefono", "6076341201");
        poner(d, "contactoCelular", "3157654321");
        poner(d, "contactoCorreo", "mflores@lasamericas.edu.co");
        poner(d, "nombrePractica", p.getNombre());
        poner(d, "objetivoGeneral", p.getObjetivoGeneral());
        poner(d, "horarioEstandar", "Lunes a viernes, 7:00 a 12:00");
        poner(i, "datos", d);

        i.getObjetivos().add(new InscripcionObjetivo(i, 1, "Reconocer el contexto institucional."));
        i.getActividades().add(new InscripcionActividad(i, 1, "Observar la jornada escolar."));
        i.getFormaciones().add(new InscripcionFormacion(i, "Secundaria", "Colegio Santander", "Bachiller académico", 2021));
        i.getExperiencias().add(new InscripcionExperiencia(i, "Jardín Mis Pequeños", "Auxiliar pedagógica", "Ana Ruiz",
            "Coordinadora", "6076000000", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30),
            "Apoyo en actividades lúdicas.", "Organizó la feria de lectura."));
        i.getReferencias().add(new InscripcionReferencia(i, "Lic. Carlos Gómez", "Colegio Santander", "Docente",
            "3009876543", "Bucaramanga"));
        return i;
    }

    /** Abre la sesión del usuario con los datos del token. */
    public static void ingresa(UUID id, String rol, Object... claims) {
        Map<String, Object> extra = new HashMap<>();
        for (int k = 0; k + 1 < claims.length; k += 2) extra.put((String) claims[k], claims[k + 1]);
        Jwt.Builder token = Jwt.withTokenValue("t").header("alg", "ES256").subject(id.toString());
        if (rol != null) token.claim("rol", rol);
        extra.forEach(token::claim);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token.build()));
    }

    public static void sale() {
        SecurityContextHolder.clearContext();
    }
}
