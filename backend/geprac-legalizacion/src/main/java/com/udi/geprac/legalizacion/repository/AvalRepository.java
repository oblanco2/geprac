package com.udi.geprac.legalizacion.repository;

import com.udi.geprac.legalizacion.domain.Aval;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla aval del esquema legalizacion (CU-07, paso 8).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public interface AvalRepository extends JpaRepository<Aval, Long> { }
