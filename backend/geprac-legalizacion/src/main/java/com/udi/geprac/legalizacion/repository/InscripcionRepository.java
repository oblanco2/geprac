package com.udi.geprac.legalizacion.repository;

import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.domain.Semestre;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla inscripcion del esquema legalizacion y, por composición, a
 * su copia de datos y a sus listas copiadas.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    /** Las inscripciones de unas prácticas en un semestre y un estado: la bandeja del tutor (CU-06, paso 2). */
    List<Inscripcion> findBySemestreAndPracticaInAndEstado(Semestre semestre, Collection<Practica> practicas,
                                                           EstadoInscripcion estado);
}
