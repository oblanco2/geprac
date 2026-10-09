package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Inscripción de una práctica por un estudiante en un semestre: tabla
 * inscripcion del esquema legalizacion. Es el expediente completo de la
 * legalización: al crearse (CU-05) copia los datos del estudiante y el
 * contenido de la práctica, y desde ahí la revisan el tutor (CU-06) y la
 * Dirección (CU-07). El estudiante es un registro de MS-01 y se guarda como
 * valor.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "inscripcion")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estudiante_id", nullable = false)
    private Long estudianteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practica_id", nullable = false)
    private Practica practica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semestre_id", nullable = false)
    private Semestre semestre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institucion_id")
    private Institucion institucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_id")
    private ContactoInstitucion contacto;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoInscripcion estado;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    @Column(name = "enviada_en")
    private LocalDateTime enviadaEn;

    @OneToOne(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private InscripcionDatos datos;

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicion")
    private List<InscripcionObjetivo> objetivos = new ArrayList<>();

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicion")
    private List<InscripcionActividad> actividades = new ArrayList<>();

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<InscripcionFormacion> formaciones = new ArrayList<>();

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<InscripcionExperiencia> experiencias = new ArrayList<>();

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<InscripcionReferencia> referencias = new ArrayList<>();

    @OneToMany(mappedBy = "inscripcion")
    @OrderBy("fecha")
    private List<Revision> revisiones = new ArrayList<>();

    @OneToOne(mappedBy = "inscripcion")
    private Aval aval;

    protected Inscripcion() { }   // exigido por JPA

    /** CU-06, paso 8: el resultado de la revisión cambia el estado. */
    public void resolver(Revision revision) {
        revisiones.add(revision);
        estado = revision.getResultado() == ResultadoRevision.APROBADA ? EstadoInscripcion.APROBADA : EstadoInscripcion.DEVUELTA;
    }

    /** CU-07, paso 8: con el aval la inscripción queda avalada y se pueden emitir sus formatos. */
    public void avalar(Aval aval) {
        this.aval = aval;
        estado = EstadoInscripcion.AVALADA;
    }

    /** El motivo de la última devolución, mientras la inscripción siga devuelta. */
    public String motivoDevolucion() {
        if (estado != EstadoInscripcion.DEVUELTA) return null;
        for (int k = revisiones.size() - 1; k >= 0; k--)
            if (revisiones.get(k).getResultado() == ResultadoRevision.DEVUELTA) return revisiones.get(k).getMotivo();
        return null;
    }

    public Long getId()                       { return id; }
    public Long getEstudianteId()             { return estudianteId; }
    public Practica getPractica()             { return practica; }
    public Semestre getSemestre()             { return semestre; }
    public Institucion getInstitucion()       { return institucion; }
    public ContactoInstitucion getContacto()  { return contacto; }
    public LocalDate getFechaInicio()         { return fechaInicio; }
    public LocalDate getFechaFin()            { return fechaFin; }
    public EstadoInscripcion getEstado()      { return estado; }
    public LocalDateTime getCreadaEn()        { return creadaEn; }
    public LocalDateTime getEnviadaEn()       { return enviadaEn; }
    public InscripcionDatos getDatos()        { return datos; }
    public List<InscripcionObjetivo> getObjetivos()       { return objetivos; }
    public List<InscripcionActividad> getActividades()     { return actividades; }
    public List<InscripcionFormacion> getFormaciones()     { return formaciones; }
    public List<InscripcionExperiencia> getExperiencias()  { return experiencias; }
    public List<InscripcionReferencia> getReferencias()    { return referencias; }
    public List<Revision> getRevisiones()                  { return revisiones; }
    public Aval getAval()                                  { return aval; }
}
