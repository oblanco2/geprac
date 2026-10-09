package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado de la revisión que el tutor académico hace de una inscripción
 * enviada: tabla revision del esquema legalizacion (CU-06). Una devolución
 * siempre lleva motivo; la base lo exige con una restricción de verificación.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "revision")
public class Revision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @Column(name = "revisor_id", nullable = false)
    private UUID revisorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ResultadoRevision resultado;

    @Column(columnDefinition = "text")
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Revision() { }   // exigido por JPA

    public Revision(Inscripcion inscripcion, UUID revisorId, ResultadoRevision resultado, String motivo) {
        this.inscripcion = inscripcion;
        this.revisorId = revisorId;
        this.resultado = resultado;
        this.motivo = motivo;
        this.fecha = LocalDateTime.now();
    }

    public Long getId()                     { return id; }
    public Inscripcion getInscripcion()     { return inscripcion; }
    public UUID getRevisorId()              { return revisorId; }
    public ResultadoRevision getResultado() { return resultado; }
    public String getMotivo()               { return motivo; }
    public LocalDateTime getFecha()         { return fecha; }
}
