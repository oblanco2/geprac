package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Institución receptora donde el estudiante desarrolla la práctica: tabla
 * institucion del esquema legalizacion (CU-03). Sus contactos le pertenecen y
 * se guardan con ella.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "institucion")
public class Institucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(length = 20)
    private String nit;

    @Column(nullable = false, length = 150)
    private String direccion;

    @Column(nullable = false, length = 80)
    private String ciudad;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Column(name = "sitio_web", length = 150)
    private String sitioWeb;

    @Column(name = "representante_legal", length = 150)
    private String representanteLegal;

    @Column(nullable = false)
    private Boolean activa;

    @OneToMany(mappedBy = "institucion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ContactoInstitucion> contactos = new ArrayList<>();

    public Institucion() { }

    public Long getId()                  { return id; }
    public String getRazonSocial()       { return razonSocial; }
    public String getNit()               { return nit; }
    public String getDireccion()         { return direccion; }
    public String getCiudad()            { return ciudad; }
    public String getTelefono()          { return telefono; }
    public String getCorreo()            { return correo; }
    public String getSitioWeb()          { return sitioWeb; }
    public String getRepresentanteLegal(){ return representanteLegal; }
    public Boolean getActiva()           { return activa; }
    public List<ContactoInstitucion> getContactos() { return contactos; }

    public void setRazonSocial(String v)        { razonSocial = v; }
    public void setNit(String v)                { nit = v; }
    public void setDireccion(String v)          { direccion = v; }
    public void setCiudad(String v)             { ciudad = v; }
    public void setTelefono(String v)           { telefono = v; }
    public void setCorreo(String v)             { correo = v; }
    public void setSitioWeb(String v)           { sitioWeb = v; }
    public void setRepresentanteLegal(String v) { representanteLegal = v; }
    public void setActiva(Boolean v)            { activa = v; }
}
