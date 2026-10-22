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
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import static com.udi.geprac.legalizacion.Fabrica.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Las reglas de CU-04 que guarda MS-02, sin base de datos: un solo semestre
 * abierto, código único y fechas en orden; un tutor por práctica y por
 * semestre, y solo quien tiene en MS-01 el rol de tutor académico.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class SemestreServiceTest {

    private static final UUID TUTOR = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    private static final UUID OTRO = UUID.fromString("00000000-0000-0000-0000-0000000000b3");

    private final SemestreRepository semestres = mock(SemestreRepository.class);
    private final PracticaRepository practicas = mock(PracticaRepository.class);
    private final TutorPracticaRepository designaciones = mock(TutorPracticaRepository.class);
    private final InscripcionRepository inscripciones = mock(InscripcionRepository.class);
    private final IdentidadCliente identidad = mock(IdentidadCliente.class);
    private final SemestreService servicio = new SemestreService(semestres, practicas, designaciones, inscripciones, identidad);

    private final Semestre abierto = semestre(5L, "2026-2", true);
    private final Practica primera = practica(1L, 1);
    private final Practica tercera = practica(3L, 3);

    @BeforeEach
    void ingresaElDirector() {
        ingresa(UUID.randomUUID(), "DIRECTOR", "programa", "LEI");
        given(semestres.save(any(Semestre.class))).willAnswer(i -> i.getArgument(0));
        given(designaciones.save(any(TutorPractica.class))).willAnswer(i -> i.getArgument(0));
        given(identidad.consultarRol(TUTOR)).willReturn("TUTOR");
        given(practicas.findById(3L)).willReturn(Optional.of(tercera));
    }

    @AfterEach
    void cierra() {
        sale();
    }

    @Test
    void laPreparacionTraeElSemestreAbiertoYElTutorDeCadaPractica() {
        given(practicas.findByCodigoProgramaOrderByOrden("LEI")).willReturn(List.of(primera, tercera));
        given(semestres.findByAbiertoTrue()).willReturn(Optional.of(abierto));
        given(designaciones.findBySemestre(abierto)).willReturn(List.of(designacion(TUTOR, abierto, tercera)));
        given(inscripciones.findBySemestreAndPracticaInAndEstado(eq(abierto), anyList(), eq(EstadoInscripcion.ENVIADA)))
            .willReturn(List.of(inscripcion(7L, 1L, tercera, abierto, EstadoInscripcion.ENVIADA),
                inscripcion(8L, 2L, tercera, abierto, EstadoInscripcion.ENVIADA)));

        PreparacionDto r = servicio.consultarPreparacion();

        assertThat(r.semestre().codigo()).isEqualTo("2026-2");
        assertThat(r.practicas()).extracting(DesignacionDto::tutorId).containsExactly(null, TUTOR);
        assertThat(r.practicas()).extracting(DesignacionDto::enviadas).containsExactly(0L, 2L);
    }

    @Test
    void sinSemestreAbiertoPresentaElUltimo() {
        given(practicas.findByCodigoProgramaOrderByOrden("LEI")).willReturn(List.of(primera));
        given(semestres.findByAbiertoTrue()).willReturn(Optional.empty());
        given(semestres.findFirstByOrderByFechaInicioDesc()).willReturn(Optional.of(semestre(4L, "2026-1", false)));

        assertThat(servicio.consultarPreparacion().semestre().abierto()).isFalse();
    }

    @Test
    void sinSemestresLasPracticasVienenSinTutor() {
        given(practicas.findByCodigoProgramaOrderByOrden("LEI")).willReturn(List.of(primera));
        given(semestres.findByAbiertoTrue()).willReturn(Optional.empty());
        given(semestres.findFirstByOrderByFechaInicioDesc()).willReturn(Optional.empty());

        PreparacionDto r = servicio.consultarPreparacion();

        assertThat(r.semestre()).isNull();
        assertThat(r.practicas()).extracting(DesignacionDto::tutorId).containsOnlyNulls();
    }

    @Test
    void laFechaDeCierreDebeSerPosteriorALaDeInicio() {
        assertThatThrownBy(() -> servicio.abrirSemestre(nuevo("2027-1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 1))))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("fechaCierre"));
    }

    @Test
    void noSeAbreOtroMientrasHayaUnoAbierto() {
        given(semestres.findByAbiertoTrue()).willReturn(Optional.of(abierto));

        assertThatThrownBy(() -> servicio.abrirSemestre(nuevo("2027-1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 6, 5))))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("el 2026-2 sigue abierto");
        verify(semestres, never()).save(any());
    }

    @Test
    void elCodigoDelSemestreNoSeRepite() {
        given(semestres.findByAbiertoTrue()).willReturn(Optional.empty());
        given(semestres.existsByCodigo("2026-2")).willReturn(true);

        assertThatThrownBy(() -> servicio.abrirSemestre(nuevo("2026-2", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 6, 5))))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> {
                assertThat(e.getCampo()).isEqualTo("codigo");
                assertThat(e.getEstado()).isEqualTo(HttpStatus.CONFLICT);
            });
    }

    @Test
    void elSemestreNuevoQuedaAbierto() {
        given(semestres.findByAbiertoTrue()).willReturn(Optional.empty());

        SemestreDto r = servicio.abrirSemestre(nuevo(" 2027-1 ", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 6, 5)));

        assertThat(r.codigo()).isEqualTo("2027-1");
        assertThat(r.abierto()).isTrue();
    }

    @Test
    void elSemestreVigenteSeCierraUnaSolaVez() {
        given(semestres.findById(5L)).willReturn(Optional.of(abierto));

        assertThat(servicio.cerrarSemestre(5L).abierto()).isFalse();
        assertThatThrownBy(() -> servicio.cerrarSemestre(5L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("ya está cerrado");
        assertThatThrownBy(() -> servicio.cerrarSemestre(6L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void soloSeDesignaAQuienTieneElRolDeTutor() {
        given(identidad.consultarRol(OTRO)).willReturn("ESTUDIANTE");

        assertThatThrownBy(() -> servicio.designarTutor(3L, OTRO))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> {
                assertThat(e.getCampo()).isEqualTo("tutorId");
                assertThat(e.getMessage()).contains("primero debe asignárselo");
            });
        verify(designaciones, never()).save(any());
    }

    @Test
    void sinSemestreAbiertoNoSeDesigna() {
        given(semestres.findByAbiertoTrue()).willReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.designarTutor(3L, TUTOR))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("No hay semestre abierto");
    }

    @Test
    void unaPracticaConTutorNoSeDesignaDeNuevo() {
        given(semestres.findByAbiertoTrue()).willReturn(Optional.of(abierto));
        given(designaciones.existsBySemestreAndPracticaId(abierto, 3L)).willReturn(true);

        assertThatThrownBy(() -> servicio.designarTutor(3L, TUTOR))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("ya tiene tutor designado en el semestre 2026-2");
        verify(designaciones, never()).save(any());
    }

    @Test
    void laDesignacionQuedaGuardadaConLasEnviadasQuePasanASuBandeja() {
        given(semestres.findByAbiertoTrue()).willReturn(Optional.of(abierto));
        given(inscripciones.findBySemestreAndPracticaInAndEstado(abierto, List.of(tercera), EstadoInscripcion.ENVIADA))
            .willReturn(List.of(inscripcion(7L, 1L, tercera, abierto, EstadoInscripcion.ENVIADA)));

        DesignacionDto r = servicio.designarTutor(3L, TUTOR);

        ArgumentCaptor<TutorPractica> d = ArgumentCaptor.forClass(TutorPractica.class);
        verify(designaciones).save(d.capture());
        assertThat(d.getValue().getTutorId()).isEqualTo(TUTOR);
        assertThat(d.getValue().getSemestre()).isSameAs(abierto);
        assertThat(r.enviadas()).isEqualTo(1L);
    }

    @Test
    void laReasignacionReemplazaAlTutor() {
        given(identidad.consultarRol(OTRO)).willReturn("TUTOR");
        given(semestres.findByAbiertoTrue()).willReturn(Optional.of(abierto));
        TutorPractica actual = designacion(TUTOR, abierto, tercera);
        given(designaciones.findBySemestreAndPracticaId(abierto, 3L)).willReturn(Optional.of(actual));

        assertThat(servicio.reasignarTutor(3L, OTRO).tutorId()).isEqualTo(OTRO);
        assertThat(actual.getTutorId()).isEqualTo(OTRO);
        verify(designaciones).save(actual);
    }

    @Test
    void noSeDesignaEnUnaPracticaDeOtroPrograma() {
        Practica otra = practica(9L, 2);
        poner(otra, "codigoPrograma", "LLC");
        given(practicas.findById(9L)).willReturn(Optional.of(otra));
        given(semestres.findByAbiertoTrue()).willReturn(Optional.of(abierto));

        assertThatThrownBy(() -> servicio.designarTutor(9L, TUTOR)).isInstanceOf(NoSuchElementException.class);
    }

    private static SemestreDto nuevo(String codigo, LocalDate inicio, LocalDate cierre) {
        return new SemestreDto(null, codigo, inicio, cierre, null);
    }
}
