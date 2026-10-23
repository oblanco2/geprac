package com.udi.geprac.legalizacion.dto;

import com.udi.geprac.legalizacion.domain.FormatoGenerado;
import com.udi.geprac.legalizacion.domain.TipoFormato;
import java.time.LocalDateTime;

/**
 * Uno de los cuatro formatos de una inscripción, con su estado de emisión
 * (P-09). Si todavía no se ha emitido, solo trae su código y su nombre.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record FormatoDto(Long id, String tipo, String nombre, String version, LocalDateTime fechaEmision,
                         String archivo) {

    public static FormatoDto de(TipoFormato tipo, FormatoGenerado emitido) {
        if (emitido == null) return new FormatoDto(null, tipo.codigo(), tipo.nombre(), null, null, null);
        return new FormatoDto(emitido.getId(), tipo.codigo(), tipo.nombre(), emitido.getPlantilla().getVersion(),
            emitido.getFechaEmision(), emitido.getArchivo());
    }
}
