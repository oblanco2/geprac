package com.udi.geprac.academico.domain;

import com.udi.geprac.academico.dto.EstudianteDto;
import jakarta.persistence.Column;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El registro guarda completo lo que el formulario de P-03 deja escoger: cada
 * estado civil de la lista cabe en su columna, también «Unión marital de
 * hecho», el más largo. Las opciones se leen de la misma validación del
 * formulario, de modo que una opción nueva más larga hace fallar la prueba.
 *
 * @author Oscar Iván Blanco Díaz
 */
class EstudianteTest {

    @Test
    void cadaEstadoCivilDeLaListaCabeEnSuColumna() throws NoSuchFieldException {
        int largo = Estudiante.class.getDeclaredField("estadoCivil").getAnnotation(Column.class).length();
        List<String> opciones = Arrays.stream(EstudianteDto.ESTADOS_CIVILES.split("\\|"))
            .filter(o -> !o.isEmpty())
            .map(o -> o.replace("\\", ""))
            .toList();

        assertThat(opciones).contains("Unión marital de hecho").hasSize(6);
        assertThat(opciones).allSatisfy(o -> {
            assertThat(o).matches(EstudianteDto.ESTADOS_CIVILES);
            assertThat(o.length()).isLessThanOrEqualTo(largo);
        });
    }
}
