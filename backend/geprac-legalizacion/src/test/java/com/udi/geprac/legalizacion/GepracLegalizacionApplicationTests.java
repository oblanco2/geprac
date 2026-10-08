package com.udi.geprac.legalizacion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Comprobación mínima de la clase de arranque.
 *
 * No levanta el contexto completo, porque eso exige conexión a la base de
 * datos, que en desarrollo se configura en application-local.yml y en Render
 * en variables de entorno. La prueba de la seguridad está en su propio
 * paquete y no necesita base.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class GepracLegalizacionApplicationTests {

	@Test
	void laClasePrincipalExiste() {
		assertNotNull(GepracLegalizacionApplication.class);
	}

}
