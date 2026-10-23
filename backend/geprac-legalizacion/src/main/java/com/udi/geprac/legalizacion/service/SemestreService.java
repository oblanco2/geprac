package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.domain.Semestre;
import com.udi.geprac.legalizacion.domain.TutorPractica;
import com.udi.geprac.legalizacion.dto.DesignacionDto;
import com.udi.geprac.legalizacion.dto.PreparacionDto;
import com.udi.geprac.legalizacion.dto.SemestreDto;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import com.udi.geprac.legalizacion.repository.PracticaRepository;
import com.udi.geprac.legalizacion.repository.SemestreRepository;
import com.udi.geprac.legalizacion.repository.TutorPracticaRepository;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-04 · Preparar el semestre, en lo que guarda MS-02: abrir y cerrar el
 * semestre y designar el tutor académico de cada práctica del programa. Solo
 * puede haber un semestre abierto a la vez, y un tutor por práctica y por
 * semestre; esa designación decide qué inscripciones llegan a la bandeja de
 * cada tutor (CU-06). El rol del usuario designado lo consulta a MS-01 (paso
 * 10). La asignación de los roles (variación 2.1) la guarda MS-01.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Service
@Transactional
public class SemestreService {

    private static final String SIN_ABIERTO = "No hay semestre abierto: abra uno antes de designar los tutores.";

    private final SemestreRepository semestres;
    private final PracticaRepository practicas;
    private final TutorPracticaRepository designaciones;
    private final InscripcionRepository inscripciones;
    private final IdentidadCliente identidad;

    public SemestreService(SemestreRepository semestres, PracticaRepository practicas, TutorPracticaRepository designaciones,
                           InscripcionRepository inscripciones, IdentidadCliente identidad) {
        this.semestres = semestres;
        this.practicas = practicas;
        this.designaciones = designaciones;
        this.inscripciones = inscripciones;
        this.identidad = identidad;
    }

    /**
     * Paso 2: el semestre abierto o, si no hay, el último, con las prácticas
     * del programa y el tutor que cada una tiene designado en él.
     */
    @Transactional(readOnly = true)
    public PreparacionDto consultarPreparacion() {
        List<Practica> delPrograma = practicas.findByCodigoProgramaOrderByOrden(UsuarioActual.programa());
        Semestre s = semestres.findByAbiertoTrue().or(semestres::findFirstByOrderByFechaInicioDesc).orElse(null);
        if (s == null)
            return new PreparacionDto(null, delPrograma.stream().map(p -> DesignacionDto.de(p, null, 0)).toList());
        Map<Long, UUID> tutores = designaciones.findBySemestre(s).stream()
            .collect(Collectors.toMap(d -> d.getPractica().getId(), TutorPractica::getTutorId));
        Map<Long, Long> enviadas = enviadas(s, delPrograma);
        return new PreparacionDto(SemestreDto.de(s), delPrograma.stream()
            .map(p -> DesignacionDto.de(p, tutores.get(p.getId()), enviadas.getOrDefault(p.getId(), 0L))).toList());
    }

    /** Pasos 5 a 8: comprueba las fechas, que no haya otro abierto y que el código sea nuevo, y lo abre. */
    public SemestreDto abrirSemestre(SemestreDto datos) {
        if (!datos.fechaCierre().isAfter(datos.fechaInicio()))   // excepción 6.1
            throw new CampoInvalidoException("fechaCierre", "La fecha de cierre debe ser posterior a la de inicio.");
        semestres.findByAbiertoTrue().ifPresent(vigente -> {   // excepción 7.1
            throw new IllegalStateException("No se abrió el semestre: el " + vigente.getCodigo() + " sigue abierto. "
                + "Solo puede haber uno abierto a la vez: ciérrelo primero con «Cerrar el semestre vigente».");
        });
        String codigo = datos.codigo().trim();
        if (semestres.existsByCodigo(codigo))   // excepción 7.2
            throw new CampoInvalidoException("codigo", "Ese código ya existe: cada semestre tiene un código único.",
                HttpStatus.CONFLICT);
        Semestre s = semestres.save(new Semestre(codigo, datos.fechaInicio(), datos.fechaCierre()));
        semestres.flush();
        return SemestreDto.de(s);
    }

    /** Variación 2.2: cierra el semestre vigente; mientras no se abra otro, ningún estudiante puede inscribir. */
    public SemestreDto cerrarSemestre(Long id) {
        Semestre s = semestres.findById(id).orElseThrow(() -> new NoSuchElementException("El semestre no existe."));
        if (!s.getAbierto()) throw new IllegalStateException("El semestre " + s.getCodigo() + " ya está cerrado.");
        s.cerrar();
        return SemestreDto.de(semestres.save(s));
    }

    /**
     * Pasos 9 a 11: designa el tutor de una práctica que todavía no lo tiene
     * en el semestre abierto. Primero le pregunta a MS-01 si el usuario tiene
     * el rol de tutor académico (paso 10).
     */
    public DesignacionDto designarTutor(Long practicaId, UUID tutorId) {
        esTutor(tutorId);
        Semestre s = semestres.findByAbiertoTrue().orElseThrow(() -> new IllegalStateException(SIN_ABIERTO));
        Practica p = delPrograma(practicaId);
        if (designaciones.existsBySemestreAndPracticaId(s, p.getId()))   // excepción 10.2
            throw new IllegalStateException("No se guardó la designación: la práctica " + p.getOrden()
                + " ya tiene tutor designado en el semestre " + s.getCodigo() + ". Puede reasignarla.");
        designaciones.save(new TutorPractica(s, p, tutorId));
        return DesignacionDto.de(p, tutorId, enviadas(s, List.of(p)).getOrDefault(p.getId(), 0L));
    }

    /**
     * Variación 9.1: reemplaza el tutor de una práctica en el semestre abierto
     * (o la designa, si quedó sin tutor). Las inscripciones ya enviadas pasan a
     * la bandeja del nuevo tutor, porque la bandeja sale de la designación.
     */
    public DesignacionDto reasignarTutor(Long practicaId, UUID tutorId) {
        esTutor(tutorId);
        Semestre s = semestres.findByAbiertoTrue().orElseThrow(() -> new IllegalStateException(SIN_ABIERTO));
        Practica p = delPrograma(practicaId);
        TutorPractica d = designaciones.findBySemestreAndPracticaId(s, p.getId())
            .orElseGet(() -> new TutorPractica(s, p, tutorId));
        d.reasignar(tutorId);
        designaciones.save(d);
        return DesignacionDto.de(p, tutorId, enviadas(s, List.of(p)).getOrDefault(p.getId(), 0L));
    }

    // ------------------------------------------------------------------ apoyo

    /** Paso 10 y excepción 10.1: el usuario designado tiene que tener el rol de tutor académico en MS-01. */
    private void esTutor(UUID tutorId) {
        if (!"TUTOR".equals(identidad.consultarRol(tutorId)))
            throw new CampoInvalidoException("tutorId", "El usuario escogido no tiene el rol de tutor académico: "
                + "primero debe asignárselo en la lista de roles.", HttpStatus.CONFLICT);
    }

    /** La práctica, solo si es del programa del director. */
    private Practica delPrograma(Long id) {
        String programa = UsuarioActual.programa();
        return practicas.findById(id).filter(p -> p.getCodigoPrograma().equals(programa))
            .orElseThrow(() -> new NoSuchElementException("La práctica no está en el catálogo de su programa."));
    }

    /** Cuántas inscripciones enviadas, que esperan revisión, tiene cada práctica en el semestre. */
    private Map<Long, Long> enviadas(Semestre s, List<Practica> de) {
        if (de.isEmpty()) return Map.of();
        return inscripciones.findBySemestreAndPracticaInAndEstado(s, de, EstadoInscripcion.ENVIADA).stream()
            .collect(Collectors.groupingBy(i -> i.getPractica().getId(), Collectors.counting()));
    }
}
