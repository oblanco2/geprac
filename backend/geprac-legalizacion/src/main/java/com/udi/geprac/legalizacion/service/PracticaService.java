package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.dto.PracticaDto;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import com.udi.geprac.legalizacion.repository.PracticaRepository;
import com.udi.geprac.legalizacion.repository.TutorPracticaRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-02 · Gestionar el catálogo de prácticas. Cada director mantiene el
 * catálogo de su programa, el del token, y ningún dato de la petición lo
 * amplía. No puede haber dos prácticas en la misma posición del plan, y una
 * práctica se guarda con sus dos listas o no se guarda (excepción 11.1).
 * Editar el catálogo no altera las inscripciones ya creadas: cada una guarda
 * su propia copia del contenido.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Service
@Transactional
public class PracticaService {

    private static final String NO_ESTA = "La práctica ya no está en el catálogo.";

    private final PracticaRepository practicas;
    private final InscripcionRepository inscripciones;
    private final TutorPracticaRepository designaciones;

    public PracticaService(PracticaRepository practicas, InscripcionRepository inscripciones,
                           TutorPracticaRepository designaciones) {
        this.practicas = practicas;
        this.inscripciones = inscripciones;
        this.designaciones = designaciones;
    }

    /** Paso 2: las prácticas del programa, ordenadas por su posición en el plan. */
    @Transactional(readOnly = true)
    public List<PracticaDto> listarPracticas() {
        return practicas.findByCodigoProgramaOrderByOrden(UsuarioActual.programa()).stream().map(PracticaDto::de).toList();
    }

    /** Pasos 8 a 11: comprueba la posición y guarda la práctica con sus dos listas. */
    public PracticaDto crearPractica(PracticaDto datos) {
        String programa = UsuarioActual.programa();
        posicionLibre(programa, datos.orden(), null);
        Practica p = new Practica(programa);
        copiar(datos, p);
        return PracticaDto.de(practicas.save(p));
    }

    /** Variaciones 8.1 y 8.2: aplica las comprobaciones de los pasos 9 y 10 y reubica la práctica si cambió su posición. */
    public PracticaDto actualizarPractica(Long id, PracticaDto datos) {
        Practica p = delPrograma(id);
        posicionLibre(p.getCodigoPrograma(), datos.orden(), p.getId());
        copiar(datos, p);
        Practica guardada = practicas.save(p);
        practicas.flush();
        return PracticaDto.de(guardada);
    }

    /**
     * Variación 2.2: elimina una práctica sin inscripciones, con sus dos listas
     * y sus designaciones de tutor, que sin ella no tienen objeto.
     */
    public void eliminarPractica(Long id) {
        Practica p = delPrograma(id);
        long n = inscripciones.countByPractica(p);
        if (n > 0)   // excepción 2.2.4.1
            throw new IllegalStateException("La práctica «" + titulo(p) + "» tiene " + n
                + (n == 1 ? " inscripción asociada" : " inscripciones asociadas") + ": no se eliminó. Puede editarla, "
                + "o marcarla como inactiva para retirarla del catálogo.");
        designaciones.deleteByPractica(p);
        practicas.delete(p);
        practicas.flush();
    }

    // ------------------------------------------------------------------ apoyo

    /** La práctica, solo si es del programa del director; la de otro programa no existe para él. */
    private Practica delPrograma(Long id) {
        String programa = UsuarioActual.programa();
        return practicas.findById(id).filter(p -> p.getCodigoPrograma().equals(programa))
            .orElseThrow(() -> new NoSuchElementException(NO_ESTA));
    }

    /** Paso 10 y excepción 10.1: ninguna otra práctica del programa ocupa esa posición del plan. */
    private void posicionLibre(String programa, Integer orden, Long propia) {
        practicas.findByCodigoProgramaAndOrden(programa, orden)
            .filter(otra -> !otra.getId().equals(propia))
            .ifPresent(otra -> {
                throw new CampoInvalidoException("orden",
                    "Ya hay una práctica en esa posición del plan: " + titulo(otra) + ".", HttpStatus.CONFLICT);
            });
    }

    private static void copiar(PracticaDto d, Practica p) {
        p.actualizar(d.orden(), d.nombre().trim(), d.objetivoGeneral().trim(), texto(d.horarioEstandar()), d.activa(),
            d.objetivos().stream().map(String::trim).toList(), d.actividades().stream().map(String::trim).toList());
    }

    private static String titulo(Practica p) {
        return p.getOrden() + " · " + p.getNombre();
    }

    private static String texto(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
