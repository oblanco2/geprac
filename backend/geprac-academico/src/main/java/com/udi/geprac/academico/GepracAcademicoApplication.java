package com.udi.geprac.academico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque de MS-01 Identidad y Perfil Académico.
 *
 * Al iniciar, Flyway monta el esquema identidad con las migraciones de
 * db/migration y Hibernate comprueba que cada entidad coincida con su tabla
 * antes de atender peticiones.
 *
 * @author Oscar Iván Blanco Díaz
 */
@SpringBootApplication
public class GepracAcademicoApplication {

	public static void main(String[] args) {
		SpringApplication.run(GepracAcademicoApplication.class, args);
	}

}
