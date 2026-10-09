package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Aval que la Dirección del Programa da a una inscripción aprobada por el
 * tutor: tabla aval del esquema legalizacion (CU-07). Es lo que habilita la
 * emisión de los formatos. Una inscripción tiene a lo sumo un aval; el director
 * es una cuenta de MS-01 y se guarda como valor.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "aval")
public class Aval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id", nullable = false, unique = true)
    private Inscripcion inscripcion;

    @Column(name = "director_id", nullable = false)
    private UUID directorId;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Aval() { }   // exigido por JPA

    public Aval(Inscripcion inscripcion, UUID directorId) {
        this.inscripcion = inscripcion;
        this.directorId = directorId;
        this.fecha = LocalDateTime.now();
    }

    public Long getId()                 { return id; }
    public Inscripcion getInscripcion() { return inscripcion; }
    public UUID getDirectorId()         { return directorId; }
    public LocalDateTime getFecha()     { return fecha; }
}
