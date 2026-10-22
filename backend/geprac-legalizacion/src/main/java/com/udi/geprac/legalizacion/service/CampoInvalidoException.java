package com.udi.geprac.legalizacion.service;

import org.springframework.http.HttpStatus;

/**
 * Una regla del caso de uso que no se cumple en un campo concreto del
 * formulario: la posición del plan que ya ocupa otra práctica, el código de un
 * semestre que ya existe, el usuario que no tiene el rol de tutor. El cliente
 * web la recibe con el nombre del campo, para señalarlo junto a él, como señala
 * los que no pasan la validación. Es la misma que usa MS-01.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public class CampoInvalidoException extends RuntimeException {

    private final String campo;
    private final HttpStatus estado;

    /** La regla no se cumple: 400. */
    public CampoInvalidoException(String campo, String mensaje) {
        this(campo, mensaje, HttpStatus.BAD_REQUEST);
    }

    /** Con el estado que corresponde; por ejemplo, 409 cuando el dato ya está registrado. */
    public CampoInvalidoException(String campo, String mensaje, HttpStatus estado) {
        super(mensaje);
        this.campo = campo;
        this.estado = estado;
    }

    public String getCampo()      { return campo; }
    public HttpStatus getEstado() { return estado; }
}
