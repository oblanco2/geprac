package com.udi.geprac.academico.repository;

import com.udi.geprac.academico.domain.Estudiante;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla estudiante del esquema identidad. Las entradas de la hoja
 * de vida se guardan con su estudiante, así que no tienen repositorio propio.
 *
 * @author Oscar Iván Blanco Díaz
 */
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    /** El registro de la cuenta que hace la petición (CU-01, paso 2). */
    Optional<Estudiante> findByUsuarioId(UUID usuarioId);

    /** Si el documento ya corresponde a un estudiante (excepción 7.2). */
    boolean existsByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);
}
