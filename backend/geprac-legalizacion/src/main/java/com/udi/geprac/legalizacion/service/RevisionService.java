package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.domain.ResultadoRevision;
import com.udi.geprac.legalizacion.domain.Revision;
import com.udi.geprac.legalizacion.domain.TutorPractica;
import com.udi.geprac.legalizacion.dto.InscripcionDto;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import com.udi.geprac.legalizacion.repository.RevisionRepository;
import com.udi.geprac.legalizacion.repository.TutorPracticaRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-06 · Revisar la inscripción. El alcance del tutor lo fijan sus
 * designaciones en el semestre abierto: solo ve y resuelve las inscripciones
 * enviadas de las prácticas que tiene designadas, y ningún dato de la petición
 * lo amplía. La revisión y el cambio de estado se guardan juntos o no se guarda
 * ninguno (excepción 8.1).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Service
@Transactional
public class RevisionService {

    private final TutorPracticaRepository designaciones;
    private final InscripcionRepository inscripciones;
    private final RevisionRepository revisiones;

    public RevisionService(TutorPracticaRepository designaciones, InscripcionRepository inscripciones,
                           RevisionRepository revisiones) {
        this.designaciones = designaciones;
        this.inscripciones = inscripciones;
        this.revisiones = revisiones;
    }

    /** Paso 2: las inscripciones enviadas de sus prácticas, de la que lleva más tiempo esperando a la más reciente. */
    @Transactional(readOnly = true)
    public List<InscripcionDto> listarPendientes() {
        List<TutorPractica> suyas = designadas();
        List<Practica> practicas = suyas.stream().map(TutorPractica::getPractica).toList();
        return inscripciones.findBySemestreAndPracticaInAndEstado(suyas.get(0).getSemestre(), practicas,
                EstadoInscripcion.ENVIADA).stream()
            .sorted(Comparator.comparing(Inscripcion::getEnviadaEn))
            .map(InscripcionDto::de)
            .toList();
    }

    /** Pasos 5 a 8 y variación 5.1: registra la revisión con su autor y su fecha, y cambia el estado. */
    public InscripcionDto registrarRevision(Long id, String resultado, String motivo) {
        ResultadoRevision r = resultado(resultado);
        // el motivo solo se registra al devolver; el que acompañe una aprobación no se guarda
        String m = r == ResultadoRevision.APROBADA || motivo == null || motivo.isBlank() ? null : motivo.trim();
        if (r == ResultadoRevision.DEVUELTA && m == null)
            throw new IllegalArgumentException("Escriba el motivo de la devolución: le indica al estudiante qué corregir.");
        List<TutorPractica> suyas = designadas();
        Inscripcion i = inscripciones.findById(id)
            .filter(x -> suyas.stream().anyMatch(d -> d.getPractica().getId().equals(x.getPractica().getId())
                && d.getSemestre().getId().equals(x.getSemestre().getId())))
            .orElseThrow(() -> new NoSuchElementException("La inscripción no está en su bandeja de revisión."));
        if (i.getEstado() != EstadoInscripcion.ENVIADA)
            throw new IllegalStateException("La inscripción ya fue resuelta: está "
                + i.getEstado().name().toLowerCase(Locale.ROOT) + ".");
        Revision revision = revisiones.save(new Revision(i, UsuarioActual.id(), r, m));
        i.resolver(revision);
        Inscripcion guardada = inscripciones.save(i);
        inscripciones.flush();
        return InscripcionDto.de(guardada);
    }

    // ------------------------------------------------------------------ apoyo

    /** Las designaciones del tutor del token en el semestre abierto; sin ellas no hay bandeja (excepción 2.2). */
    private List<TutorPractica> designadas() {
        List<TutorPractica> suyas = designaciones.findByTutorIdAndSemestreAbiertoTrue(UsuarioActual.id());
        if (suyas.isEmpty())
            throw new IllegalStateException("La Dirección del Programa todavía no le ha designado prácticas en el semestre abierto.");
        return suyas;
    }

    private static ResultadoRevision resultado(String valor) {
        try {
            return ResultadoRevision.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El resultado de la revisión es APROBADA o DEVUELTA.");
        }
    }
}
