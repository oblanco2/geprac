package com.udi.geprac.legalizacion.formatos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plantilla de un formato institucional, escrita como texto para que la
 * Dirección del Programa pueda revisarla y versionarla aparte del código
 * (RNF-09). Cada renglón es una instrucción de composición:
 *
 * <pre>
 * &#64;titulo SOLICITUD Y APROBACIÓN DE PRÁCTICA   el nombre que sale en el encabezado
 * &#64;codigo PR-04                               el código oficial del formato
 * = 1. DATOS DEL ESTUDIANTE                     una barra de sección
 * | Nombres: {nombres!} | Documento: {numeroDocumento!} |   una fila de campos con borde
 * *[No registra.] | Empresa: {experiencias.empresa} |      se repite por cada elemento de la lista
 * _ {nombres} {apellidos}\nEstudiante | Dirección del Programa   un bloque de firmas
 * ~                                             un espacio adicional
 * # comentario                                  no sale en el documento
 * </pre>
 *
 * Cualquier otro renglón es un párrafo. {campo} se reemplaza por el dato de la
 * inscripción y {campo!} además es obligatorio: si falta, no se emite nada y
 * se informa qué dato falta y en qué formato (CU-08, excepción 6.1).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public final class Plantilla {

    private static final Pattern MARCA = Pattern.compile("\\{([A-Za-z]+)(?:\\.([A-Za-z]+))?(!?)\\}");
    private static final Pattern VACIO = Pattern.compile("^\\*(?:\\[([^\\]]*)\\])?\\s*");

    /** Cómo se nombra cada dato obligatorio cuando falta. */
    private static final Map<String, String> ETIQUETAS = Map.ofEntries(
        Map.entry("nombres", "los nombres del estudiante"),
        Map.entry("apellidos", "los apellidos del estudiante"),
        Map.entry("tipoDocumento", "el tipo de documento del estudiante"),
        Map.entry("numeroDocumento", "el número de documento del estudiante"),
        Map.entry("direccion", "la dirección del estudiante"),
        Map.entry("ciudad", "la ciudad de residencia del estudiante"),
        Map.entry("celular", "el celular del estudiante"),
        Map.entry("correoPersonal", "el correo personal del estudiante"),
        Map.entry("nombrePrograma", "el programa del estudiante"),
        Map.entry("semestreCursado", "el semestre que cursa el estudiante"),
        Map.entry("institucionRazonSocial", "la razón social de la institución receptora"),
        Map.entry("institucionDireccion", "la dirección de la institución receptora"),
        Map.entry("institucionCiudad", "la ciudad de la institución receptora"),
        Map.entry("institucionTelefono", "el teléfono de la institución receptora"),
        Map.entry("contactoNombre", "el nombre del tutor del escenario"),
        Map.entry("contactoCargo", "el cargo del tutor del escenario"),
        Map.entry("contactoTelefono", "el teléfono del tutor del escenario"),
        Map.entry("nombrePractica", "el nombre de la práctica"),
        Map.entry("ordenPractica", "la posición de la práctica en el plan"),
        Map.entry("objetivoGeneral", "el objetivo general de la práctica"),
        Map.entry("semestre", "el semestre de la inscripción"),
        Map.entry("fechaInicio", "la fecha de inicio de la práctica"),
        Map.entry("fechaFin", "la fecha de finalización de la práctica"));

    private final String titulo;
    private final String codigo;
    private final List<String> cuerpo;

    private Plantilla(String titulo, String codigo, List<String> cuerpo) {
        this.titulo = titulo;
        this.codigo = codigo;
        this.cuerpo = cuerpo;
    }

    /** Lee el texto de la plantilla; exige el título y el código del formato. */
    public static Plantilla leer(String texto) {
        String titulo = null, codigo = null;
        List<String> cuerpo = new ArrayList<>();
        for (String renglon : texto.replace("\r", "").split("\n")) {
            String r = renglon.stripTrailing();
            if (r.startsWith("#")) continue;
            if (r.startsWith("@titulo ")) titulo = r.substring(8).trim();
            else if (r.startsWith("@codigo ")) codigo = r.substring(8).trim();
            else cuerpo.add(r);
        }
        if (titulo == null || codigo == null)
            throw new IllegalArgumentException("La plantilla debe declarar @titulo y @codigo.");
        while (!cuerpo.isEmpty() && cuerpo.get(0).isBlank()) cuerpo.remove(0);
        return new Plantilla(titulo, codigo, cuerpo);
    }

    public String titulo() { return titulo; }
    public String codigo() { return codigo; }

    /** Comprueba que la inscripción tenga los datos obligatorios del formato; si falta uno, dice cuál. */
    public void comprobar(DatosFormato datos) {
        for (String r : cuerpo) {
            Matcher m = MARCA.matcher(r);
            while (m.find()) {
                if (m.group(2) != null || m.group(3).isEmpty()) continue;
                String v = datos.campos().get(m.group(1));
                if (v == null || v.isBlank())
                    throw new DatoFaltanteException("Falta " + ETIQUETAS.getOrDefault(m.group(1), "el dato «" + m.group(1) + "»")
                        + " para diligenciar el formato " + codigo + ". Como la inscripción ya está avalada, no se puede "
                        + "editar: pídale a la Dirección del Programa que corrija ese dato. No se emitió ningún formato.");
            }
        }
    }

    /** Diligencia la plantilla con los datos y devuelve el documento en PDF. */
    public byte[] diligenciar(DatosFormato datos, String version, String pie) {
        comprobar(datos);
        DocumentoPdf doc = new DocumentoPdf(titulo, codigo, version, pie);
        for (int k = 0; k < cuerpo.size(); k++) {
            String r = cuerpo.get(k);
            if (!r.startsWith("*")) {
                componer(doc, r, datos, null);
                continue;
            }
            // los renglones seguidos que repiten la misma lista forman un grupo; uno sin
            // marca de lista, como un espacio, se repite con el grupo en que está
            String lista = listaDe(r);
            if (lista == null)
                throw new IllegalArgumentException("El renglón «" + r + "» repite una lista, pero no nombra ninguna.");
            List<String> grupo = new ArrayList<>(List.of(r));
            while (k + 1 < cuerpo.size() && cuerpo.get(k + 1).startsWith("*")
                    && (listaDe(cuerpo.get(k + 1)) == null || lista.equals(listaDe(cuerpo.get(k + 1)))))
                grupo.add(cuerpo.get(++k));
            List<Map<String, String>> elementos = datos.listas().getOrDefault(lista, List.of());
            if (elementos.isEmpty()) {
                Matcher v = VACIO.matcher(grupo.get(0));
                if (v.find() && v.group(1) != null) doc.parrafo(v.group(1));
                continue;
            }
            for (Map<String, String> e : elementos)
                for (String g : grupo) componer(doc, VACIO.matcher(g).replaceFirst(""), datos, e);
        }
        return doc.cerrar();
    }

    // ----------------------------------------------------------------- apoyo

    private static void componer(DocumentoPdf doc, String r, DatosFormato d, Map<String, String> elemento) {
        if (r.isBlank()) doc.espacio(4);
        else if (r.equals("~")) doc.espacio(12);
        else if (r.startsWith("= ")) doc.seccion(llenar(r.substring(2), d, elemento));
        else if (r.startsWith("|")) doc.fila(celdas(r, d, elemento));
        else if (r.startsWith("_ "))
            doc.firmas(Arrays.stream(llenar(r.substring(2), d, elemento).split("\\|"))
                .map(s -> s.trim().replace("\\n", "\n")).toList());
        else doc.parrafo(llenar(r, d, elemento));
    }

    /** Las celdas de una fila: «Etiqueta: valor»; el valor vacío sale como raya. */
    private static List<String[]> celdas(String r, DatosFormato d, Map<String, String> elemento) {
        List<String[]> celdas = new ArrayList<>();
        for (String c : r.substring(1).split("\\|")) {
            if (c.isBlank()) continue;
            int dos = c.indexOf(": ");
            String etiqueta = dos < 0 ? "" : c.substring(0, dos).trim();
            String valor = llenar(dos < 0 ? c : c.substring(dos + 2), d, elemento).trim();
            celdas.add(new String[] { etiqueta, valor.isEmpty() ? "—" : valor });
        }
        return celdas;
    }

    private static String llenar(String texto, DatosFormato d, Map<String, String> elemento) {
        Matcher m = MARCA.matcher(texto);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String v = m.group(2) == null ? d.campos().get(m.group(1))
                : elemento == null ? null : elemento.get(m.group(2));
            m.appendReplacement(sb, Matcher.quoteReplacement(v == null ? "" : v.trim()));
        }
        m.appendTail(sb);
        return sb.toString().replaceAll(" {2,}", " ");
    }

    /** La lista que repite un renglón que empieza con «*»: la de su primera marca con punto, o ninguna. */
    private static String listaDe(String r) {
        Matcher m = MARCA.matcher(r);
        while (m.find()) if (m.group(2) != null) return m.group(1);
        return null;
    }
}
