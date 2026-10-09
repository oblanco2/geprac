package com.udi.geprac.legalizacion.repository;

import com.udi.geprac.legalizacion.domain.Institucion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla institucion del esquema legalizacion y, por composición,
 * a sus contactos.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public interface InstitucionRepository extends JpaRepository<Institucion, Long> {

    /** El catálogo de instituciones, por razón social (CU-03, paso 2). */
    List<Institucion> findAllByOrderByRazonSocial();

    /** Si ya existe la institución en esa ciudad (CU-03, paso 9). */
    boolean existsByRazonSocialAndCiudad(String razonSocial, String ciudad);
}
