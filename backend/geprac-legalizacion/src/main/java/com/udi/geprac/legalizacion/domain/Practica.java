package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Práctica del plan de estudios de un programa, con su contenido estandarizado:
 * tabla practica del esquema legalizacion. La mantiene la Dirección del
 * Programa (CU-02) y la inscripción copia su contenido (CU-05).
 *
 * El programa se nombra por su código institucional, que es la llave del
 * programa en MS-01; aquí se guarda como valor, sin clave foránea. Los
 * objetivos específicos y las actividades le pertenecen y se guardan con ella.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "practica")
public class Practica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_programa", nullable = false, length = 10)
    private String codigoPrograma;

    /** Posición en el plan de estudios, de 1 a 8: es el semestre en que se cursa. */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "objetivo_general", nullable = false, columnDefinition = "text")
    private String objetivoGeneral;

    @Column(name = "horario_estandar", length = 150)
    private String horarioEstandar;

    @Column(nullable = false)
    private Boolean activa;

    @OneToMany(mappedBy = "practica", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicion")
    private List<ObjetivoPractica> objetivos = new ArrayList<>();

    @OneToMany(mappedBy = "practica", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicion")
    private List<ActividadPractica> actividades = new ArrayList<>();

    protected Practica() { }   // exigido por JPA

    /** Una práctica nueva del catálogo de un programa (CU-02, paso 11). */
    public Practica(String codigoPrograma) {
        this.codigoPrograma = codigoPrograma;
    }

    /**
     * CU-02, pasos 5 a 7 y variaciones 8.1 y 8.2: el contenido de la práctica,
     * con sus dos listas en el orden en que quedaron. Las inscripciones ya
     * creadas no cambian: cada una guarda su propia copia.
     */
    public void actualizar(int orden, String nombre, String objetivoGeneral, String horarioEstandar, boolean activa,
                           List<String> objetivos, List<String> actividades) {
        this.orden = orden;
        this.nombre = nombre;
        this.objetivoGeneral = objetivoGeneral;
        this.horarioEstandar = horarioEstandar;
        this.activa = activa;
        this.objetivos.clear();
        for (int i = 0; i < objetivos.size(); i++) this.objetivos.add(new ObjetivoPractica(this, i + 1, objetivos.get(i)));
        this.actividades.clear();
        for (int i = 0; i < actividades.size(); i++) this.actividades.add(new ActividadPractica(this, i + 1, actividades.get(i)));
    }

    public Long getId()                 { return id; }
    public String getCodigoPrograma()   { return codigoPrograma; }
    public Integer getOrden()           { return orden; }
    public String getNombre()           { return nombre; }
    public String getObjetivoGeneral()  { return objetivoGeneral; }
    public String getHorarioEstandar()  { return horarioEstandar; }
    public Boolean getActiva()          { return activa; }
    public List<ObjetivoPractica> getObjetivos()   { return objetivos; }
    public List<ActividadPractica> getActividades() { return actividades; }
}
