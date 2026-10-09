package com.udi.geprac.legalizacion.repository;

import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.domain.Semestre;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a la tabla inscripcion del esquema legalizacion y, por composición, a
 * su copia de datos y a sus listas copiadas.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    /** Las inscripciones de un estudiante, en todos los semestres (CU-09, paso 3). */
    List<Inscripcion> findByEstudianteId(Long estudianteId);

    /** Las inscripciones de unas prácticas en un semestre y un estado: la bandeja del tutor (CU-06, paso 2). */
    List<Inscripcion> findBySemestreAndPracticaInAndEstado(Semestre semestre, Collection<Practica> practicas,
                                                           EstadoInscripcion estado);

    /** Las inscripciones de un programa en un estado: la bandeja de aval de la Dirección (CU-07, paso 2). */
    List<Inscripcion> findByEstadoAndPracticaCodigoPrograma(EstadoInscripcion estado, String codigoPrograma);

    /** Todas las inscripciones de un programa, en todos los semestres: el historial de la Dirección (CU-09, 2.2). */
    List<Inscripcion> findByPracticaCodigoPrograma(String codigoPrograma);

    /**
     * Las inscripciones de las prácticas que el tutor tuvo designadas, en los
     * semestres en que tuvo la designación: el historial del tutor (CU-09, 2.1).
     */
    @Query("""
        select i from Inscripcion i
         where exists (select d from TutorPractica d
                        where d.tutorId = :tutorId and d.practica = i.practica and d.semestre = i.semestre)""")
    List<Inscripcion> findDesignadasAlTutor(@Param("tutorId") UUID tutorId);
}
