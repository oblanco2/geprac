package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Práctica del plan de estudios de un programa, con su contenido estandarizado:
 * tabla practica del esquema legalizacion. La mantiene la Dirección del
 * Programa (CU-02); aquí se consulta.
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
