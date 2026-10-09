package com.udi.geprac.academico.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cuenta con la que una persona ingresa al software: tabla usuario del esquema
 * identidad.
 *
 * Su identificador es el mismo que asigna Supabase Auth, y MS-01 crea la fila
 * la primera vez que la persona ingresa. El rol lo asigna la Dirección del
 * Programa; el programa solo se registra para el director, que opera sobre él.
 *
 * @author Oscar Iván Blanco Díaz
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    private UUID id;

    @Column(name = "nombre_presentacion", nullable = false, length = 150)
    private String nombrePresentacion;

    @Column(name = "correo_institucional", nullable = false, length = 150)
    private String correoInstitucional;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Rol rol;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    /** El programa que dirige, cuando el rol es DIRECTOR. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_programa")
    private Programa programa;

    protected Usuario() { }   // exigido por JPA

    /** La cuenta del primer ingreso: sin rol hasta que la Dirección del Programa se lo asigne. */
    public Usuario(UUID id, String nombrePresentacion, String correoInstitucional) {
        this.id = id;
        this.nombrePresentacion = nombrePresentacion;
        this.correoInstitucional = correoInstitucional;
        this.creadoEn = LocalDateTime.now();
    }

    public UUID getId()                     { return id; }
    public String getNombrePresentacion()   { return nombrePresentacion; }
    public String getCorreoInstitucional()  { return correoInstitucional; }
    public Rol getRol()                     { return rol; }
    public LocalDateTime getCreadoEn()      { return creadoEn; }
    public Programa getPrograma()           { return programa; }
}
