package com.udi.geprac.academico.controller;

import com.udi.geprac.academico.dto.ProgramaDto;
import com.udi.geprac.academico.service.ProgramaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Programas académicos: el dato de referencia que el estudiante escoge en su
 * perfil (CU-01). Solo se consultan; los registra la migración V2 y ningún
 * caso de uso los crea ni los modifica.
 *
 * @author Oscar Iván Blanco Díaz
 */
@RestController
@RequestMapping("/programas")
public class ProgramaController {

    private final ProgramaService servicio;

    public ProgramaController(ProgramaService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/programas: los programas, ordenados por código. */
    @GetMapping
    public List<ProgramaDto> listar() {
        return servicio.listar();
    }
}
