package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.dto.InstitucionDto;
import com.udi.geprac.legalizacion.service.InstitucionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * CU-03 · Gestionar instituciones receptoras. Solo el director mantiene el
 * catálogo, del que el estudiante escoge la institución al inscribir su
 * práctica (CU-05): por eso el estudiante también lo consulta, con solo las
 * instituciones activas.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@RestController
@RequestMapping("/instituciones")
public class InstitucionController {

    private final InstitucionService servicio;

    public InstitucionController(InstitucionService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/instituciones: el catálogo con sus contactos (P-14); al estudiante, las activas (P-05 y P-06). */
    @GetMapping
    public List<InstitucionDto> listarInstituciones() {
        return servicio.listarInstituciones();
    }

    /** POST /api/instituciones: una institución nueva con sus contactos (P-15). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstitucionDto crearInstitucion(@Valid @RequestBody InstitucionDto datos) {
        return servicio.crearInstitucion(datos);
    }

    /** PUT /api/instituciones/{id} */
    @PutMapping("/{id}")
    public InstitucionDto actualizarInstitucion(@PathVariable("id") Long id, @Valid @RequestBody InstitucionDto datos) {
        return servicio.actualizarInstitucion(id, datos);
    }

    /** DELETE /api/instituciones/{id} */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarInstitucion(@PathVariable("id") Long id) {
        servicio.eliminarInstitucion(id);
    }
}
