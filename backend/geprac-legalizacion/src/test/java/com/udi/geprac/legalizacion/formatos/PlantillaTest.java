package com.udi.geprac.legalizacion.formatos;

import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.Inscripcion;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.udi.geprac.legalizacion.Fabrica.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Las plantillas y el documento PDF: las cuatro plantillas de la versión 1 se
 * diligencian con la copia de una inscripción, una lista vacía deja su leyenda
 * y el texto que no cabe pasa a la página siguiente.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class PlantillaTest {

    private final Inscripcion inscripcion = inscripcion(5L, 7L, practica(3L, 3), semestre(1L, "2026-2", true),
        EstadoInscripcion.AVALADA);
    private final DatosFormato datos = DatosFormato.de(inscripcion, LocalDateTime.of(2026, 10, 9, 9, 30));

    @ParameterizedTest
    @ValueSource(strings = { "PR-01", "PR-02", "PR-04", "PR-05" })
    void lasCuatroPlantillasVigentesSeDiligencian(String codigo) throws IOException {
        Plantilla p = Plantilla.leer(recurso("/plantillas/" + codigo + "_v1.txt"));

        String pdf = new String(p.diligenciar(datos, "1", "Emitido por GEPRAC"), StandardCharsets.ISO_8859_1);

        assertThat(p.codigo()).isEqualTo(codigo);
        assertThat(pdf).startsWith("%PDF-1.4").endsWith("%%EOF\n").contains("Valeria").contains("(Código: " + codigo + ")");
    }

    @Test
    void sinTituloNiCodigoNoEsUnaPlantilla() {
        assertThatThrownBy(() -> Plantilla.leer("= Datos\n| Nombres: {nombres} |"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unaListaVaciaDejaSuLeyenda() {
        Plantilla p = Plantilla.leer("@titulo PRUEBA\n@codigo PR-00\n*[No registra referencias.] {referencias.nombre}");
        DatosFormato sinReferencias = new DatosFormato(datos.campos(), Map.of("referencias", List.of()));

        assertThat(new String(p.diligenciar(sinReferencias, "1", "pie"), StandardCharsets.ISO_8859_1))
            .contains("(No registra referencias.)");
    }

    @Test
    void elTextoQueNoCabePasaALaPaginaSiguiente() {
        DocumentoPdf doc = new DocumentoPdf("PRUEBA", "PR-00", "1", "pie");
        for (int k = 0; k < 80; k++) doc.parrafo("Renglón " + k + " del documento de prueba.");

        assertThat(doc.paginas()).isGreaterThan(1);
        assertThat(new String(doc.cerrar(), StandardCharsets.ISO_8859_1)).contains("/Count " + doc.paginas());
    }

    private static String recurso(String ruta) throws IOException {
        try (InputStream in = PlantillaTest.class.getResourceAsStream(ruta)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
