package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;

/**
 * Copia de una entrada de las referencias personales del estudiante en la inscripción: tabla
 * inscripcion_referencia del esquema legalizacion. Se toma de la hoja de vida al crear la
 * inscripción y con ella se diligencia el formato PR-01.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "inscripcion_referencia")
public class InscripcionReferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String empresa;

    @Column(nullable = false, length = 100)
    private String cargo;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 80)
    private String ciudad;

    protected InscripcionReferencia() { }   // exigido por JPA

    public InscripcionReferencia(Inscripcion inscripcion, String nombre, String empresa, String cargo, String telefono, String ciudad) {
        this.inscripcion = inscripcion;
        this.nombre = nombre;
        this.empresa = empresa;
        this.cargo = cargo;
        this.telefono = telefono;
        this.ciudad = ciudad;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmpresa() { return empresa; }
    public String getCargo() { return cargo; }
    public String getTelefono() { return telefono; }
    public String getCiudad() { return ciudad; }
}
