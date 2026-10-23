package com.udi.geprac.legalizacion.repository;

import com.udi.geprac.legalizacion.domain.PlantillaFormato;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla plantilla_formato del esquema legalizacion.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public interface PlantillaFormatoRepository extends JpaRepository<PlantillaFormato, Long> {

    /** La plantilla vigente de cada formato (CU-08, paso 5); la base admite una sola por formato. */
    List<PlantillaFormato> findByVigenteTrue();
}
