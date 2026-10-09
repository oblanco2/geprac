package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Copia de un objetivo específico de la práctica en la inscripción: tabla inscripcion_objetivo
 * del esquema legalizacion. Se toma del catálogo al crear la inscripción, de
 * modo que editar el catálogo después no cambia lo que ya se inscribió.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "inscripcion_objetivo")
public class InscripcionObjetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(nullable = false)
    private Integer posicion;

    @Column(nullable = false, columnDefinition = "text")
    private String texto;

    protected InscripcionObjetivo() { }   // exigido por JPA

    public InscripcionObjetivo(Inscripcion inscripcion, int posicion, String texto) {
        this.inscripcion = inscripcion;
        this.posicion = posicion;
        this.texto = texto;
    }

    public Long getId()                  { return id; }
    public Inscripcion getInscripcion()  { return inscripcion; }
    public Integer getPosicion()         { return posicion; }
    public String getTexto()             { return texto; }
}
