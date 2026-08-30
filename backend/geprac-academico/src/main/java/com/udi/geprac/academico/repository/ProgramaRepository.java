package com.udi.geprac.academico.repository;

import com.udi.geprac.academico.domain.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProgramaRepository extends JpaRepository<Programa, Integer> {

    Optional<Programa> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);
}