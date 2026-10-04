package com.udi.geprac.academico.repository;

import com.udi.geprac.academico.domain.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Acceso a la tabla programa del esquema identidad. La llave es el código
 * institucional del programa.
 *
 * @author Oscar Iván Blanco Díaz
 */
public interface ProgramaRepository extends JpaRepository<Programa, String> {

    Optional<Programa> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);
}
