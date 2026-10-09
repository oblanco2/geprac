package com.udi.geprac.legalizacion.formatos;

import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.InscripcionDatos;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Los datos con que se diligencian las plantillas: los de la copia que guarda
 * la inscripción —los del estudiante y la práctica, tomados al iniciarla, y los
 * de la institución y su contacto, tomados al seleccionarlos— y los de la
 * emisión. La ciudad y la fecha con que se firman los formatos son la sede de
 * la universidad y la fecha de emisión, no datos de la inscripción (CU-08).
 *
 * @param campos los datos sueltos, por su nombre en la plantilla
 * @param listas las listas copiadas —objetivos, actividades, formación,
 *               experiencia y referencias—, cada elemento con sus datos
 * @author Darien Asdrwal Pesca Ojeda
 */
public record DatosFormato(Map<String, String> campos, Map<String, List<Map<String, String>>> listas) {

    /** La sede de la universidad, donde se firman los formatos. */
    public static final String CIUDAD_SEDE = "Bucaramanga";

    private static final DateTimeFormatter CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter LARGA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "CO"));

    public static DatosFormato de(Inscripcion i, LocalDateTime emision) {
        InscripcionDatos d = i.getDatos();
        Map<String, String> c = new LinkedHashMap<>();
        c.put("nombres", d.getNombres());
        c.put("apellidos", d.getApellidos());
        c.put("tipoDocumento", d.getTipoDocumento());
        c.put("numeroDocumento", d.getNumeroDocumento());
        c.put("lugarExpedicion", d.getLugarExpedicion());
        c.put("fechaNacimiento", fecha(d.getFechaNacimiento()));
        c.put("lugarNacimiento", d.getLugarNacimiento());
        c.put("genero", d.getGenero());
        c.put("estadoCivil", d.getEstadoCivil());
        c.put("eps", d.getEps());
        c.put("direccion", d.getDireccion());
        c.put("barrio", d.getBarrio());
        c.put("ciudad", d.getCiudad());
        c.put("telefonoFijo", d.getTelefonoFijo());
        c.put("celular", d.getCelular());
        c.put("correoPersonal", d.getCorreoPersonal());
        c.put("nombrePrograma", d.getNombrePrograma());
        c.put("semestreCursado", d.getSemestreCursado() == null ? null : String.valueOf(d.getSemestreCursado()));
        c.put("perfilProfesional", d.getPerfilProfesional());
        c.put("herramientasTrabajo", d.getHerramientasTrabajo());
        c.put("institucionRazonSocial", d.getInstitucionRazonSocial());
        c.put("institucionNit", d.getInstitucionNit());
        c.put("institucionDireccion", d.getInstitucionDireccion());
        c.put("institucionCiudad", d.getInstitucionCiudad());
        c.put("institucionTelefono", d.getInstitucionTelefono());
        c.put("institucionCorreo", d.getInstitucionCorreo());
        c.put("institucionSitioWeb", d.getInstitucionSitioWeb());
        c.put("institucionRepresentante", d.getInstitucionRepresentante());
        c.put("contactoNombre", d.getContactoNombre());
        c.put("contactoCargo", d.getContactoCargo());
        c.put("contactoTelefono", d.getContactoTelefono());
        c.put("contactoCelular", d.getContactoCelular());
        c.put("contactoCorreo", d.getContactoCorreo());
        c.put("nombrePractica", d.getNombrePractica());
        c.put("objetivoGeneral", d.getObjetivoGeneral());
        c.put("horarioEstandar", d.getHorarioEstandar());
        c.put("ordenPractica", String.valueOf(i.getPractica().getOrden()));
        c.put("semestre", i.getSemestre().getCodigo());
        c.put("fechaInicio", fecha(i.getFechaInicio()));
        c.put("fechaFin", fecha(i.getFechaFin()));
        c.put("ciudadEmision", CIUDAD_SEDE);
        c.put("fechaEmision", emision.toLocalDate().format(LARGA));

        Map<String, List<Map<String, String>>> l = new LinkedHashMap<>();
        l.put("objetivos", i.getObjetivos().stream()
            .map(o -> Map.of("posicion", String.valueOf(o.getPosicion()), "texto", o.getTexto())).toList());
        l.put("actividades", i.getActividades().stream()
            .map(a -> Map.of("posicion", String.valueOf(a.getPosicion()), "texto", a.getTexto())).toList());
        l.put("formaciones", i.getFormaciones().stream().map(f -> elemento(
            "tipo", f.getTipo(), "nombre", f.getNombre(), "institucion", f.getInstitucion(),
            "anio", f.getAnio() == null ? "En curso" : String.valueOf(f.getAnio()))).toList());
        l.put("experiencias", i.getExperiencias().stream().map(x -> elemento(
            "empresa", x.getEmpresa(), "cargo", x.getCargo(), "jefeInmediato", x.getJefeInmediato(),
            "cargoJefe", x.getCargoJefe(), "telefonoEmpresa", x.getTelefonoEmpresa(),
            "periodo", fecha(x.getFechaInicio()) + " a " + (x.getFechaFin() == null ? "la fecha" : fecha(x.getFechaFin())),
            "funciones", x.getFunciones(), "logros", x.getLogros())).toList());
        l.put("referencias", i.getReferencias().stream().map(r -> elemento(
            "nombre", r.getNombre(), "empresa", r.getEmpresa(), "cargo", r.getCargo(),
            "telefono", r.getTelefono(), "ciudad", r.getCiudad())).toList());
        return new DatosFormato(c, l);
    }

    private static Map<String, String> elemento(String... pares) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int k = 0; k + 1 < pares.length; k += 2) m.put(pares[k], pares[k + 1]);
        return m;
    }

    private static String fecha(LocalDate f) {
        return f == null ? null : f.format(CORTA);
    }
}
