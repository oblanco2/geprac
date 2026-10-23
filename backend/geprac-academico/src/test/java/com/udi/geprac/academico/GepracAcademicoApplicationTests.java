package com.udi.geprac.academico;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Comprobación mínima de la clase de arranque.
 *
 * No levanta el contexto completo, porque eso exige conexión a la base de
 * datos: la contraseña vive en application-local.yml en desarrollo y en las
 * variables de entorno de Render. Las pruebas del controlador y de la
 * seguridad están en sus propios paquetes y no necesitan base.
 *
 * @author Oscar Iván Blanco Díaz
 */
class GepracAcademicoApplicationTests {

	@Test
	void laClasePrincipalExiste() {
		assertNotNull(GepracAcademicoApplication.class);
	}

}
