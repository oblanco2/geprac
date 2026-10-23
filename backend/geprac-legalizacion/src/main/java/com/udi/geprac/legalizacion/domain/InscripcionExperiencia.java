package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Copia de una entrada de la experiencia laboral del estudiante en la inscripción: tabla
 * inscripcion_experiencia del esquema legalizacion. Se toma de la hoja de vida al crear la
 * inscripción y con ella se diligencia el formato PR-01.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "inscripcion_experiencia")
public class InscripcionExperiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

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

    protected InscripcionExperiencia() { }   // exigido por JPA

    public InscripcionExperiencia(Inscripcion inscripcion, String empresa, String cargo, String jefeInmediato, String cargoJefe, String telefonoEmpresa, LocalDate fechaInicio, LocalDate fechaFin, String funciones, String logros) {
        this.inscripcion = inscripcion;
        this.empresa = empresa;
        this.cargo = cargo;
        this.jefeInmediato = jefeInmediato;
        this.cargoJefe = cargoJefe;
        this.telefonoEmpresa = telefonoEmpresa;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.funciones = funciones;
        this.logros = logros;
    }

    public Long getId() { return id; }
    public String getEmpresa() { return empresa; }
    public String getCargo() { return cargo; }
    public String getJefeInmediato() { return jefeInmediato; }
    public String getCargoJefe() { return cargoJefe; }
    public String getTelefonoEmpresa() { return telefonoEmpresa; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getFunciones() { return funciones; }
    public String getLogros() { return logros; }
}
