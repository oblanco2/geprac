package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.FormatoGenerado;
import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.PlantillaFormato;
import com.udi.geprac.legalizacion.domain.TipoFormato;
import com.udi.geprac.legalizacion.dto.FormatoDto;
import com.udi.geprac.legalizacion.formatos.DatoFaltanteException;
import com.udi.geprac.legalizacion.repository.FormatoGeneradoRepository;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import com.udi.geprac.legalizacion.repository.PlantillaFormatoRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Las reglas de CU-08 sin base de datos: los formatos solo salen de una
 * inscripción propia y avalada, se emiten los cuatro o ninguno, y la descarga
 * entrega el PDF de la emisión.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class FormatoServiceTest {

    private static final long ESTUDIANTE = 7L;

    private final InscripcionRepository inscripciones = mock(InscripcionRepository.class);
    private final PlantillaFormatoRepository plantillas = mock(PlantillaFormatoRepository.class);
    private final FormatoGeneradoRepository formatos = mock(FormatoGeneradoRepository.class);
    private final FormatoService servicio = new FormatoService(inscripciones, plantillas, formatos);

    private Inscripcion avalada;

    @BeforeEach
    void ingresaElEstudiante() {
        ingresa(UUID.randomUUID(), "ESTUDIANTE", "programa", "LEI", "estudiante_id", ESTUDIANTE);
        avalada = inscripcion(5L, ESTUDIANTE, practica(3L, 3), semestre(1L, "2026-2", true), EstadoInscripcion.AVALADA);
        given(inscripciones.findById(5L)).willReturn(Optional.of(avalada));
        given(plantillas.findByVigenteTrue()).willReturn(vigentes(TipoFormato.values()));
        given(formatos.findByInscripcionId(5L)).willReturn(List.of());
        given(formatos.saveAll(anyList())).willAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void cierra() {
        sale();
    }

    @Test
    void sinAvalLosFormatosNoSeHabilitan() {
        poner(avalada, "estado", EstadoInscripcion.APROBADA);

        assertThatThrownBy(() -> servicio.consultarFormatos(5L))
            .isInstanceOf(IllegalStateException.class).hasMessageEndingWith("hoy está «aprobada».");
    }

    @Test
    void laInscripcionDeOtroEstudianteNoSePresenta() {
        poner(avalada, "estudianteId", 99L);

        assertThatThrownBy(() -> servicio.consultarFormatos(5L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void sinPlantillaVigenteNoSeEmiteNinguno() {
        given(plantillas.findByVigenteTrue()).willReturn(vigentes(TipoFormato.PR01, TipoFormato.PR02, TipoFormato.PR04));

        assertThatThrownBy(() -> servicio.emitirFormatos(5L))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("PR-05");
        verify(formatos, never()).saveAll(anyList());
    }

    @Test
    void seEmitenLosCuatroConSuPlantillaYSuFecha() {
        List<FormatoDto> r = servicio.emitirFormatos(5L);

        assertThat(r).extracting(FormatoDto::tipo).containsExactly("PR-01", "PR-02", "PR-04", "PR-05");
        assertThat(r).allSatisfy(f -> {
            assertThat(f.version()).isEqualTo("1");
            assertThat(f.fechaEmision()).isNotNull();
            assertThat(f.archivo()).endsWith("_1099331208_2026-2.pdf");
        });
    }

    @Test
    void unDatoFaltanteNoDejaUnaEmisionParcial() {
        poner(avalada.getDatos(), "direccion", null);

        assertThatThrownBy(() -> servicio.emitirFormatos(5L))
            .isInstanceOf(DatoFaltanteException.class)
            .hasMessageContaining("la dirección del estudiante").hasMessageContaining("PR-01");
        verify(formatos, never()).saveAll(anyList());
    }

    @Test
    void emitirDeNuevoReemplazaLaEmisionAnterior() {
        FormatoGenerado anterior = poner(new FormatoGenerado(avalada, TipoFormato.PR01), "id", 40L);
        anterior.emitir(vigentes(TipoFormato.PR01).get(0), "PR-01_viejo.pdf", LocalDateTime.of(2026, 9, 1, 8, 0));
        given(formatos.findByInscripcionId(5L)).willReturn(List.of(anterior));

        servicio.emitirFormatos(5L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FormatoGenerado>> guardados = ArgumentCaptor.forClass(List.class);
        verify(formatos).saveAll(guardados.capture());
        assertThat(guardados.getValue()).hasSize(4).contains(anterior);
        assertThat(anterior.getFechaEmision()).isAfter(LocalDateTime.of(2026, 9, 1, 8, 0));
    }

    @Test
    void laDescargaEntregaElPdfDeLaEmision() {
        FormatoGenerado emitido = poner(new FormatoGenerado(avalada, TipoFormato.PR04), "id", 41L);
        emitido.emitir(vigentes(TipoFormato.PR04).get(0), "PR-04_1099331208_2026-2.pdf", LocalDateTime.of(2026, 10, 9, 9, 0));
        given(formatos.findById(41L)).willReturn(Optional.of(emitido));

        String pdf = new String(servicio.descargarFormato(41L), StandardCharsets.ISO_8859_1);

        assertThat(pdf).startsWith("%PDF-1.4").contains("SOLICITUD Y APROBACI").contains("Valeria Cruz Rangel")
            .contains("9 de octubre de 2026");
    }

    private static List<PlantillaFormato> vigentes(TipoFormato... tipos) {
        List<PlantillaFormato> l = new ArrayList<>();
        Arrays.stream(tipos).forEach(t -> {
            PlantillaFormato p = nuevo(PlantillaFormato.class);
            poner(p, "tipo", t);
            poner(p, "version", "1");
            poner(p, "vigente", true);
            poner(p, "archivo", "plantillas/" + t.codigo() + "_v1.txt");
            l.add(p);
        });
        return l;
    }
}
