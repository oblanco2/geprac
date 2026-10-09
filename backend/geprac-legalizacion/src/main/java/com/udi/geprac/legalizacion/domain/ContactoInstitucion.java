package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;

/**
 * Contacto de una institución receptora: tabla contacto_institucion del
 * esquema legalizacion. El que escoge el estudiante figura como tutor del
 * escenario en el formato de solicitud y aprobación (PR-04).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "contacto_institucion")
public class ContactoInstitucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institucion_id", nullable = false)
    private Institucion institucion;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String cargo;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 20)
    private String celular;

    @Column(length = 150)
    private String correo;

    protected ContactoInstitucion() { }   // exigido por JPA

    public ContactoInstitucion(Institucion institucion) {
        this.institucion = institucion;
    }

    public Long getId()                  { return id; }
    public Institucion getInstitucion()  { return institucion; }
    public String getNombre()            { return nombre; }
    public String getCargo()             { return cargo; }
    public String getTelefono()          { return telefono; }
    public String getCelular()           { return celular; }
    public String getCorreo()            { return correo; }

    public void setNombre(String v)    { nombre = v; }
    public void setCargo(String v)     { cargo = v; }
    public void setTelefono(String v)  { telefono = v; }
    public void setCelular(String v)   { celular = v; }
    public void setCorreo(String v)    { correo = v; }
}
