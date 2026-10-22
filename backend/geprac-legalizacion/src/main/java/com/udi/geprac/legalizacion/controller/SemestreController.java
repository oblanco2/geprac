package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.dto.DesignacionDto;
import com.udi.geprac.legalizacion.dto.PreparacionDto;
import com.udi.geprac.legalizacion.dto.SemestreDto;
import com.udi.geprac.legalizacion.dto.TutorDto;
import com.udi.geprac.legalizacion.service.SemestreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * CU-04 · Preparar el semestre: el semestre académico y los tutores de cada
 * práctica. Solo el director. La lista de usuarios con su rol, que la misma
 * pantalla presenta, la atiende MS-01.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@RestController
@RequestMapping("/semestres")
public class SemestreController {

    private final SemestreService servicio;

    public SemestreController(SemestreService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/semestres/preparacion: el semestre y las prácticas del programa con su tutor (P-16). */
    @GetMapping("/preparacion")
    public PreparacionDto consultarPreparacion() {
        return servicio.consultarPreparacion();
    }

    /** POST /api/semestres: abre un semestre nuevo con su código y sus fechas (P-16, pasos 3 a 8). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SemestreDto abrirSemestre(@Valid @RequestBody SemestreDto datos) {
        return servicio.abrirSemestre(datos);
    }

    /** POST /api/semestres/{id}/cierre: cierra el semestre vigente (P-16, variación 2.2). */
    @PostMapping("/{id}/cierre")
    public SemestreDto cerrarSemestre(@PathVariable("id") Long id) {
        return servicio.cerrarSemestre(id);
    }

    /** POST /api/semestres/abierto/practicas/{practicaId}/tutor: designa el tutor de una práctica (pasos 9 a 11). */
    @PostMapping("/abierto/practicas/{practicaId}/tutor")
    @ResponseStatus(HttpStatus.CREATED)
    public DesignacionDto designarTutor(@PathVariable("practicaId") Long practicaId, @Valid @RequestBody TutorDto datos) {
        return servicio.designarTutor(practicaId, datos.tutorId());
    }

    /** PUT /api/semestres/abierto/practicas/{practicaId}/tutor: reasigna el tutor de una práctica (variación 9.1). */
    @PutMapping("/abierto/practicas/{practicaId}/tutor")
    public DesignacionDto reasignarTutor(@PathVariable("practicaId") Long practicaId, @Valid @RequestBody TutorDto datos) {
        return servicio.reasignarTutor(practicaId, datos.tutorId());
    }
}
