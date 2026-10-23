package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.dto.HistorialDto;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import com.udi.geprac.legalizacion.repository.PracticaRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-09 · Consultar el historial de prácticas. Es un solo caso de uso con tres
 * alcances, y el alcance lo determina el rol del token, no un dato que el
 * usuario indique: el estudiante ve sus inscripciones sobre las ocho prácticas
 * de su plan, el tutor las de las prácticas que tuvo designadas y la Dirección
 * todas las de su programa. No modifica ningún dato.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Service
@Transactional(readOnly = true)
public class HistorialService {

    /** Del semestre más reciente al más antiguo, en el orden del plan y por estudiante. */
    private static final Comparator<HistorialDto> ORDEN = Comparator
        .comparing(HistorialDto::semestre, Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(HistorialDto::ordenPractica)
        .thenComparing(HistorialDto::estudiante, Comparator.nullsFirst(Comparator.naturalOrder()));

    private final InscripcionRepository inscripciones;
    private final PracticaRepository practicas;

    public HistorialService(InscripcionRepository inscripciones, PracticaRepository practicas) {
        this.inscripciones = inscripciones;
        this.practicas = practicas;
    }

    /** Paso 2: el alcance por el rol del token. */
    public List<HistorialDto> consultarHistorial() {
        String rol = UsuarioActual.rol();
        if ("ESTUDIANTE".equals(rol)) return delEstudiante();
        if ("TUTOR".equals(rol)) return delTutor();
        if ("DIRECTOR".equals(rol)) return delPrograma();
        throw new IllegalStateException("Su cuenta espera la asignación del rol por parte de la Dirección del Programa.");
    }

    /**
     * Pasos 3 a 5: las inscripciones del estudiante en todos los semestres,
     * completadas con las prácticas de su plan que no ha inscrito. Cada práctica
     * sale en el orden del plan, con sus inscripciones de la más reciente a la
     * más antigua.
     */
    private List<HistorialDto> delEstudiante() {
        Long estudiante = UsuarioActual.estudianteId();
        String programa = UsuarioActual.programa();
        List<HistorialDto> suyas = inscripciones.findByEstudianteId(estudiante).stream().map(HistorialDto::de).toList();
        Set<Integer> inscritas = suyas.stream().map(HistorialDto::ordenPractica).collect(Collectors.toSet());
        List<HistorialDto> r = new ArrayList<>(suyas);
        for (Practica p : practicas.findByCodigoProgramaOrderByOrden(programa))
            if (!inscritas.contains(p.getOrden())) r.add(HistorialDto.noInscrita(p));
        r.sort(Comparator.comparing(HistorialDto::ordenPractica)
            .thenComparing(HistorialDto::semestre, Comparator.nullsLast(Comparator.reverseOrder())));
        return r;
    }

    /** Variación 2.1: las inscripciones enviadas de las prácticas que el tutor tuvo designadas. */
    private List<HistorialDto> delTutor() {
        return inscripciones.findDesignadasAlTutor(UsuarioActual.id()).stream()
            .filter(i -> i.getEstado() != EstadoInscripcion.BORRADOR)
            .map(HistorialDto::de).sorted(ORDEN).toList();
    }

    /** Variación 2.2: todas las inscripciones del programa, en todos los semestres. */
    private List<HistorialDto> delPrograma() {
        return inscripciones.findByPracticaCodigoPrograma(UsuarioActual.programa()).stream()
            .map(HistorialDto::de).sorted(ORDEN).toList();
    }
}
