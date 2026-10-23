package com.udi.geprac.academico.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Programa académico de licenciatura: tabla programa del esquema identidad.
 *
 * Es un dato de referencia. Su llave es el código institucional, que es
 * estable, y MS-02 nombra el programa por ese mismo código.
 *
 * @author Oscar Iván Blanco Díaz
 */
@Entity
@Table(name = "programa")
public class Programa {

    @Id
    @NotBlank(message = "El código es obligatorio")
    @Size(max = 10)
    @Column(length = 10)
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;

    protected Programa() { }   // exigido por JPA

    public String getCodigo()  { return codigo; }
    public String getNombre()  { return nombre; }

    public void setCodigo(String codigo)  { this.codigo = codigo; }
    public void setNombre(String nombre)  { this.nombre = nombre; }
}
