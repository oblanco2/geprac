package com.udi.geprac.academico.service;

import org.springframework.http.HttpStatus;

/**
 * Una regla del caso de uso que no se cumple en un campo concreto del
 * formulario: el documento que ya es de otro estudiante, el programa que no
 * existe, el año posterior al año en curso. El cliente web la recibe con el
 * nombre del campo, para señalarlo junto a él, como señala los que no pasan la
 * validación.
 *
 * @author Oscar Iván Blanco Díaz
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
