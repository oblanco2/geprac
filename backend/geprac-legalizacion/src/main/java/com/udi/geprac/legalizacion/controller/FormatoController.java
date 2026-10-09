package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.dto.FormatoDto;
import com.udi.geprac.legalizacion.service.FormatoService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * CU-08 · Emitir los formatos institucionales. Solo el estudiante, y siempre
 * sobre sus propias inscripciones avaladas.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@RestController
@RequestMapping("/formatos")
public class FormatoController {

    private final FormatoService servicio;

    public FormatoController(FormatoService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/formatos?inscripcion={id}: los cuatro formatos con su estado de emisión (P-09). */
    @GetMapping
    public List<FormatoDto> consultarFormatos(@RequestParam("inscripcion") Long inscripcionId) {
        return servicio.consultarFormatos(inscripcionId);
    }

    /** POST /api/formatos?inscripcion={id}: emite los cuatro, o los emite de nuevo. */
    @PostMapping
    public List<FormatoDto> emitirFormatos(@RequestParam("inscripcion") Long inscripcionId) {
        return servicio.emitirFormatos(inscripcionId);
    }

    /** GET /api/formatos/{id}/archivo: el documento en PDF, tal como fue emitido. */
    @GetMapping(value = "/{id}/archivo", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] descargarFormato(@PathVariable("id") Long formatoId) {
        return servicio.descargarFormato(formatoId);
    }
}
