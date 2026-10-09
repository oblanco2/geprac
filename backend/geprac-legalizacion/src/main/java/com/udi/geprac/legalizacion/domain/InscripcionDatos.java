package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Copia de los datos con los que se diligencian los formatos: tabla
 * inscripcion_datos del esquema legalizacion. Reúne, en el momento de inscribir,
 * los datos personales y los bloques de texto de la hoja de vida del estudiante
 * (de MS-01), el contenido de la práctica (del catálogo) y la institución
 * receptora con su contacto. Comparte la llave de su inscripción.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Entity
@Table(name = "inscripcion_datos")
public class InscripcionDatos {

    @Id
    @Column(name = "inscripcion_id")
    private Long inscripcionId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id")
    private Inscripcion inscripcion;

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

    @Column(name = "estado_civil", length = 20)
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

    @Column(name = "nombre_programa", nullable = false, length = 150)
    private String nombrePrograma;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "semestre_cursado", nullable = false)
    private Integer semestreCursado;

    @Column(name = "perfil_profesional", columnDefinition = "text")
    private String perfilProfesional;

    @Column(name = "herramientas_trabajo", columnDefinition = "text")
    private String herramientasTrabajo;

    @Column(name = "institucion_razon_social", length = 150)
    private String institucionRazonSocial;

    @Column(name = "institucion_nit", length = 20)
    private String institucionNit;

    @Column(name = "institucion_direccion", length = 150)
    private String institucionDireccion;

    @Column(name = "institucion_ciudad", length = 80)
    private String institucionCiudad;

    @Column(name = "institucion_telefono", length = 20)
    private String institucionTelefono;

    @Column(name = "institucion_correo", length = 150)
    private String institucionCorreo;

    @Column(name = "institucion_sitio_web", length = 150)
    private String institucionSitioWeb;

    @Column(name = "institucion_representante", length = 150)
    private String institucionRepresentante;

    @Column(name = "contacto_nombre", length = 150)
    private String contactoNombre;

    @Column(name = "contacto_cargo", length = 100)
    private String contactoCargo;

    @Column(name = "contacto_telefono", length = 20)
    private String contactoTelefono;

    @Column(name = "contacto_celular", length = 20)
    private String contactoCelular;

    @Column(name = "contacto_correo", length = 150)
    private String contactoCorreo;

    @Column(name = "nombre_practica", nullable = false, length = 150)
    private String nombrePractica;

    @Column(name = "objetivo_general", nullable = false, columnDefinition = "text")
    private String objetivoGeneral;

    @Column(name = "horario_estandar", length = 150)
    private String horarioEstandar;

    protected InscripcionDatos() { }   // exigido por JPA

    public Long getInscripcionId() { return inscripcionId; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getLugarExpedicion() { return lugarExpedicion; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getLugarNacimiento() { return lugarNacimiento; }
    public String getGenero() { return genero; }
    public String getEstadoCivil() { return estadoCivil; }
    public String getEps() { return eps; }
    public String getDireccion() { return direccion; }
    public String getBarrio() { return barrio; }
    public String getCiudad() { return ciudad; }
    public String getTelefonoFijo() { return telefonoFijo; }
    public String getCelular() { return celular; }
    public String getCorreoPersonal() { return correoPersonal; }
    public String getNombrePrograma() { return nombrePrograma; }
    public Integer getSemestreCursado() { return semestreCursado; }
    public String getPerfilProfesional() { return perfilProfesional; }
    public String getHerramientasTrabajo() { return herramientasTrabajo; }
    public String getInstitucionRazonSocial() { return institucionRazonSocial; }
    public String getInstitucionNit() { return institucionNit; }
    public String getInstitucionDireccion() { return institucionDireccion; }
    public String getInstitucionCiudad() { return institucionCiudad; }
    public String getInstitucionTelefono() { return institucionTelefono; }
    public String getInstitucionCorreo() { return institucionCorreo; }
    public String getInstitucionSitioWeb() { return institucionSitioWeb; }
    public String getInstitucionRepresentante() { return institucionRepresentante; }
    public String getContactoNombre() { return contactoNombre; }
    public String getContactoCargo() { return contactoCargo; }
    public String getContactoTelefono() { return contactoTelefono; }
    public String getContactoCelular() { return contactoCelular; }
    public String getContactoCorreo() { return contactoCorreo; }
    public String getNombrePractica() { return nombrePractica; }
    public String getObjetivoGeneral() { return objetivoGeneral; }
    public String getHorarioEstandar() { return horarioEstandar; }
}
