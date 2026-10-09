package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.Aval;
import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.domain.Semestre;
import com.udi.geprac.legalizacion.dto.InscripcionDto;
import com.udi.geprac.legalizacion.repository.AvalRepository;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
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
 * Las reglas de CU-07 sin base de datos: el director avala solo inscripciones
 * aprobadas de su programa, y el aval queda con su autor y su fecha.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class AvalServiceTest {

    private static final UUID DIRECTOR = UUID.fromString("00000000-0000-0000-0000-00000000000d");

    private final InscripcionRepository inscripciones = mock(InscripcionRepository.class);
    private final AvalRepository avales = mock(AvalRepository.class);
    private final AvalService servicio = new AvalService(inscripciones, avales);

    private final Semestre abierto = semestre(1L, "2026-2", true);
    private final Practica practica = practica(3L, 3);

    @BeforeEach
    void ingresaElDirector() {
        ingresa(DIRECTOR, "DIRECTOR", "programa", "LEI");
        given(avales.save(any(Aval.class))).willAnswer(i -> i.getArgument(0));
        given(inscripciones.save(any(Inscripcion.class))).willAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void cierra() {
        sale();
    }

    @Test
    void laBandejaTraeLasAprobadasDelProgramaDelToken() {
        given(inscripciones.findByEstadoAndPracticaCodigoPrograma(EstadoInscripcion.APROBADA, "LEI"))
            .willReturn(List.of(inscripcion(5L, 7L, practica, abierto, EstadoInscripcion.APROBADA)));
        given(inscripciones.findByEstadoAndPracticaCodigoPrograma(EstadoInscripcion.AVALADA, "LEI")).willReturn(List.of());

        assertThat(servicio.listarAprobadas()).extracting(InscripcionDto::id).containsExactly(5L);
    }

    @Test
    void elAvalQuedaConSuAutorYLaInscripcionAvalada() {
        given(inscripciones.findById(5L))
            .willReturn(Optional.of(inscripcion(5L, 7L, practica, abierto, EstadoInscripcion.APROBADA)));

        InscripcionDto r = servicio.registrarAval(5L);

        ArgumentCaptor<Aval> aval = ArgumentCaptor.forClass(Aval.class);
        verify(avales).save(aval.capture());
        assertThat(aval.getValue().getDirectorId()).isEqualTo(DIRECTOR);
        assertThat(aval.getValue().getFecha()).isNotNull();
        assertThat(r.estado()).isEqualTo("AVALADA");
        assertThat(r.aval().directorId()).isEqualTo(DIRECTOR);
    }

    @Test
    void ningunaInscripcionLlegaAlAvalSinLaRevisionDelTutor() {
        given(inscripciones.findById(5L))
            .willReturn(Optional.of(inscripcion(5L, 7L, practica, abierto, EstadoInscripcion.ENVIADA)));

        assertThatThrownBy(() -> servicio.registrarAval(5L))
            .isInstanceOf(IllegalStateException.class).hasMessageEndingWith("esta está enviada.");
        verify(avales, never()).save(any());
    }

    @Test
    void noAvalaUnaInscripcionDeOtroPrograma() {
        Practica otra = poner(practica(9L, 3), "codigoPrograma", "LLC");
        given(inscripciones.findById(5L))
            .willReturn(Optional.of(inscripcion(5L, 7L, otra, abierto, EstadoInscripcion.APROBADA)));

        assertThatThrownBy(() -> servicio.registrarAval(5L)).isInstanceOf(NoSuchElementException.class);
        verify(avales, never()).save(any());
    }
}
