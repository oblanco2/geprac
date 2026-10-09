package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.ContactoInstitucion;
import com.udi.geprac.legalizacion.domain.Institucion;
import com.udi.geprac.legalizacion.dto.ContactoDto;
import com.udi.geprac.legalizacion.dto.InstitucionDto;
import com.udi.geprac.legalizacion.repository.InstitucionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static com.udi.geprac.legalizacion.Fabrica.poner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

/**
 * Las reglas de CU-03 sin base de datos: una institución no se repite en la
 * misma ciudad, sus contactos se corrigen sin perder los que quedan, y la que
 * tiene inscripciones no se elimina.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class InstitucionServiceTest {

    private final InstitucionRepository instituciones = mock(InstitucionRepository.class);
    private final InstitucionService servicio = new InstitucionService(instituciones);

    @Test
    void unaInstitucionNoSeRepiteEnLaMismaCiudad() {
        given(instituciones.existsByRazonSocialAndCiudad("Jardín Los Cerezos", "Bucaramanga")).willReturn(true);
        InstitucionDto datos = new InstitucionDto(null, " Jardín Los Cerezos ", null, "Calle 1", "Bucaramanga",
            "6076543210", null, null, null, true, List.of(new ContactoDto(null, "Ana", "Rectora", "300", null, null)));

        assertThatThrownBy(() -> servicio.crearInstitucion(datos))
            .isInstanceOf(IllegalStateException.class).hasMessage("Ya existe «Jardín Los Cerezos» en Bucaramanga.");
    }

    @Test
    void alCorregirLosContactosSeConservanSeAnadenYSeQuitan() {
        Institucion i = poner(new Institucion(), "id", 1L);
        i.setRazonSocial("Jardín Los Cerezos");
        i.setCiudad("Bucaramanga");
        ContactoInstitucion queda = poner(new ContactoInstitucion(i), "id", 10L);
        ContactoInstitucion sale = poner(new ContactoInstitucion(i), "id", 11L);
        i.getContactos().addAll(List.of(queda, sale));
        given(instituciones.findById(1L)).willReturn(Optional.of(i));
        given(instituciones.save(any(Institucion.class))).willAnswer(x -> x.getArgument(0));

        InstitucionDto r = servicio.actualizarInstitucion(1L, new InstitucionDto(1L, "Jardín Los Cerezos", null,
            "Calle 1", "Bucaramanga", "6076543210", null, null, null, true, List.of(
                new ContactoDto(10L, "Ana Ruiz", "Rectora", "300", null, null),
                new ContactoDto(null, "Luis Díaz", "Coordinador", "301", null, null))));

        assertThat(r.contactos()).extracting(ContactoDto::nombre).containsExactly("Ana Ruiz", "Luis Díaz");
        assertThat(i.getContactos()).doesNotContain(sale);
    }

    @Test
    void unaInstitucionConInscripcionesNoSeElimina() {
        given(instituciones.existsById(2L)).willReturn(true);
        willThrow(new DataIntegrityViolationException("fk_inscripcion_institucion")).given(instituciones).flush();

        assertThatThrownBy(() -> servicio.eliminarInstitucion(2L))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("márquela como inactiva");
    }
}
