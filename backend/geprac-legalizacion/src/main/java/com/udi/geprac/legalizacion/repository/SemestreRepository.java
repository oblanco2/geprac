package com.udi.geprac.legalizacion.repository;

import com.udi.geprac.legalizacion.domain.Semestre;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla semestre del esquema legalizacion. El índice único parcial
 * del esquema garantiza que solo haya uno abierto.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public interface SemestreRepository extends JpaRepository<Semestre, Long> {

    /** El semestre abierto, si hay (CU-04, pasos 7 y 9). */
    Optional<Semestre> findByAbiertoTrue();

    /** El último semestre, el que presenta la preparación cuando ninguno está abierto (CU-04, paso 2). */
    Optional<Semestre> findFirstByOrderByFechaInicioDesc();

    /** Si el código ya es de otro semestre (CU-04, excepción 7.2). */
    boolean existsByCodigo(String codigo);
}
