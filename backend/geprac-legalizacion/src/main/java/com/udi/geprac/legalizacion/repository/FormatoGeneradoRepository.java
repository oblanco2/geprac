package com.udi.geprac.legalizacion.repository;

import com.udi.geprac.legalizacion.domain.FormatoGenerado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla formato_generado del esquema legalizacion.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public interface FormatoGeneradoRepository extends JpaRepository<FormatoGenerado, Long> {

    /** Los formatos ya emitidos de una inscripción (CU-08, paso 3). */
    List<FormatoGenerado> findByInscripcionId(Long inscripcionId);
}
