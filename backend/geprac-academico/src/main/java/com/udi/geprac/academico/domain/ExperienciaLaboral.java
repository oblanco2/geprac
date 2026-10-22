package com.udi.geprac.academico.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entrada de experiencia laboral de la hoja de vida: tabla experiencia_laboral
 * del esquema identidad. La fecha de retiro queda vacía mientras el empleo
 * está vigente; el tiempo de trabajo que pide el formato se calcula con las
 * dos fechas y no se guarda.
 *
 * @author Oscar Iván Blanco Díaz
 */
@Entity
@Table(name = "experiencia_laboral")
public class ExperienciaLaboral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false, length = 150)
    private String empresa;

    @Column(nullable = false, length = 100)
    private String cargo;

    @Column(name = "jefe_inmediato", length = 150)
    private String jefeInmediato;

    @Column(name = "cargo_jefe", length = 100)
    private String cargoJefe;

    @Column(name = "telefono_empresa", length = 20)
    private String telefonoEmpresa;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(columnDefinition = "text")
    private String funciones;

    @Column(columnDefinition = "text")
    private String logros;

    protected ExperienciaLaboral() { }   // exigido por JPA

    public ExperienciaLaboral(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Long getId()                { return id; }
    public Estudiante getEstudiante()  { return estudiante; }
    public String getEmpresa()         { return empresa; }
    public String getCargo()           { return cargo; }
    public String getJefeInmediato()   { return jefeInmediato; }
    public String getCargoJefe()       { return cargoJefe; }
    public String getTelefonoEmpresa() { return telefonoEmpresa; }
    public LocalDate getFechaInicio()  { return fechaInicio; }
    public LocalDate getFechaFin()     { return fechaFin; }
    public String getFunciones()       { return funciones; }
    public String getLogros()          { return logros; }

    public void setEmpresa(String v)         { this.empresa = v; }
    public void setCargo(String v)           { this.cargo = v; }
    public void setJefeInmediato(String v)   { this.jefeInmediato = v; }
    public void setCargoJefe(String v)       { this.cargoJefe = v; }
    public void setTelefonoEmpresa(String v) { this.telefonoEmpresa = v; }
    public void setFechaInicio(LocalDate v)  { this.fechaInicio = v; }
    public void setFechaFin(LocalDate v)     { this.fechaFin = v; }
    public void setFunciones(String v)       { this.funciones = v; }
    public void setLogros(String v)          { this.logros = v; }
}
