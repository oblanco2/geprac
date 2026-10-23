package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Periodo académico en el que se inscriben las prácticas: tabla semestre del
 * esquema legalizacion. Solo uno puede estar abierto a la vez (CU-04).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "semestre")
public class Semestre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDate fechaCierre;

    @Column(nullable = false)
    private Boolean abierto;

    protected Semestre() { }   // exigido por JPA

    /** CU-04, paso 8: el semestre nace abierto. */
    public Semestre(String codigo, LocalDate fechaInicio, LocalDate fechaCierre) {
        this.codigo = codigo;
        this.fechaInicio = fechaInicio;
        this.fechaCierre = fechaCierre;
        this.abierto = true;
    }

    /** CU-04, variación 2.2: mientras no se abra otro, ningún estudiante puede inscribir. */
    public void cerrar() {
        this.abierto = false;
    }

    public Long getId()               { return id; }
    public String getCodigo()         { return codigo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaCierre() { return fechaCierre; }
    public Boolean getAbierto()       { return abierto; }
}
