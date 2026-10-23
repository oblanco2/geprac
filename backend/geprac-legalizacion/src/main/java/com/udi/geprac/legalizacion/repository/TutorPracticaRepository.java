package com.udi.geprac.legalizacion.repository;

import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.domain.Semestre;
import com.udi.geprac.legalizacion.domain.TutorPractica;
import java.util.List;
import java.util.Optional;
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

    /** Las designaciones de un semestre: la tabla de tutores de la preparación (CU-04, paso 2). */
    List<TutorPractica> findBySemestre(Semestre semestre);

    /** Si la práctica ya tiene tutor en el semestre (CU-04, paso 10 y excepción 10.2). */
    boolean existsBySemestreAndPracticaId(Semestre semestre, Long practicaId);

    /** La designación de una práctica en el semestre, para reasignarla (CU-04, variación 9.1). */
    Optional<TutorPractica> findBySemestreAndPracticaId(Semestre semestre, Long practicaId);

    /** Las designaciones de una práctica que se elimina del catálogo (CU-02, variación 2.2). */
    void deleteByPractica(Practica practica);
}
