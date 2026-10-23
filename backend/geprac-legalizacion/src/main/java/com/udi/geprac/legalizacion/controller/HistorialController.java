package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.dto.HistorialDto;
import com.udi.geprac.legalizacion.service.HistorialService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CU-09 · Consultar el historial de prácticas. Los tres roles, cada uno con el
 * alcance que le corresponde.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@RestController
@RequestMapping("/historial")
public class HistorialController {

    private final HistorialService servicio;

    public HistorialController(HistorialService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/historial: Mis prácticas (P-02), el historial del tutor o el del programa (P-19). */
    @GetMapping
    public List<HistorialDto> consultarHistorial() {
        return servicio.consultarHistorial();
    }
}
