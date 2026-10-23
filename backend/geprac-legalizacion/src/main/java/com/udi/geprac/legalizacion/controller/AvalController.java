package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.dto.InscripcionDto;
import com.udi.geprac.legalizacion.service.AvalService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * CU-07 · Avalar la inscripción. Solo el director, y siempre sobre las
 * inscripciones de su programa.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@RestController
@RequestMapping("/avales")
public class AvalController {

    private final AvalService servicio;

    public AvalController(AvalService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/avales/pendientes: la bandeja de aval (P-17). */
    @GetMapping("/pendientes")
    public List<InscripcionDto> listarAprobadas() {
        return servicio.listarAprobadas();
    }

    /** POST /api/avales/{id}: avala la inscripción {id} (P-18). */
    @PostMapping("/{id}")
    public InscripcionDto registrarAval(@PathVariable("id") Long id) {
        return servicio.registrarAval(id);
    }
}
