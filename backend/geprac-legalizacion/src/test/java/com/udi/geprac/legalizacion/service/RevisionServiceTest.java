package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.domain.ResultadoRevision;
import com.udi.geprac.legalizacion.domain.Revision;
import com.udi.geprac.legalizacion.domain.Semestre;
import com.udi.geprac.legalizacion.dto.InscripcionDto;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import com.udi.geprac.legalizacion.repository.RevisionRepository;
import com.udi.geprac.legalizacion.repository.TutorPracticaRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.udi.geprac.legalizacion.Fabrica.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Las reglas de CU-06 sin base de datos: el tutor solo resuelve inscripciones
 * enviadas de las prácticas que tiene designadas, y una devolución lleva motivo.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class RevisionServiceTest {

    private static final UUID TUTOR = UUID.fromString("00000000-0000-0000-0000-00000000000a");

    private final TutorPracticaRepository designaciones = mock(TutorPracticaRepository.class);
    private final InscripcionRepository inscripciones = mock(InscripcionRepository.class);
    private final RevisionRepository revisiones = mock(RevisionRepository.class);
    private final RevisionService servicio = new RevisionService(designaciones, inscripciones, revisiones);

    private final Semestre semestre = semestre(1L, "2026-2", true);
    private final Practica designada = practica(3L, 3);

    @BeforeEach
    void ingresaElTutor() {
        ingresa(TUTOR, "TUTOR");
        given(designaciones.findByTutorIdAndSemestreAbiertoTrue(TUTOR))
            .willReturn(List.of(designacion(TUTOR, semestre, designada)));
        given(revisiones.save(any(Revision.class))).willAnswer(i -> i.getArgument(0));
        given(inscripciones.save(any(Inscripcion.class))).willAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void cierra() {
        sale();
    }

    @Test
    void sinDesignacionesNoHayBandeja() {
        given(designaciones.findByTutorIdAndSemestreAbiertoTrue(TUTOR)).willReturn(List.of());

        assertThatThrownBy(servicio::listarPendientes)
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("no le ha designado prácticas");
    }

    @Test
    void laBandejaTraeLasEnviadasDeSusPracticas() {
        given(inscripciones.findBySemestreAndPracticaInAndEstado(semestre, List.of(designada), EstadoInscripcion.ENVIADA))
            .willReturn(List.of(enviada(designada)));

        assertThat(servicio.listarPendientes()).extracting(InscripcionDto::estado).containsExactly("ENVIADA");
    }

    @Test
    void unaDevolucionSinMotivoNoSeRegistra() {
        assertThatThrownBy(() -> servicio.registrarRevision(4L, "DEVUELTA", "  "))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("motivo");
        verify(revisiones, never()).save(any());
    }

    @Test
    void noResuelveUnaInscripcionDeUnaPracticaQueNoTieneDesignada() {
        given(inscripciones.findById(4L)).willReturn(Optional.of(enviada(practica(5L, 5))));

        assertThatThrownBy(() -> servicio.registrarRevision(4L, "APROBADA", null))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void unaInscripcionYaResueltaNoSeRevisaDeNuevo() {
        given(inscripciones.findById(4L))
            .willReturn(Optional.of(inscripcion(4L, 7L, designada, semestre, EstadoInscripcion.APROBADA)));

        assertThatThrownBy(() -> servicio.registrarRevision(4L, "DEVUELTA", "Falta la EPS"))
            .isInstanceOf(IllegalStateException.class).hasMessage("La inscripción ya fue resuelta: está aprobada.");
    }

    @Test
    void laAprobacionQuedaRegistradaConSuAutor() {
        given(inscripciones.findById(4L)).willReturn(Optional.of(enviada(designada)));

        InscripcionDto r = servicio.registrarRevision(4L, "aprobada", "Buen trabajo");

        ArgumentCaptor<Revision> revision = ArgumentCaptor.forClass(Revision.class);
        verify(revisiones).save(revision.capture());
        assertThat(revision.getValue().getRevisorId()).isEqualTo(TUTOR);
        assertThat(revision.getValue().getResultado()).isEqualTo(ResultadoRevision.APROBADA);
        assertThat(revision.getValue().getMotivo()).as("el motivo solo se registra al devolver").isNull();
        assertThat(r.estado()).isEqualTo("APROBADA");
    }

    @Test
    void laDevolucionLlevaSuMotivoAlEstudiante() {
        given(inscripciones.findById(4L)).willReturn(Optional.of(enviada(designada)));

        InscripcionDto r = servicio.registrarRevision(4L, "DEVUELTA", " Actualice la EPS. ");

        assertThat(r.estado()).isEqualTo("DEVUELTA");
        assertThat(r.motivoDevolucion()).isEqualTo("Actualice la EPS.");
        assertThat(r.revisiones()).extracting(InscripcionDto.RevisionResumen::resultado).containsExactly("DEVUELTA");
    }

    private Inscripcion enviada(Practica p) {
        return inscripcion(4L, 7L, p, semestre, EstadoInscripcion.ENVIADA);
    }
}
