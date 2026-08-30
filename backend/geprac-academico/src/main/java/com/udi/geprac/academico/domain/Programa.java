package com.udi.geprac.academico.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "programa")
public class Programa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_programa")
    private Integer id;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;

    @Size(max = 50)
    @Column(length = 50)
    private String nivel;

    @NotNull(message = "Indica las horas de práctica")
    @Min(value = 1, message = "Debe ser mayor que cero")
    @Column(name = "horas_practica", nullable = false)
    private Integer horasPractica = 320;

    protected Programa() { }   // exigido por JPA

    public Integer getId()               { return id; }
    public String  getCodigo()           { return codigo; }
    public String  getNombre()           { return nombre; }
    public String  getNivel()            { return nivel; }
    public Integer getHorasPractica()    { return horasPractica; }

    public void setCodigo(String codigo)              { this.codigo = codigo; }
    public void setNombre(String nombre)              { this.nombre = nombre; }
    public void setNivel(String nivel)                { this.nivel = nivel; }
    public void setHorasPractica(Integer horas)       { this.horasPractica = horas; }
}