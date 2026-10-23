package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Plantilla de un formato institucional: tabla plantilla_formato del esquema
 * legalizacion. El archivo se versiona aparte del código (RNF-09): un cambio en
 * el formato oficial se resuelve con una plantilla nueva puesta en vigencia,
 * sin modificar el software. Solo una por formato está vigente.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "plantilla_formato")
public class PlantillaFormato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 6)
    private TipoFormato tipo;

    @Column(nullable = false, length = 10)
    private String version;

    @Column(nullable = false)
    private Boolean vigente;

    /** Ubicación del archivo de la plantilla, dentro de los recursos del servicio. */
    @Column(nullable = false, length = 255)
    private String archivo;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    protected PlantillaFormato() { }   // exigido por JPA

    public Long getId()               { return id; }
    public TipoFormato getTipo()      { return tipo; }
    public String getVersion()        { return version; }
    public Boolean getVigente()       { return vigente; }
    public String getArchivo()        { return archivo; }
    public LocalDateTime getCreadaEn(){ return creadaEn; }
}
