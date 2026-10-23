package com.udi.geprac.legalizacion.repository;

import com.udi.geprac.legalizacion.domain.Practica;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla practica del esquema legalizacion.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public interface PracticaRepository extends JpaRepository<Practica, Long> {

    /** Las prácticas del plan de un programa, en el orden del plan (CU-02, paso 2; CU-09, paso 4). */
    List<Practica> findByCodigoProgramaOrderByOrden(String codigoPrograma);

    /** La práctica que ocupa una posición del plan de un programa (CU-02, paso 10). */
    Optional<Practica> findByCodigoProgramaAndOrden(String codigoPrograma, Integer orden);
}
