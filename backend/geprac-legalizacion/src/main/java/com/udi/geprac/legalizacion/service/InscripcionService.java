package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.ContactoInstitucion;
import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.InscripcionDatos;
import com.udi.geprac.legalizacion.domain.InscripcionExperiencia;
import com.udi.geprac.legalizacion.domain.InscripcionFormacion;
import com.udi.geprac.legalizacion.domain.InscripcionReferencia;
import com.udi.geprac.legalizacion.domain.Institucion;
import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.domain.Semestre;
import com.udi.geprac.legalizacion.dto.ContactoDto;
import com.udi.geprac.legalizacion.dto.ExpedienteDto;
import com.udi.geprac.legalizacion.dto.InscripcionCambiosDto;
import com.udi.geprac.legalizacion.dto.InscripcionDto;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import com.udi.geprac.legalizacion.repository.InstitucionRepository;
import com.udi.geprac.legalizacion.repository.PracticaRepository;
import com.udi.geprac.legalizacion.repository.SemestreRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-05 · Inscribir la práctica del periodo. El software no le presenta al
 * estudiante una lista para elegir: le corresponde la práctica activa del
 * semestre que cursa, en el semestre abierto. Al crear la inscripción copia en
 * ella, de una vez, sus datos personales y su hoja de vida —que le pide a
 * MS-01 con el token del estudiante— y el contenido de la práctica; un cambio
 * posterior del perfil o del catálogo no altera lo ya inscrito.
 *
 * El estudiante solo opera sus propias inscripciones: su registro sale de
 * MS-01 o del token, nunca de un dato de la petición.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Service
@Transactional
public class InscripcionService {

    private static final String SIN_SEMESTRE = "La Dirección del Programa todavía no ha abierto el semestre: "
        + "la inscripción se habilita cuando lo abra.";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final IdentidadCliente identidad;
    private final SemestreRepository semestres;
    private final PracticaRepository practicas;
    private final InscripcionRepository inscripciones;
    private final InstitucionRepository instituciones;

    public InscripcionService(IdentidadCliente identidad, SemestreRepository semestres, PracticaRepository practicas,
                              InscripcionRepository inscripciones, InstitucionRepository instituciones) {
        this.identidad = identidad;
        this.semestres = semestres;
        this.practicas = practicas;
        this.inscripciones = inscripciones;
        this.instituciones = instituciones;
    }

    /**
     * Pasos 1 a 4, variaciones 4.1 y 4.1.1.1, y excepciones 2.1, 2.2 y 3.1: la
     * inscripción de la práctica que le corresponde en el semestre abierto. Si
     * ya la había iniciado, la devuelve como está, sin volver a copiar nada, y
     * el cliente la retoma en el paso en que quedó o presenta su estado. Si
     * no, la crea en borrador con su copia del expediente y de la práctica.
     */
    public InscripcionDto iniciarInscripcion() {
        ExpedienteDto expediente = identidad.consultarExpediente();
        ExpedienteDto.Perfil perfil = expediente.perfil();
        Semestre semestre = semestres.findByAbiertoTrue().orElseThrow(() -> new IllegalStateException(SIN_SEMESTRE));
        // sin registro en MS-01 no hay programa ni semestre que cursa: falta todo el perfil
        if (perfil.id() == null || perfil.codigoPrograma() == null || perfil.semestreActual() == null)
            throw incompleto(perfil);
        Practica practica = practicas.findByCodigoProgramaAndOrden(perfil.codigoPrograma(), perfil.semestreActual())
            .orElseThrow(() -> sinPractica(perfil.semestreActual()));
        Optional<Inscripcion> iniciada = inscripciones.findByEstudianteId(perfil.id()).stream()
            .filter(i -> i.getSemestre().getId().equals(semestre.getId())
                && i.getPractica().getId().equals(practica.getId()))
            .findFirst();
        if (iniciada.isPresent()) return InscripcionDto.de(iniciada.get());
        if (!practica.getActiva()) throw sinPractica(perfil.semestreActual());
        if (perfil.completitud() == null || !perfil.completitud().completo() || expediente.hojaVida() == null)
            throw incompleto(perfil);

        Inscripcion nueva = new Inscripcion(perfil.id(), practica, semestre);
        copiarRegistro(nueva, perfil, expediente.hojaVida());
        try {
            return InscripcionDto.de(inscripciones.saveAndFlush(nueva));
        } catch (DataIntegrityViolationException e) {
            // la restricción de una inscripción por práctica y semestre: otra sesión la creó primero
            throw new IllegalStateException("La inscripción ya se había iniciado desde otra sesión: "
                + "vuelva a «Inscribir práctica» para retomarla.");
        }
    }

    /**
     * Pasos 6 a 11, variaciones 6.1 y 11.1 y excepción 9.1: guarda lo que el
     * estudiante diligenció en cualquiera de los tres pasos sin cambiar el
     * estado, de modo que la nueva sigue en borrador y la devuelta sigue
     * devuelta. Lo que no viene en la petición queda como estaba.
     */
    public InscripcionDto actualizarInscripcion(Long id, InscripcionCambiosDto cambios) {
        Inscripcion i = editable(id);
        if (cambios.institucionId() != null) i.elegirInstitucion(institucion(i, cambios.institucionId()));
        if (cambios.contactoId() != null || cambios.fechaInicio() != null || cambios.fechaFin() != null)
            elegirEscenario(i, cambios);
        // paso 11: los datos del registro que el estudiante ajusta solo para esta inscripción
        InscripcionDatos d = i.getDatos();
        if (cambios.direccion() != null) d.setDireccion(cambios.direccion().trim());
        if (cambios.celular() != null) d.setCelular(cambios.celular().trim());
        if (cambios.correoPersonal() != null) d.setCorreoPersonal(cambios.correoPersonal().trim());
        return InscripcionDto.de(inscripciones.saveAndFlush(i));
    }

    /**
     * Pasos 12 a 15, excepciones 14.1 y 14.2 y variación 16.2: comprueba que
     * tenga la institución, el contacto y las dos fechas, y la pasa a la
     * bandeja del tutor designado con la fecha del envío; la devuelta, con la
     * del reenvío.
     */
    public InscripcionDto enviarARevision(Long id) {
        Inscripcion i = editable(id);
        List<String> faltan = new ArrayList<>();
        if (i.getInstitucion() == null) faltan.add("la institución receptora");
        if (i.getContacto() == null) faltan.add("el contacto del escenario");
        if (i.getFechaInicio() == null || i.getFechaFin() == null) faltan.add("las fechas de la práctica");
        if (!faltan.isEmpty()) throw new RegistroIncompletoException("La inscripción no se envió: está incompleta.", faltan);
        i.enviar();
        return InscripcionDto.de(inscripciones.saveAndFlush(i));
    }

    /**
     * Variación 16.1: la inscripción del estudiante en el semestre abierto,
     * con su estado y, si fue devuelta, el motivo que registró el tutor.
     */
    @Transactional(readOnly = true)
    public InscripcionDto consultarEstado() {
        Long estudiante = UsuarioActual.estudianteId();
        Semestre semestre = semestres.findByAbiertoTrue().orElseThrow(() -> new IllegalStateException(SIN_SEMESTRE));
        return inscripciones.findByEstudianteId(estudiante).stream()
            .filter(i -> i.getSemestre().getId().equals(semestre.getId()))
            .max(Comparator.comparing(Inscripcion::getCreadaEn))
            .map(InscripcionDto::de)
            .orElseThrow(() -> new NoSuchElementException("No tiene inscripción en el semestre " + semestre.getCodigo()
                + ": cuando inscriba su práctica, su estado aparecerá aquí."));
    }

    // ------------------------------------------------------------------ apoyo

    /** La inscripción del estudiante del token; la de otro estudiante no existe para él. */
    private Inscripcion propia(Long id) {
        Long estudiante = UsuarioActual.estudianteId();
        return inscripciones.findById(id)
            .filter(i -> i.getEstudianteId().equals(estudiante))
            .orElseThrow(() -> new NoSuchElementException("La inscripción no existe o no es suya."));
    }

    /** Excepción 14.2: solo el borrador y la devuelta admiten cambios, y solo mientras su semestre siga abierto. */
    private Inscripcion editable(Long id) {
        Inscripcion i = propia(id);
        if (!i.editable())
            throw new IllegalStateException("La inscripción ya fue enviada al tutor académico, quizá desde otra sesión: "
                + "no admite cambios ni se envía de nuevo. Está " + i.getEstado().name().toLowerCase(Locale.ROOT) + ".");
        if (!i.getSemestre().getAbierto())
            throw new IllegalStateException("El semestre " + i.getSemestre().getCodigo()
                + " ya está cerrado: la inscripción no admite cambios.");
        return i;
    }

    /** Paso 6: una institución del catálogo activo; la que ya tenía escogida se conserva aunque la hayan retirado. */
    private Institucion institucion(Inscripcion i, Long institucionId) {
        boolean misma = i.getInstitucion() != null && i.getInstitucion().getId().equals(institucionId);
        return instituciones.findById(institucionId)
            .filter(x -> misma || x.getActiva())
            .orElseThrow(() -> new CampoInvalidoException("institucionId",
                "Esa institución no está en el catálogo de instituciones activas: seleccione otra."));
    }

    /**
     * Pasos 8 a 10 y excepción 9.1: el contacto y las dos fechas van juntos,
     * porque el formato de solicitud y aprobación los exige. Si falta uno, o
     * una fecha no corresponde, se señalan todos y no se guarda el paso.
     */
    private static void elegirEscenario(Inscripcion i, InscripcionCambiosDto c) {
        Map<String, String> errores = new LinkedHashMap<>();
        ContactoInstitucion contacto = null;
        if (i.getInstitucion() == null)
            errores.put("institucionId", "Primero seleccione la institución receptora: el contacto se escoge entre "
                + "las personas registradas en ella.");
        else if (c.contactoId() == null)
            errores.put("contactoId", "Seleccione la persona de la institución que lo acompañará.");
        else {
            contacto = i.getInstitucion().getContactos().stream()
                .filter(x -> x.getId().equals(c.contactoId())).findFirst().orElse(null);
            if (contacto == null)
                errores.put("contactoId", "Esa persona no es contacto de la institución seleccionada: selecciónela de nuevo.");
        }
        Semestre s = i.getSemestre();
        String dentro = "La fecha debe quedar dentro del semestre " + s.getCodigo() + ": del "
            + FECHA.format(s.getFechaInicio()) + " al " + FECHA.format(s.getFechaCierre()) + ".";
        if (c.fechaInicio() == null) errores.put("fechaInicio", ContactoDto.OBLIGATORIO);
        else if (fuera(c.fechaInicio(), s)) errores.put("fechaInicio", dentro);
        if (c.fechaFin() == null) errores.put("fechaFin", ContactoDto.OBLIGATORIO);
        else if (fuera(c.fechaFin(), s)) errores.put("fechaFin", dentro);
        else if (c.fechaInicio() != null && !c.fechaFin().isAfter(c.fechaInicio()))
            errores.put("fechaFin", "La fecha de finalización debe ser posterior a la de inicio.");
        if (!errores.isEmpty())
            throw new CampoInvalidoException(errores, "Revise los datos señalados: el software no guardó el paso.");
        i.elegirEscenario(contacto, c.fechaInicio(), c.fechaFin());
    }

    private static boolean fuera(LocalDate fecha, Semestre s) {
        return fecha.isBefore(s.getFechaInicio()) || fecha.isAfter(s.getFechaCierre());
    }

    /** Paso 4: la copia del registro del estudiante, tal como está en MS-01 en este momento. */
    private static void copiarRegistro(Inscripcion i, ExpedienteDto.Perfil p, ExpedienteDto.HojaVida h) {
        InscripcionDatos d = i.getDatos();
        d.setNombres(p.nombres());
        d.setApellidos(p.apellidos());
        d.setTipoDocumento(p.tipoDocumento());
        d.setNumeroDocumento(p.numeroDocumento());
        d.setLugarExpedicion(p.lugarExpedicion());
        d.setFechaNacimiento(p.fechaNacimiento());
        d.setLugarNacimiento(p.lugarNacimiento());
        d.setGenero(p.genero());
        d.setEstadoCivil(p.estadoCivil());
        d.setEps(p.eps());
        d.setDireccion(p.direccion());
        d.setBarrio(p.barrio());
        d.setCiudad(p.ciudad());
        d.setTelefonoFijo(p.telefonoFijo());
        d.setCelular(p.celular());
        d.setCorreoPersonal(p.correoPersonal());
        d.setNombrePrograma(p.nombrePrograma());
        d.setSemestreCursado(p.semestreActual());
        d.setPerfilProfesional(h.perfilProfesional());
        d.setHerramientasTrabajo(h.herramientasTrabajo());
        lista(h.formaciones()).forEach(f -> i.getFormaciones().add(
            new InscripcionFormacion(i, f.tipo(), f.institucion(), f.nombre(), f.anio())));
        lista(h.experiencias()).forEach(x -> i.getExperiencias().add(new InscripcionExperiencia(i, x.empresa(),
            x.cargo(), x.jefeInmediato(), x.cargoJefe(), x.telefonoEmpresa(), x.fechaInicio(), x.fechaFin(),
            x.funciones(), x.logros())));
        lista(h.referencias()).forEach(r -> i.getReferencias().add(
            new InscripcionReferencia(i, r.nombre(), r.empresa(), r.cargo(), r.telefono(), r.ciudad())));
    }

    private static <T> List<T> lista(List<T> l) {
        return l == null ? List.of() : l;
    }

    /** Excepción 3.1: lo que le falta al registro, con los nombres del indicador de MS-01. */
    private static RegistroIncompletoException incompleto(ExpedienteDto.Perfil p) {
        List<String> faltan = p.completitud() == null || p.completitud().faltan() == null
            ? List.of() : p.completitud().faltan();
        return new RegistroIncompletoException("Complete su perfil y su hoja de vida para inscribir la práctica: la "
            + "inscripción copia esos datos, y los formatos los exigen. No se creó la inscripción.", faltan);
    }

    /** Excepción 2.2: la práctica del semestre que cursa no está cargada o está retirada del catálogo. */
    private static IllegalStateException sinPractica(Integer orden) {
        return new IllegalStateException("La Dirección del Programa todavía no ha cargado la práctica " + orden
            + " de su plan, o la tiene retirada del catálogo: no se creó la inscripción.");
    }
}
