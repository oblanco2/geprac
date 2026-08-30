package com.udi.geprac.academico;

import org.junit.jupiter.api.Test;

/**
 * La prueba de contexto se sustituye por esta comprobación mínima.
 *
 * Arrancar el contexto completo exige conexión a la base de datos, que
 * durante la construcción no está disponible: la contraseña vive en
 * application-local.yml en desarrollo y en variables de entorno en Render.
 *
 * Las pruebas de integración reales irán con Testcontainers más adelante.
 */
class GepracAcademicoApplicationTests {

    @Test
    void laClasePrincipalExiste() {
        // Verifica que la clase de arranque está donde debe
        org.junit.jupiter.api.Assertions.assertNotNull(
            GepracAcademicoApplication.class);
    }
}