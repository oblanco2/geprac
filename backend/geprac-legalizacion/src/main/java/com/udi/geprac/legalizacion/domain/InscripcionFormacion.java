package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Copia de una entrada de la formación académica del estudiante en la inscripción: tabla
 * inscripcion_formacion del esquema legalizacion. Se toma de la hoja de vida al crear la
 * inscripción y con ella se diligencia el formato PR-01.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "inscripcion_formacion")
public class InscripcionFormacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @Column(nullable = false, length = 15)
    private String tipo;

    @Column(nullable = false, length = 150)
    private String institucion;

    @Column(nullable = false, length = 150)
    private String nombre;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Integer anio;

    protected InscripcionFormacion() { }   // exigido por JPA

    public InscripcionFormacion(Inscripcion inscripcion, String tipo, String institucion, String nombre, Integer anio) {
        this.inscripcion = inscripcion;
        this.tipo = tipo;
        this.institucion = institucion;
        this.nombre = nombre;
        this.anio = anio;
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getInstitucion() { return institucion; }
    public String getNombre() { return nombre; }
    public Integer getAnio() { return anio; }
}
