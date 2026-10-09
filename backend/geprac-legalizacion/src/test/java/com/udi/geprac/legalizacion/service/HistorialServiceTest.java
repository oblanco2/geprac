package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.domain.Semestre;
import com.udi.geprac.legalizacion.dto.HistorialDto;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import com.udi.geprac.legalizacion.repository.PracticaRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static com.udi.geprac.legalizacion.Fabrica.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Las reglas de CU-09 sin base de datos: un solo historial con tres alcances,
 * que decide el rol del token.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class HistorialServiceTest {

    private static final UUID TUTOR = UUID.fromString("00000000-0000-0000-0000-00000000000a");

    private final InscripcionRepository inscripciones = mock(InscripcionRepository.class);
    private final PracticaRepository practicas = mock(PracticaRepository.class);
    private final HistorialService servicio = new HistorialService(inscripciones, practicas);

    private final Semestre anterior = semestre(1L, "2026-1", false);
    private final Semestre abierto = semestre(2L, "2026-2", true);
    private final List<Practica> plan = IntStream.rangeClosed(1, 8).mapToObj(k -> practica(k, k)).toList();

    @AfterEach
    void cierra() {
        sale();
    }

    @Test
    void elEstudianteVeLasOchoPracticasDeSuPlan() {
        ingresa(UUID.randomUUID(), "ESTUDIANTE", "programa", "LEI", "estudiante_id", 7L);
        given(inscripciones.findByEstudianteId(7L)).willReturn(List.of(
            inscripcion(1L, 7L, plan.get(0), anterior, EstadoInscripcion.AVALADA),
            inscripcion(2L, 7L, plan.get(1), abierto, EstadoInscripcion.ENVIADA)));
        given(practicas.findByCodigoProgramaOrderByOrden("LEI")).willReturn(plan);

        List<HistorialDto> r = servicio.consultarHistorial();

        assertThat(r).extracting(HistorialDto::ordenPractica).containsExactly(1, 2, 3, 4, 5, 6, 7, 8);
        assertThat(r).extracting(HistorialDto::estado)
            .containsExactly("AVALADA", "ENVIADA", null, null, null, null, null, null);
    }

    @Test
    void sinPerfilNoSePuedeCompletarElPlan() {
        ingresa(UUID.randomUUID(), "ESTUDIANTE", "programa", "LEI");

        assertThatThrownBy(servicio::consultarHistorial)
            .isInstanceOf(IllegalStateException.class).hasMessageStartingWith("Todavía no ha registrado su perfil");
    }

    @Test
    void elTutorNoVeLasInscripcionesEnBorrador() {
        ingresa(TUTOR, "TUTOR");
        given(inscripciones.findDesignadasAlTutor(TUTOR)).willReturn(List.of(
            inscripcion(3L, 8L, plan.get(2), abierto, EstadoInscripcion.BORRADOR),
            inscripcion(4L, 9L, plan.get(2), abierto, EstadoInscripcion.ENVIADA)));

        assertThat(servicio.consultarHistorial()).extracting(HistorialDto::inscripcionId).containsExactly(4L);
    }

    @Test
    void laDireccionVeTodoSuProgramaDelSemestreMasReciente() {
        ingresa(UUID.randomUUID(), "DIRECTOR", "programa", "LEI");
        given(inscripciones.findByPracticaCodigoPrograma("LEI")).willReturn(List.of(
            inscripcion(1L, 7L, plan.get(0), anterior, EstadoInscripcion.AVALADA),
            inscripcion(5L, 8L, plan.get(2), abierto, EstadoInscripcion.BORRADOR)));

        assertThat(servicio.consultarHistorial()).extracting(HistorialDto::semestre).containsExactly("2026-2", "2026-1");
    }

    @Test
    void unaCuentaSinRolNoTieneHistorial() {
        ingresa(UUID.randomUUID(), null);

        assertThatThrownBy(servicio::consultarHistorial)
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("asignación del rol");
    }
}
