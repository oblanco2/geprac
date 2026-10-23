package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.dto.PracticaDto;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import com.udi.geprac.legalizacion.repository.PracticaRepository;
import com.udi.geprac.legalizacion.repository.TutorPracticaRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static com.udi.geprac.legalizacion.Fabrica.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Las reglas de CU-02 sin base de datos: el catálogo es el del programa del
 * token, no hay dos prácticas en la misma posición del plan, cada práctica
 * lleva sus dos listas en su orden y una práctica con inscripciones no se
 * elimina.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class PracticaServiceTest {

    private final PracticaRepository practicas = mock(PracticaRepository.class);
    private final InscripcionRepository inscripciones = mock(InscripcionRepository.class);
    private final TutorPracticaRepository designaciones = mock(TutorPracticaRepository.class);
    private final PracticaService servicio = new PracticaService(practicas, inscripciones, designaciones);

    private final Practica tercera = practica(3L, 3);

    @BeforeEach
    void ingresaElDirector() {
        ingresa(UUID.randomUUID(), "DIRECTOR", "programa", "LEI");
        given(practicas.save(any(Practica.class))).willAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void cierra() {
        sale();
    }

    @Test
    void elCatalogoEsElDelProgramaDelToken() {
        given(practicas.findByCodigoProgramaOrderByOrden("LEI")).willReturn(List.of(practica(1L, 1), tercera));

        assertThat(servicio.listarPracticas()).extracting(PracticaDto::orden).containsExactly(1, 3);
    }

    @Test
    void unaPracticaNuevaSeGuardaConSusDosListasEnSuOrden() {
        given(practicas.findByCodigoProgramaAndOrden("LEI", 8)).willReturn(Optional.empty());

        PracticaDto r = servicio.crearPractica(datos(8));

        assertThat(r.orden()).isEqualTo(8);
        assertThat(r.objetivos()).containsExactly("Reconocer el contexto de la ludoteca.", "Planear dos experiencias.");
        assertThat(r.actividades()).containsExactly("Observar una jornada.");
        assertThat(r.horarioEstandar()).as("un texto vacío se guarda como nulo").isNull();
    }

    @Test
    void laPosicionOcupadaSeSenalaConLaPracticaQueLaOcupa() {
        given(practicas.findByCodigoProgramaAndOrden("LEI", 3)).willReturn(Optional.of(tercera));

        assertThatThrownBy(() -> servicio.crearPractica(datos(3)))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> {
                assertThat(e.getCampo()).isEqualTo("orden");
                assertThat(e.getEstado()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(e.getMessage()).isEqualTo("Ya hay una práctica en esa posición del plan: 3 · Práctica pedagógica 3.");
            });
        verify(practicas, never()).save(any());
    }

    @Test
    void alActualizarlaConservaSuPropiaPosicion() {
        given(practicas.findById(3L)).willReturn(Optional.of(tercera));
        given(practicas.findByCodigoProgramaAndOrden("LEI", 3)).willReturn(Optional.of(tercera));

        PracticaDto r = servicio.actualizarPractica(3L, datos(3));

        assertThat(r.nombre()).isEqualTo("Práctica en espacios no convencionales");
        assertThat(r.objetivos()).hasSize(2);
    }

    @Test
    void noSeReubicaEnLaPosicionDeOtra() {
        given(practicas.findById(3L)).willReturn(Optional.of(tercera));
        given(practicas.findByCodigoProgramaAndOrden("LEI", 1)).willReturn(Optional.of(practica(1L, 1)));

        assertThatThrownBy(() -> servicio.actualizarPractica(3L, datos(1)))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("orden"));
    }

    @Test
    void laPracticaDeOtroProgramaNoExisteParaElDirector() {
        Practica otra = practica(9L, 2);
        poner(otra, "codigoPrograma", "LLC");
        given(practicas.findById(9L)).willReturn(Optional.of(otra));

        assertThatThrownBy(() -> servicio.actualizarPractica(9L, datos(2))).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> servicio.eliminarPractica(9L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void unaPracticaConInscripcionesNoSeElimina() {
        given(practicas.findById(3L)).willReturn(Optional.of(tercera));
        given(inscripciones.countByPractica(tercera)).willReturn(4L);

        assertThatThrownBy(() -> servicio.eliminarPractica(3L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageStartingWith("La práctica «3 · Práctica pedagógica 3» tiene 4 inscripciones asociadas: no se eliminó.");
        verify(practicas, never()).delete(any());
    }

    @Test
    void sinInscripcionesSeEliminaConSusDesignaciones() {
        given(practicas.findById(3L)).willReturn(Optional.of(tercera));

        servicio.eliminarPractica(3L);

        verify(designaciones).deleteByPractica(tercera);
        verify(practicas).delete(tercera);
    }

    private static PracticaDto datos(int orden) {
        return new PracticaDto(null, orden, " Práctica en espacios no convencionales ", "Diseñar experiencias fuera del aula.",
            "  ", true, List.of("Reconocer el contexto de la ludoteca.", " Planear dos experiencias. "),
            List.of("Observar una jornada."));
    }
}
