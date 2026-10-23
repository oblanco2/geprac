package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.Aval;
import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.Revision;
import com.udi.geprac.legalizacion.dto.InscripcionDto;
import com.udi.geprac.legalizacion.repository.AvalRepository;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-07 · Avalar la inscripción. El director opera solo sobre las
 * inscripciones de su programa, que sale del token, y solo avala las que el
 * tutor ya aprobó. El aval y el cambio de estado se guardan juntos o no se
 * guarda ninguno (excepción 8.1): nunca queda una inscripción avalada sin
 * constancia de quién la avaló.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Service
@Transactional
public class AvalService {

    private final InscripcionRepository inscripciones;
    private final AvalRepository avales;

    public AvalService(InscripcionRepository inscripciones, AvalRepository avales) {
        this.inscripciones = inscripciones;
        this.avales = avales;
    }

    /**
     * Paso 2 y variación 2.1: las inscripciones aprobadas del programa, de la que
     * lleva más tiempo esperando a la más reciente, y después las ya avaladas del
     * semestre abierto, para consultarlas.
     */
    @Transactional(readOnly = true)
    public List<InscripcionDto> listarAprobadas() {
        String programa = UsuarioActual.programa();
        Stream<Inscripcion> pendientes = inscripciones
            .findByEstadoAndPracticaCodigoPrograma(EstadoInscripcion.APROBADA, programa).stream()
            .sorted(Comparator.comparing(AvalService::aprobadaEn));
        Stream<Inscripcion> avaladas = inscripciones
            .findByEstadoAndPracticaCodigoPrograma(EstadoInscripcion.AVALADA, programa).stream()
            .filter(i -> Boolean.TRUE.equals(i.getSemestre().getAbierto()))
            .sorted(Comparator.comparing((Inscripcion i) -> i.getAval().getFecha()).reversed());
        return Stream.concat(pendientes, avaladas).map(InscripcionDto::de).toList();
    }

    /** Pasos 5 a 8: comprueba que la inscripción esté aprobada, registra el aval con su autor y su fecha, y la deja avalada. */
    public InscripcionDto registrarAval(Long id) {
        String programa = UsuarioActual.programa();
        Inscripcion i = inscripciones.findById(id)
            .filter(x -> programa.equals(x.getPractica().getCodigoPrograma()))
            .orElseThrow(() -> new NoSuchElementException("La inscripción no pertenece a su programa."));
        if (i.getEstado() != EstadoInscripcion.APROBADA)
            throw new IllegalStateException("Solo se avala una inscripción aprobada por el tutor académico: esta está "
                + i.getEstado().name().toLowerCase(Locale.ROOT) + ".");
        Aval aval = avales.save(new Aval(i, UsuarioActual.id()));
        i.avalar(aval);
        Inscripcion guardada = inscripciones.save(i);
        inscripciones.flush();
        return InscripcionDto.de(guardada);
    }

    /** La fecha de la aprobación del tutor: la de su última revisión. */
    private static LocalDateTime aprobadaEn(Inscripcion i) {
        return i.getRevisiones().stream().map(Revision::getFecha).max(Comparator.naturalOrder())
            .orElse(i.getEnviadaEn() == null ? i.getCreadaEn() : i.getEnviadaEn());
    }
}
