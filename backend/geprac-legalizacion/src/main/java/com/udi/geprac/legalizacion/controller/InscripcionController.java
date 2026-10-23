package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.dto.InscripcionCambiosDto;
import com.udi.geprac.legalizacion.dto.InscripcionDto;
import com.udi.geprac.legalizacion.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * CU-05 · Inscribir la práctica del periodo. Solo el estudiante inscribe, y
 * solo opera sus propias inscripciones: ninguna ruta recibe el estudiante,
 * porque sale de su token y de su registro en MS-01.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@RestController
@RequestMapping("/inscripciones")
public class InscripcionController {

    private final InscripcionService servicio;

    public InscripcionController(InscripcionService servicio) {
        this.servicio = servicio;
    }

    /**
     * POST /api/inscripciones: la inscripción de la práctica que le corresponde
     * en el semestre abierto. La crea en borrador, con la copia de su
     * expediente que le pide a MS-01, o devuelve la que ya había iniciado (P-05).
     */
    @PostMapping
    public InscripcionDto iniciarInscripcion() {
        return servicio.iniciarInscripcion();
    }

    /** PUT /api/inscripciones/{id}: lo diligenciado en cualquiera de los tres pasos, sin cambiar el estado (P-05 a P-07). */
    @PutMapping("/{id}")
    public InscripcionDto actualizarInscripcion(@PathVariable("id") Long id,
                                                @Valid @RequestBody InscripcionCambiosDto datos) {
        return servicio.actualizarInscripcion(id, datos);
    }

    /** POST /api/inscripciones/{id}/envio: la envía, o la reenvía si fue devuelta, a revisión del tutor (P-07). */
    @PostMapping("/{id}/envio")
    public InscripcionDto enviarARevision(@PathVariable("id") Long id) {
        return servicio.enviarARevision(id);
    }

    /** GET /api/inscripciones/actual: la inscripción del semestre abierto con su estado y el motivo si fue devuelta (P-08). */
    @GetMapping("/actual")
    public InscripcionDto consultarEstado() {
        return servicio.consultarEstado();
    }
}
