package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Actividad de una práctica, en el orden en que sale impreso en el formato:
 * tabla actividad_practica del esquema legalizacion. Pertenece a la práctica y no
 * existe sin ella.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "actividad_practica")
public class ActividadPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practica_id", nullable = false)
    private Practica practica;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(nullable = false)
    private Integer posicion;

    @Column(nullable = false, columnDefinition = "text")
    private String texto;

    protected ActividadPractica() { }   // exigido por JPA

    public ActividadPractica(Practica practica, int posicion, String texto) {
        this.practica = practica;
        this.posicion = posicion;
        this.texto = texto;
    }

    public Long getId()            { return id; }
    public Practica getPractica()  { return practica; }
    public Integer getPosicion()   { return posicion; }
    public String getTexto()       { return texto; }
}
