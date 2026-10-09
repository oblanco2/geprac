package com.udi.geprac.legalizacion.repository;

import com.udi.geprac.legalizacion.domain.Revision;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla revision del esquema legalizacion (CU-06, paso 8).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public interface RevisionRepository extends JpaRepository<Revision, Long> { }
