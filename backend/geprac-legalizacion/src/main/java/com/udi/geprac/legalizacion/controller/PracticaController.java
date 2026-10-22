package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.dto.PracticaDto;
import com.udi.geprac.legalizacion.service.PracticaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * CU-02 · Gestionar el catálogo de prácticas. Solo el director, y siempre sobre
 * el catálogo de su programa: de él precarga la inscripción su contenido
 * estandarizado (CU-05).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@RestController
@RequestMapping("/practicas")
public class PracticaController {

    private final PracticaService servicio;

    public PracticaController(PracticaService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/practicas: el catálogo del programa en el orden del plan, con sus dos listas (P-12). */
    @GetMapping
    public List<PracticaDto> listarPracticas() {
        return servicio.listarPracticas();
    }

    /** POST /api/practicas: una práctica nueva con sus objetivos y actividades (P-13). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PracticaDto crearPractica(@Valid @RequestBody PracticaDto datos) {
        return servicio.crearPractica(datos);
    }

    /** PUT /api/practicas/{id}: corrige la práctica {id} y sus dos listas (P-13). */
    @PutMapping("/{id}")
    public PracticaDto actualizarPractica(@PathVariable("id") Long id, @Valid @RequestBody PracticaDto datos) {
        return servicio.actualizarPractica(id, datos);
    }

    /** DELETE /api/practicas/{id}: elimina una práctica sin inscripciones (P-12). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPractica(@PathVariable("id") Long id) {
        servicio.eliminarPractica(id);
    }
}
