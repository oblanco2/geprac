package com.udi.geprac.academico.repository;

import com.udi.geprac.academico.domain.Usuario;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla usuario del esquema identidad. La llave es el
 * identificador que asigna Supabase Auth.
 *
 * @author Oscar Iván Blanco Díaz
 */
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /**
     * Las cuentas por nombre: la lista de roles de CU-04 y, en la bandeja de
     * aval (CU-07), el nombre del tutor que aprobó cada inscripción.
     */
    List<Usuario> findAllByOrderByNombrePresentacion();
}
