package com.udi.geprac.academico.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Registro único y persistente del estudiante practicante: tabla estudiante
 * del esquema identidad (CU-01).
 *
 * Guarda los datos personales, el programa y el semestre que cursa, y los dos
 * bloques narrativos de la hoja de vida. Las entradas de formación académica,
 * de experiencia laboral y de referencias personales le pertenecen y se
 * guardan con él. De este registro salen los datos que MS-02 copia en cada
 * inscripción (CU-05).
 *
 * @author Oscar Iván Blanco Díaz
 */
@Entity
@Table(name = "estudiante")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** La cuenta con la que ingresa; una cuenta tiene a lo sumo un registro de estudiante. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(name = "tipo_documento", nullable = false, length = 4)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(name = "lugar_expedicion", length = 80)
    private String lugarExpedicion;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "lugar_nacimiento", length = 80)
    private String lugarNacimiento;

    @Column(length = 10)
    private String genero;

    @Column(name = "estado_civil", length = 25)
    private String estadoCivil;

    @Column(length = 80)
    private String eps;

    @Column(length = 150)
    private String direccion;

    @Column(length = 80)
    private String barrio;

    @Column(length = 80)
    private String ciudad;

    @Column(name = "telefono_fijo", length = 20)
    private String telefonoFijo;

    @Column(length = 20)
    private String celular;

    @Column(name = "correo_personal", length = 150)
    private String correoPersonal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_programa", nullable = false)
    private Programa programa;

    /** Semestre que cursa, de 1 a 8: decide qué práctica le corresponde inscribir. */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "semestre_actual", nullable = false)
    private Integer semestreActual;

    @Column(name = "perfil_profesional", columnDefinition = "text")
    private String perfilProfesional;

    @Column(name = "herramientas_trabajo", columnDefinition = "text")
    private String herramientasTrabajo;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormacionAcademica> formaciones = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExperienciaLaboral> experiencias = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ReferenciaPersonal> referencias = new ArrayList<>();

    protected Estudiante() { }   // exigido por JPA

    /** El registro nuevo de una cuenta (CU-01, paso 8). */
    public Estudiante(Usuario usuario) {
        this.usuario = usuario;
        this.creadoEn = LocalDateTime.now();
    }

    /** Marca la fecha de la última modificación (variación 6.1). */
    public void tocar() {
        this.actualizadoEn = LocalDateTime.now();
    }

    public Long getId()                        { return id; }
    public Usuario getUsuario()                { return usuario; }
    public String getNombres()                 { return nombres; }
    public String getApellidos()               { return apellidos; }
    public String getTipoDocumento()           { return tipoDocumento; }
    public String getNumeroDocumento()         { return numeroDocumento; }
    public String getLugarExpedicion()         { return lugarExpedicion; }
    public LocalDate getFechaNacimiento()      { return fechaNacimiento; }
    public String getLugarNacimiento()         { return lugarNacimiento; }
    public String getGenero()                  { return genero; }
    public String getEstadoCivil()             { return estadoCivil; }
    public String getEps()                     { return eps; }
    public String getDireccion()               { return direccion; }
    public String getBarrio()                  { return barrio; }
    public String getCiudad()                  { return ciudad; }
    public String getTelefonoFijo()            { return telefonoFijo; }
    public String getCelular()                 { return celular; }
    public String getCorreoPersonal()          { return correoPersonal; }
    public Programa getPrograma()              { return programa; }
    public Integer getSemestreActual()         { return semestreActual; }
    public String getPerfilProfesional()       { return perfilProfesional; }
    public String getHerramientasTrabajo()     { return herramientasTrabajo; }
    public LocalDateTime getCreadoEn()         { return creadoEn; }
    public LocalDateTime getActualizadoEn()    { return actualizadoEn; }
    public List<FormacionAcademica> getFormaciones()  { return formaciones; }
    public List<ExperienciaLaboral> getExperiencias() { return experiencias; }
    public List<ReferenciaPersonal> getReferencias()  { return referencias; }

    public void setNombres(String v)               { this.nombres = v; }
    public void setApellidos(String v)             { this.apellidos = v; }
    public void setTipoDocumento(String v)         { this.tipoDocumento = v; }
    public void setNumeroDocumento(String v)       { this.numeroDocumento = v; }
    public void setLugarExpedicion(String v)       { this.lugarExpedicion = v; }
    public void setFechaNacimiento(LocalDate v)    { this.fechaNacimiento = v; }
    public void setLugarNacimiento(String v)       { this.lugarNacimiento = v; }
    public void setGenero(String v)                { this.genero = v; }
    public void setEstadoCivil(String v)           { this.estadoCivil = v; }
    public void setEps(String v)                   { this.eps = v; }
    public void setDireccion(String v)             { this.direccion = v; }
    public void setBarrio(String v)                { this.barrio = v; }
    public void setCiudad(String v)                { this.ciudad = v; }
    public void setTelefonoFijo(String v)          { this.telefonoFijo = v; }
    public void setCelular(String v)               { this.celular = v; }
    public void setCorreoPersonal(String v)        { this.correoPersonal = v; }
    public void setPrograma(Programa v)            { this.programa = v; }
    public void setSemestreActual(Integer v)       { this.semestreActual = v; }
    public void setPerfilProfesional(String v)     { this.perfilProfesional = v; }
    public void setHerramientasTrabajo(String v)   { this.herramientasTrabajo = v; }
}
