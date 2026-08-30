package com.udi.geprac.academico.controller;

import com.udi.geprac.academico.domain.Programa;
import com.udi.geprac.academico.service.ProgramaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/programas")
public class ProgramaController {

    private final ProgramaService servicio;

    public ProgramaController(ProgramaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Programa> listar() {
        return servicio.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Programa crear(@Valid @RequestBody Programa programa) {
        return servicio.crear(programa);
    }
}