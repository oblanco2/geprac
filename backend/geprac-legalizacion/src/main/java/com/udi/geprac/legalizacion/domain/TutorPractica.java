package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Designación del tutor académico responsable de una práctica en un semestre:
 * tabla tutor_practica del esquema legalizacion (CU-04). Es la que decide qué
 * inscripciones llegan a la bandeja de cada tutor (CU-06). El tutor es una
 * cuenta de MS-01 y se guarda como valor.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "tutor_practica")
public class TutorPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semestre_id", nullable = false)
    private Semestre semestre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practica_id", nullable = false)
    private Practica practica;

    @Column(name = "tutor_id", nullable = false)
    private UUID tutorId;

    protected TutorPractica() { }   // exigido por JPA

    /** CU-04, paso 11: el tutor de una práctica en el semestre. */
    public TutorPractica(Semestre semestre, Practica practica, UUID tutorId) {
        this.semestre = semestre;
        this.practica = practica;
        this.tutorId = tutorId;
    }

    /** CU-04, variación 9.1: las inscripciones ya enviadas pasan a la bandeja del nuevo tutor. */
    public void reasignar(UUID tutorId) {
        this.tutorId = tutorId;
    }

    public Long getId()           { return id; }
    public Semestre getSemestre() { return semestre; }
    public Practica getPractica() { return practica; }
    public UUID getTutorId()      { return tutorId; }
}
