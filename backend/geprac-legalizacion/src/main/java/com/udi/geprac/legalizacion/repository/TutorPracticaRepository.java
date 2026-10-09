package com.udi.geprac.legalizacion.repository;

import com.udi.geprac.legalizacion.domain.TutorPractica;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla tutor_practica del esquema legalizacion: las designaciones
 * que deciden qué inscripciones revisa cada tutor.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public interface TutorPracticaRepository extends JpaRepository<TutorPractica, Long> {

    /** Las prácticas que el tutor tiene designadas en el semestre abierto (CU-06, pasos 2 y 3). */
    List<TutorPractica> findByTutorIdAndSemestreAbiertoTrue(UUID tutorId);
}
