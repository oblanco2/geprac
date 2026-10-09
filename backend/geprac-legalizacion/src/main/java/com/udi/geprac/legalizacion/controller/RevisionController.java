package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.dto.InscripcionDto;
import com.udi.geprac.legalizacion.dto.RevisionDto;
import com.udi.geprac.legalizacion.service.RevisionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * CU-06 · Revisar la inscripción. Solo el tutor académico, y siempre dentro de
 * las prácticas que tiene designadas en el semestre abierto.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@RestController
@RequestMapping("/revisiones")
public class RevisionController {

    private final RevisionService servicio;

    public RevisionController(RevisionService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/revisiones/pendientes: la bandeja de revisión (P-10). */
    @GetMapping("/pendientes")
    public List<InscripcionDto> listarPendientes() {
        return servicio.listarPendientes();
    }

    /** POST /api/revisiones/{id}: aprueba o devuelve con motivo la inscripción {id} (P-11). */
    @PostMapping("/{id}")
    public InscripcionDto registrarRevision(@PathVariable("id") Long id, @Valid @RequestBody RevisionDto datos) {
        return servicio.registrarRevision(id, datos.resultado(), datos.motivo());
    }
}
