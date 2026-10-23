package com.udi.geprac.legalizacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MS-02 Legalización de Prácticas.
 *
 * Mantiene lo transaccional del software —catálogo de prácticas,
 * instituciones, semestres, inscripciones, revisiones, avales y formatos
 * emitidos— en el esquema legalizacion de su propia base de datos.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@SpringBootApplication
public class GepracLegalizacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(GepracLegalizacionApplication.class, args);
	}

}
