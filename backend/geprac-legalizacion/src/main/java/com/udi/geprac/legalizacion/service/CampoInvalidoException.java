package com.udi.geprac.legalizacion.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Una regla del caso de uso que no se cumple en un campo concreto del
 * formulario: la posición del plan que ya ocupa otra práctica, el código de un
 * semestre que ya existe, el usuario que no tiene el rol de tutor. El cliente
 * web la recibe con el nombre del campo, para señalarlo junto a él, como señala
 * los que no pasan la validación. Es la misma que usa MS-01; aquí admite
 * además varios campos a la vez, como el segundo paso de la inscripción, que
 * señala juntos el contacto y las fechas que faltan (CU-05, excepción 9.1).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public class CampoInvalidoException extends RuntimeException {

    private final Map<String, String> campos;
    private final HttpStatus estado;

    /** La regla no se cumple: 400. */
    public CampoInvalidoException(String campo, String mensaje) {
        this(campo, mensaje, HttpStatus.BAD_REQUEST);
    }

    /** Con el estado que corresponde; por ejemplo, 409 cuando el dato ya está registrado. */
    public CampoInvalidoException(String campo, String mensaje, HttpStatus estado) {
        this(Map.of(campo, mensaje), mensaje, estado);
    }

    /** Varias reglas que no se cumplen, cada una con su mensaje en su campo: 400, con un mensaje general. */
    public CampoInvalidoException(Map<String, String> campos, String mensaje) {
        this(campos, mensaje, HttpStatus.BAD_REQUEST);
    }

    private CampoInvalidoException(Map<String, String> campos, String mensaje, HttpStatus estado) {
        super(mensaje);
        this.campos = Collections.unmodifiableMap(new LinkedHashMap<>(campos));
        this.estado = estado;
    }

    /** El primer campo señalado. */
    public String getCampo()                { return campos.keySet().iterator().next(); }
    public Map<String, String> getCampos()  { return campos; }
    public HttpStatus getEstado()           { return estado; }
}
