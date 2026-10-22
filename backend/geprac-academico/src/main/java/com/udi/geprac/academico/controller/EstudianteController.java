package com.udi.geprac.academico.controller;

import com.udi.geprac.academico.dto.BloquesDto;
import com.udi.geprac.academico.dto.EntradaDto;
import com.udi.geprac.academico.dto.EstudianteDto;
import com.udi.geprac.academico.dto.HojaVidaDto;
import com.udi.geprac.academico.service.EstudianteService;
import com.udi.geprac.academico.service.EstudianteService.Bloque;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * CU-01 · Mantener perfil del estudiante. Solo el estudiante opera su propio
 * registro: la ruta dice «me» porque el registro sale de la cuenta del token,
 * y ningún dato de la petición lo cambia por otro. MS-02 consulta estas mismas
 * rutas, con el token del estudiante, para copiar su expediente en la
 * inscripción (CU-05).
 *
 * @author Oscar Iván Blanco Díaz
 */
@RestController
@RequestMapping("/estudiantes/me")
public class EstudianteController {

    private final EstudianteService servicio;

    public EstudianteController(EstudianteService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/estudiantes/me: los datos personales con el indicador, o el formulario vacío (P-03). */
    @GetMapping
    public EstudianteDto consultarPerfil() {
        return servicio.consultarPerfil();
    }

    /** PUT /api/estudiantes/me: crea o actualiza los datos personales (P-03). */
    @PutMapping
    public EstudianteDto guardarPerfil(@Valid @RequestBody EstudianteDto datos) {
        return servicio.guardarPerfil(datos);
    }

    /** GET /api/estudiantes/me/hoja-vida: los cinco bloques de la hoja de vida (P-04). */
    @GetMapping("/hoja-vida")
    public HojaVidaDto consultarHojaVida() {
        return servicio.consultarHojaVida();
    }

    /** PUT /api/estudiantes/me/hoja-vida: el perfil profesional y las herramientas de trabajo. */
    @PutMapping("/hoja-vida")
    public HojaVidaDto guardarBloques(@Valid @RequestBody BloquesDto bloques) {
        return servicio.guardarBloques(bloques);
    }

    /** POST /api/estudiantes/me/formaciones */
    @PostMapping("/formaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public EntradaDto.Formacion crearFormacion(@Valid @RequestBody EntradaDto.Formacion entrada) {
        return servicio.crearFormacion(entrada);
    }

    /** POST /api/estudiantes/me/experiencias */
    @PostMapping("/experiencias")
    @ResponseStatus(HttpStatus.CREATED)
    public EntradaDto.Experiencia crearExperiencia(@Valid @RequestBody EntradaDto.Experiencia entrada) {
        return servicio.crearExperiencia(entrada);
    }

    /** POST /api/estudiantes/me/referencias */
    @PostMapping("/referencias")
    @ResponseStatus(HttpStatus.CREATED)
    public EntradaDto.Referencia crearReferencia(@Valid @RequestBody EntradaDto.Referencia entrada) {
        return servicio.crearReferencia(entrada);
    }

    /** PUT /api/estudiantes/me/formaciones/{id} */
    @PutMapping("/formaciones/{id}")
    public EntradaDto actualizarFormacion(@PathVariable("id") Long id, @Valid @RequestBody EntradaDto.Formacion entrada) {
        return servicio.actualizarEntrada(Bloque.FORMACIONES, id, entrada);
    }

    /** PUT /api/estudiantes/me/experiencias/{id} */
    @PutMapping("/experiencias/{id}")
    public EntradaDto actualizarExperiencia(@PathVariable("id") Long id, @Valid @RequestBody EntradaDto.Experiencia entrada) {
        return servicio.actualizarEntrada(Bloque.EXPERIENCIAS, id, entrada);
    }

    /** PUT /api/estudiantes/me/referencias/{id} */
    @PutMapping("/referencias/{id}")
    public EntradaDto actualizarReferencia(@PathVariable("id") Long id, @Valid @RequestBody EntradaDto.Referencia entrada) {
        return servicio.actualizarEntrada(Bloque.REFERENCIAS, id, entrada);
    }

    /** DELETE /api/estudiantes/me/{bloque}/{id}: formaciones, experiencias o referencias. */
    @DeleteMapping("/{bloque}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarEntrada(@PathVariable("bloque") String bloque, @PathVariable("id") Long id) {
        servicio.eliminarEntrada(Bloque.de(bloque), id);
    }
}
