package com.udi.geprac.academico.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entrada de formación académica de la hoja de vida: tabla formacion_academica
 * del esquema identidad. El tipo es uno de los tres que admite el formato
 * PR-01 —Secundaria, Universitaria u Otros— y el año queda vacío mientras la
 * formación está en curso.
 *
 * @author Oscar Iván Blanco Díaz
 */
@Entity
@Table(name = "formacion_academica")
public class FormacionAcademica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false, length = 15)
    private String tipo;

    @Column(nullable = false, length = 150)
    private String institucion;

    @Column(nullable = false, length = 150)
    private String nombre;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Integer anio;

    protected FormacionAcademica() { }   // exigido por JPA

    public FormacionAcademica(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Long getId()               { return id; }
    public Estudiante getEstudiante() { return estudiante; }
    public String getTipo()           { return tipo; }
    public String getInstitucion()    { return institucion; }
    public String getNombre()         { return nombre; }
    public Integer getAnio()          { return anio; }

    public void setTipo(String v)        { this.tipo = v; }
    public void setInstitucion(String v) { this.institucion = v; }
    public void setNombre(String v)      { this.nombre = v; }
    public void setAnio(Integer v)       { this.anio = v; }
}
