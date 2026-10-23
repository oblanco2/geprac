package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Formato institucional emitido a partir de una inscripción avalada: tabla
 * formato_generado del esquema legalizacion (CU-08). Registra con qué
 * plantilla y en qué fecha se emitió; con esos dos datos y la copia de la
 * inscripción, que después del aval ya no cambia, el documento se entrega
 * siempre tal como se emitió. Hay uno por formato e inscripción: emitir de
 * nuevo lo reemplaza.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "formato_generado")
public class FormatoGenerado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @Column(nullable = false, length = 6)
    private TipoFormato tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plantilla_id", nullable = false)
    private PlantillaFormato plantilla;

    /** Nombre con que se entrega el documento. */
    @Column(nullable = false, length = 255)
    private String archivo;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    protected FormatoGenerado() { }   // exigido por JPA

    public FormatoGenerado(Inscripcion inscripcion, TipoFormato tipo) {
        this.inscripcion = inscripcion;
        this.tipo = tipo;
    }

    /** CU-08, pasos 5 a 7 y variación 4.1: la emisión con su plantilla y su fecha; si ya había una, la reemplaza. */
    public void emitir(PlantillaFormato plantilla, String archivo, LocalDateTime fecha) {
        this.plantilla = plantilla;
        this.archivo = archivo;
        this.fechaEmision = fecha;
    }

    public Long getId()                    { return id; }
    public Inscripcion getInscripcion()    { return inscripcion; }
    public TipoFormato getTipo()           { return tipo; }
    public PlantillaFormato getPlantilla() { return plantilla; }
    public String getArchivo()             { return archivo; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
}
