package com.udi.geprac.academico.domain;

import jakarta.persistence.*;

/**
 * Referencia personal de la hoja de vida: tabla referencia_personal del
 * esquema identidad. El formato PR-01 recomienda un máximo de tres, y ese tope
 * lo aplica el servicio, no la base de datos.
 *
 * @author Oscar Iván Blanco Díaz
 */
@Entity
@Table(name = "referencia_personal")
public class ReferenciaPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    /** Con su tratamiento: Lic., Mg., Dr. */
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

    protected ReferenciaPersonal() { }   // exigido por JPA

    public ReferenciaPersonal(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Long getId()               { return id; }
    public Estudiante getEstudiante() { return estudiante; }
    public String getNombre()         { return nombre; }
    public String getEmpresa()        { return empresa; }
    public String getCargo()          { return cargo; }
    public String getTelefono()       { return telefono; }
    public String getCiudad()         { return ciudad; }

    public void setNombre(String v)   { this.nombre = v; }
    public void setEmpresa(String v)  { this.empresa = v; }
    public void setCargo(String v)    { this.cargo = v; }
    public void setTelefono(String v) { this.telefono = v; }
    public void setCiudad(String v)   { this.ciudad = v; }
}
