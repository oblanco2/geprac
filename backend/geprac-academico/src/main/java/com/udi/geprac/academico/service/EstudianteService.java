package com.udi.geprac.academico.service;

import com.udi.geprac.academico.domain.Estudiante;
import com.udi.geprac.academico.domain.ExperienciaLaboral;
import com.udi.geprac.academico.domain.FormacionAcademica;
import com.udi.geprac.academico.domain.Programa;
import com.udi.geprac.academico.domain.ReferenciaPersonal;
import com.udi.geprac.academico.domain.Usuario;
import com.udi.geprac.academico.dto.BloquesDto;
import com.udi.geprac.academico.dto.CompletitudDto;
import com.udi.geprac.academico.dto.EntradaDto;
import com.udi.geprac.academico.dto.EstudianteDto;
import com.udi.geprac.academico.dto.HojaVidaDto;
import com.udi.geprac.academico.repository.EstudianteRepository;
import com.udi.geprac.academico.repository.ProgramaRepository;
import com.udi.geprac.academico.repository.UsuarioRepository;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-01 · Mantener perfil del estudiante. El estudiante crea y mantiene el
 * registro único que persiste entre semestres: sus datos personales y los
 * cinco bloques de su hoja de vida. Todo se opera sobre el registro de la
 * cuenta que trae el token, nunca sobre uno que el estudiante nombre.
 *
 * El indicador de completitud se calcula aquí, sobre los datos que exigen los
 * cuatro formatos, para que la pantalla y la inscripción (CU-05) midan lo
 * mismo.
 *
 * @author Oscar Iván Blanco Díaz
 */
@Service
@Transactional
public class EstudianteService {

    /** Los bloques de la hoja de vida que tienen entradas, nombrados como el último tramo de su ruta. */
    public enum Bloque {
        FORMACIONES("formaciones", "La entrada de formación académica"),
        EXPERIENCIAS("experiencias", "La experiencia laboral"),
        REFERENCIAS("referencias", "La referencia personal");

        private final String ruta;
        private final String entrada;

        Bloque(String ruta, String entrada) {
            this.ruta = ruta;
            this.entrada = entrada;
        }

        /** El bloque de una ruta; uno que no existe responde 404. */
        public static Bloque de(String ruta) {
            for (Bloque b : values()) if (b.ruta.equals(ruta)) return b;
            throw new NoSuchElementException("La hoja de vida no tiene el bloque «" + ruta + "».");
        }
    }

    /** El máximo de referencias que recomienda el formato PR-01 (variación 14.2). */
    static final int MAX_REFERENCIAS = 3;

    /** El orden de los tres tipos de formación del formato: la tabla se ordena por él. */
    private static final List<String> TIPOS_FORMACION = List.of("Secundaria", "Universitaria", "Otros");

    private final EstudianteRepository estudiantes;
    private final UsuarioRepository usuarios;
    private final ProgramaRepository programas;

    public EstudianteService(EstudianteRepository estudiantes, UsuarioRepository usuarios, ProgramaRepository programas) {
        this.estudiantes = estudiantes;
        this.usuarios = usuarios;
        this.programas = programas;
    }

    // ------------------------------------------------------------- datos personales (P-03)

    /**
     * Paso 2 y variación 2.1: el registro con su indicador o, si todavía no
     * existe, el formulario vacío con el correo institucional de la cuenta.
     */
    @Transactional(readOnly = true)
    public EstudianteDto consultarPerfil() {
        UUID cuenta = UsuarioActual.id();
        return estudiantes.findByUsuarioId(cuenta)
            .map(e -> EstudianteDto.de(e, completitud(e)))
            .orElseGet(() -> EstudianteDto.vacio(cuenta(cuenta), completitud(null)));
    }

    /**
     * Pasos 6 a 9 y variación 6.1: crea el registro o lo actualiza. Comprueba
     * que el programa exista (excepción 7.3) y que el documento no sea de otro
     * estudiante (excepción 7.2); los datos mínimos y los formatos ya llegaron
     * validados (excepción 7.1).
     */
    public EstudianteDto guardarPerfil(EstudianteDto datos) {
        UUID cuenta = UsuarioActual.id();
        Programa programa = programas.findByCodigoIgnoreCase(datos.codigoPrograma().trim())
            .orElseThrow(() -> new CampoInvalidoException("codigoPrograma",
                "El programa que seleccionó ya no existe. Selecciónelo de nuevo."));
        String tipo = datos.tipoDocumento().trim();
        String numero = conPuntos(datos.numeroDocumento());

        Estudiante e = estudiantes.findByUsuarioId(cuenta).orElse(null);
        boolean cambiaDocumento = e == null || !tipo.equals(e.getTipoDocumento()) || !numero.equals(e.getNumeroDocumento());
        if (cambiaDocumento && estudiantes.existsByTipoDocumentoAndNumeroDocumento(tipo, numero))
            throw new CampoInvalidoException("numeroDocumento", "Ese número de documento ya corresponde a otro estudiante. "
                + "Si considera que es suyo, comuníquese con la Dirección del Programa.", HttpStatus.CONFLICT);

        if (e == null) e = new Estudiante(cuenta(cuenta));
        else e.tocar();
        e.setNombres(datos.nombres().trim());
        e.setApellidos(datos.apellidos().trim());
        e.setTipoDocumento(tipo);
        e.setNumeroDocumento(numero);
        e.setLugarExpedicion(texto(datos.lugarExpedicion()));
        e.setFechaNacimiento(datos.fechaNacimiento());
        e.setLugarNacimiento(texto(datos.lugarNacimiento()));
        e.setGenero(texto(datos.genero()));
        e.setEstadoCivil(texto(datos.estadoCivil()));
        e.setEps(texto(datos.eps()));
        e.setDireccion(texto(datos.direccion()));
        e.setBarrio(texto(datos.barrio()));
        e.setCiudad(texto(datos.ciudad()));
        e.setTelefonoFijo(texto(datos.telefonoFijo()));
        e.setCelular(texto(datos.celular()));
        e.setCorreoPersonal(texto(datos.correoPersonal()));
        e.setPrograma(programa);
        e.setSemestreActual(datos.semestreActual());
        Estudiante guardado = estudiantes.save(e);
        return EstudianteDto.de(guardado, completitud(guardado));
    }

    // ------------------------------------------------------------- hoja de vida (P-04)

    /** Pasos 10 y 11 y variación 11.1: los cinco bloques, vacíos o con lo registrado. */
    @Transactional(readOnly = true)
    public HojaVidaDto consultarHojaVida() {
        return hojaVida(registro());
    }

    /** Pasos 12 y 13: el perfil profesional y las herramientas de trabajo. */
    public HojaVidaDto guardarBloques(BloquesDto bloques) {
        Estudiante e = registro();
        e.setPerfilProfesional(texto(bloques.perfilProfesional()));
        e.setHerramientasTrabajo(texto(bloques.herramientasTrabajo()));
        e.tocar();
        return hojaVida(estudiantes.save(e));
    }

    /** Pasos 14 a 16: una entrada de formación académica. */
    public EntradaDto.Formacion crearFormacion(EntradaDto.Formacion entrada) {
        Estudiante e = registro();
        comprobarAnio(entrada.anio());
        FormacionAcademica f = new FormacionAcademica(e);
        copiar(entrada, f);
        e.getFormaciones().add(f);
        guardar(e);
        return EntradaDto.Formacion.de(f);
    }

    /** Variación 14.1: una experiencia laboral. */
    public EntradaDto.Experiencia crearExperiencia(EntradaDto.Experiencia entrada) {
        Estudiante e = registro();
        comprobarFechas(entrada);
        ExperienciaLaboral x = new ExperienciaLaboral(e);
        copiar(entrada, x);
        e.getExperiencias().add(x);
        guardar(e);
        return EntradaDto.Experiencia.de(x);
    }

    /** Variación 14.2: una referencia personal, hasta el máximo de tres (excepción 14.2.3.1). */
    public EntradaDto.Referencia crearReferencia(EntradaDto.Referencia entrada) {
        Estudiante e = registro();
        if (e.getReferencias().size() >= MAX_REFERENCIAS)
            throw new IllegalStateException("La hoja de vida ya tiene tres referencias, que es el máximo que admite el "
                + "formato PR-01: elimine una para registrar otra.");
        ReferenciaPersonal r = new ReferenciaPersonal(e);
        copiar(entrada, r);
        e.getReferencias().add(r);
        guardar(e);
        return EntradaDto.Referencia.de(r);
    }

    /**
     * Variación 16.1: corrige una entrada con las comprobaciones de su bloque.
     * Si la entrada ya no existe, responde 404 (excepción 16.2.4.1).
     */
    public EntradaDto actualizarEntrada(Bloque bloque, Long id, EntradaDto entrada) {
        Estudiante e = registro();
        switch (entrada) {
            case EntradaDto.Formacion f when bloque == Bloque.FORMACIONES -> {
                comprobarAnio(f.anio());
                copiar(f, buscar(e.getFormaciones(), FormacionAcademica::getId, id, bloque));
            }
            case EntradaDto.Experiencia x when bloque == Bloque.EXPERIENCIAS -> {
                comprobarFechas(x);
                copiar(x, buscar(e.getExperiencias(), ExperienciaLaboral::getId, id, bloque));
            }
            case EntradaDto.Referencia r when bloque == Bloque.REFERENCIAS ->
                copiar(r, buscar(e.getReferencias(), ReferenciaPersonal::getId, id, bloque));
            default -> throw new IllegalArgumentException("Los datos no corresponden al bloque «" + bloque.ruta + "».");
        }
        guardar(e);
        return switch (bloque) {
            case FORMACIONES -> EntradaDto.Formacion.de(buscar(e.getFormaciones(), FormacionAcademica::getId, id, bloque));
            case EXPERIENCIAS -> EntradaDto.Experiencia.de(buscar(e.getExperiencias(), ExperienciaLaboral::getId, id, bloque));
            case REFERENCIAS -> EntradaDto.Referencia.de(buscar(e.getReferencias(), ReferenciaPersonal::getId, id, bloque));
        };
    }

    /**
     * Variación 16.2: retira una entrada, ya confirmada en el cliente web. Si
     * ya no existe —se eliminó desde otra sesión— responde 404 (excepción
     * 16.2.4.1).
     */
    public void eliminarEntrada(Bloque bloque, Long id) {
        Estudiante e = registro();
        boolean quitada = switch (bloque) {
            case FORMACIONES -> e.getFormaciones().removeIf(f -> id.equals(f.getId()));
            case EXPERIENCIAS -> e.getExperiencias().removeIf(x -> id.equals(x.getId()));
            case REFERENCIAS -> e.getReferencias().removeIf(r -> id.equals(r.getId()));
        };
        if (!quitada) throw new NoSuchElementException(bloque.entrada + " ya no existe: se eliminó desde otra sesión.");
        guardar(e);
    }

    // ------------------------------------------------------------- indicador de completitud

    /**
     * El indicador del paso 9: los dieciocho datos personales y los cuatro
     * bloques obligatorios de la hoja de vida que exigen los formatos. Sin
     * registro, falta todo.
     */
    static CompletitudDto completitud(Estudiante e) {
        Map<String, Object> exigidos = new LinkedHashMap<>();
        exigidos.put("Nombres", e == null ? null : e.getNombres());
        exigidos.put("Apellidos", e == null ? null : e.getApellidos());
        exigidos.put("Tipo de documento", e == null ? null : e.getTipoDocumento());
        exigidos.put("Número de documento", e == null ? null : e.getNumeroDocumento());
        exigidos.put("Lugar de expedición", e == null ? null : e.getLugarExpedicion());
        exigidos.put("Fecha de nacimiento", e == null ? null : e.getFechaNacimiento());
        exigidos.put("Lugar de nacimiento", e == null ? null : e.getLugarNacimiento());
        exigidos.put("Género", e == null ? null : e.getGenero());
        exigidos.put("Estado civil", e == null ? null : e.getEstadoCivil());
        exigidos.put("EPS vigente", e == null ? null : e.getEps());
        exigidos.put("Dirección", e == null ? null : e.getDireccion());
        exigidos.put("Barrio", e == null ? null : e.getBarrio());
        exigidos.put("Ciudad", e == null ? null : e.getCiudad());
        exigidos.put("Teléfono fijo", e == null ? null : e.getTelefonoFijo());
        exigidos.put("Celular", e == null ? null : e.getCelular());
        exigidos.put("Correo personal", e == null ? null : e.getCorreoPersonal());
        exigidos.put("Programa", e == null ? null : e.getPrograma());
        exigidos.put("Semestre que cursa", e == null ? null : e.getSemestreActual());
        exigidos.put("Perfil profesional", e == null ? null : e.getPerfilProfesional());
        exigidos.put("Herramientas de trabajo", e == null ? null : e.getHerramientasTrabajo());
        exigidos.put("Formación académica", e == null || e.getFormaciones().isEmpty() ? null : e.getFormaciones());
        exigidos.put("Referencias personales", e == null || e.getReferencias().isEmpty() ? null : e.getReferencias());

        List<String> faltan = new ArrayList<>();
        exigidos.forEach((nombre, valor) -> {
            if (valor == null || (valor instanceof String s && s.isBlank())) faltan.add(nombre);
        });
        int porcentaje = (int) Math.round((exigidos.size() - faltan.size()) * 100.0 / exigidos.size());
        return new CompletitudDto(porcentaje, faltan.isEmpty(), List.copyOf(faltan));
    }

    /** El número de documento con los puntos de miles, como lo imprimen los formatos. */
    static String conPuntos(String numero) {
        String digitos = numero.replaceAll("\\D", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digitos.length(); i++) {
            if (i > 0 && (digitos.length() - i) % 3 == 0) sb.append('.');
            sb.append(digitos.charAt(i));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ apoyo

    /** El registro de la cuenta; la hoja de vida se guarda con él, así que debe existir. */
    private Estudiante registro() {
        return estudiantes.findByUsuarioId(UsuarioActual.id())
            .orElseThrow(() -> new IllegalStateException("Registre primero sus datos personales en «Mi perfil»: "
                + "la hoja de vida se guarda con ellos."));
    }

    private Usuario cuenta(UUID id) {
        return usuarios.findById(id)
            .orElseThrow(() -> new IllegalStateException("La cuenta no está registrada: cierre la sesión e ingrese de nuevo."));
    }

    /**
     * Guarda las entradas del registro y marca la fecha de la modificación. El
     * registro ya está administrado en la transacción, así que basta con
     * sincronizarlo: las entradas nuevas se insertan en cascada y reciben su
     * identificador en el mismo objeto que se devuelve al cliente.
     */
    private void guardar(Estudiante e) {
        e.tocar();
        estudiantes.flush();
    }

    private static HojaVidaDto hojaVida(Estudiante e) {
        List<EntradaDto.Formacion> formaciones = e.getFormaciones().stream()
            .sorted(Comparator.comparingInt((FormacionAcademica f) -> TIPOS_FORMACION.indexOf(f.getTipo()))
                .thenComparing(f -> f.getAnio() == null ? 9999 : f.getAnio(), Comparator.reverseOrder()))
            .map(EntradaDto.Formacion::de).toList();
        List<EntradaDto.Experiencia> experiencias = e.getExperiencias().stream()
            .sorted(Comparator.comparing(ExperienciaLaboral::getFechaInicio).reversed())
            .map(EntradaDto.Experiencia::de).toList();
        List<EntradaDto.Referencia> referencias = e.getReferencias().stream()
            .sorted(Comparator.comparing(ReferenciaPersonal::getId, Comparator.nullsLast(Comparator.naturalOrder())))
            .map(EntradaDto.Referencia::de).toList();
        return new HojaVidaDto(e.getPerfilProfesional(), e.getHerramientasTrabajo(), formaciones, experiencias,
            referencias, completitud(e));
    }

    private static <T> T buscar(List<T> lista, Function<T, Long> id, Long buscado, Bloque bloque) {
        return lista.stream().filter(x -> buscado.equals(id.apply(x))).findFirst()
            .orElseThrow(() -> new NoSuchElementException(bloque.entrada + " ya no existe: se eliminó desde otra sesión."));
    }

    /** Excepción 15.1: el año no puede ser posterior al año en curso. */
    private static void comprobarAnio(Integer anio) {
        if (anio == null) return;
        if (anio > Year.now().getValue())
            throw new CampoInvalidoException("anio", "El año no puede ser posterior al año en curso.");
        if (anio < 1950)
            throw new CampoInvalidoException("anio", "Escriba un año desde 1950.");
    }

    /** Variación 14.1: la fecha de retiro, si la hay, no es anterior a la de ingreso. */
    private static void comprobarFechas(EntradaDto.Experiencia x) {
        if (x.fechaFin() != null && x.fechaFin().isBefore(x.fechaInicio()))
            throw new CampoInvalidoException("fechaFin", "La fecha de retiro no puede ser anterior a la de ingreso.");
    }

    private static void copiar(EntradaDto.Formacion d, FormacionAcademica f) {
        f.setTipo(d.tipo().trim());
        f.setInstitucion(d.institucion().trim());
        f.setNombre(d.nombre().trim());
        f.setAnio(d.anio());
    }

    private static void copiar(EntradaDto.Experiencia d, ExperienciaLaboral x) {
        x.setEmpresa(d.empresa().trim());
        x.setCargo(d.cargo().trim());
        x.setJefeInmediato(texto(d.jefeInmediato()));
        x.setCargoJefe(texto(d.cargoJefe()));
        x.setTelefonoEmpresa(texto(d.telefonoEmpresa()));
        x.setFechaInicio(d.fechaInicio());
        x.setFechaFin(d.fechaFin());
        x.setFunciones(texto(d.funciones()));
        x.setLogros(texto(d.logros()));
    }

    private static void copiar(EntradaDto.Referencia d, ReferenciaPersonal r) {
        r.setNombre(d.nombre().trim());
        r.setEmpresa(d.empresa().trim());
        r.setCargo(d.cargo().trim());
        r.setTelefono(d.telefono().trim());
        r.setCiudad(d.ciudad().trim());
    }

    private static String texto(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
