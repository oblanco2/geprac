package com.udi.geprac.legalizacion.formatos;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Documento PDF tamaño carta, escrito sin bibliotecas externas: con él se
 * diligencian las plantillas de los cuatro formatos (CU-08). Usa las fuentes
 * estándar Helvetica y Helvetica negrita, que todo lector de PDF trae, con la
 * codificación WinAnsi, la de Windows-1252, que incluye las tildes, la eñe y
 * los signos del español.
 *
 * El contenido se compone de arriba hacia abajo —barras de sección, filas de
 * campos con borde, párrafos y bloques de firmas— y pasa a la página siguiente
 * cuando no cabe. Cada página lleva el encabezado del formato, con su código,
 * su versión y su número de página, y un pie con los datos de la emisión.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public final class DocumentoPdf {

    private static final Charset WINANSI = Charset.forName("windows-1252");
    private static final float ANCHO = 612, ALTO = 792, MARGEN = 48;
    private static final float CONTENIDO = ANCHO - 2 * MARGEN;
    private static final float ALTO_ENCABEZADO = 58;
    private static final float TOPE = ALTO - MARGEN - ALTO_ENCABEZADO - 14;
    private static final float PISO = MARGEN + 26;
    private static final String GRIS_TEXTO = "0.33 g", NEGRO = "0 g";

    /** Anchos de Helvetica y Helvetica negrita, en milésimas del tamaño, de los caracteres 32 a 255 de WinAnsi. */
    private static final short[] ANCHO_NORMAL = {
        278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278, 556, 556, 556, 556,
        556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556, 1015, 667, 667, 722, 722, 667, 611, 778,
        722, 278, 500, 667, 556, 833, 722, 778, 667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278,
        278, 278, 469, 556, 333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
        556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584, 761, 556, 0, 222, 556,
        333, 1000, 556, 556, 333, 1000, 667, 333, 1000, 0, 611, 0, 0, 222, 222, 333, 333, 350, 556, 1000,
        333, 1000, 500, 333, 944, 0, 500, 667, 278, 333, 556, 556, 556, 556, 260, 556, 333, 737, 370, 556,
        584, 333, 737, 333, 400, 584, 333, 333, 333, 556, 537, 278, 333, 333, 365, 556, 834, 834, 834, 611,
        667, 667, 667, 667, 667, 667, 1000, 722, 667, 667, 667, 667, 278, 278, 278, 278, 722, 722, 778, 778,
        778, 778, 778, 584, 778, 722, 722, 722, 722, 667, 667, 611, 556, 556, 556, 556, 556, 556, 889, 500,
        556, 556, 556, 556, 278, 278, 278, 278, 556, 556, 556, 556, 556, 556, 556, 584, 611, 556, 556, 556,
        556, 500, 556, 500
    };
    private static final short[] ANCHO_NEGRITA = {
        278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278, 556, 556, 556, 556,
        556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611, 975, 722, 722, 722, 722, 667, 611, 778,
        722, 278, 556, 722, 611, 833, 722, 778, 667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333,
        278, 333, 584, 556, 333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
        611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584, 761, 556, 0, 278, 556,
        500, 1000, 556, 556, 333, 1000, 667, 333, 1000, 0, 611, 0, 0, 278, 278, 500, 500, 350, 556, 1000,
        333, 1000, 556, 333, 944, 0, 500, 667, 278, 333, 556, 556, 556, 556, 280, 556, 333, 737, 370, 556,
        584, 333, 737, 333, 400, 584, 333, 333, 333, 611, 556, 278, 333, 333, 365, 556, 834, 834, 834, 611,
        722, 722, 722, 722, 722, 722, 1000, 722, 667, 667, 667, 667, 278, 278, 278, 278, 722, 722, 778, 778,
        778, 778, 778, 584, 778, 722, 722, 722, 722, 667, 667, 611, 556, 556, 556, 556, 556, 556, 889, 556,
        556, 556, 556, 556, 278, 278, 278, 278, 611, 611, 611, 611, 611, 611, 611, 584, 611, 611, 611, 611,
        611, 556, 611, 556
    };

    private final String titulo, codigo, version, pie;
    private final List<StringBuilder> paginas = new ArrayList<>();
    private StringBuilder pagina;
    private float y;

    /**
     * @param titulo  el nombre del formato, en mayúsculas, como sale en el encabezado
     * @param codigo  el código oficial, como PR-04
     * @param version la versión de la plantilla
     * @param pie     el texto del pie de página: quién emitió el documento y cuándo
     */
    public DocumentoPdf(String titulo, String codigo, String version, String pie) {
        this.titulo = titulo;
        this.codigo = codigo;
        this.version = version;
        this.pie = pie;
        nuevaPagina();
    }

    // ------------------------------------------------------------- contenido

    /** Una barra de sección, con el título en negrita sobre fondo gris. */
    public void seccion(String texto) {
        cabe(22 + 30);
        y -= 6;
        pagina.append("0.86 g ").append(n(MARGEN)).append(' ').append(n(y - 15)).append(' ')
            .append(n(CONTENIDO)).append(" 15 re f\n");
        texto(MARGEN + 5, y - 11, true, 9, texto.toUpperCase(Locale.of("es", "CO")), NEGRO);
        y -= 15 + 6;
    }

    /** Un párrafo de texto corrido, partido en renglones a lo ancho de la página. */
    public void parrafo(String texto) {
        List<String> renglones = partir(texto, false, 9.5f, CONTENIDO);
        for (String r : renglones) {
            cabe(13);
            texto(MARGEN, y - 9.5f, false, 9.5f, r, NEGRO);
            y -= 13;
        }
        y -= 3;
    }

    /** Un espacio en blanco del alto indicado, en puntos. */
    public void espacio(float puntos) {
        y -= puntos;
    }

    /**
     * Una fila de campos con borde, como las de un formulario impreso: cada
     * celda lleva su etiqueta arriba, en letra pequeña, y su valor debajo. Las
     * celdas se reparten el ancho según lo que contienen.
     *
     * @param celdas pares {etiqueta, valor}; la etiqueta puede ser vacía
     */
    public void fila(List<String[]> celdas) {
        int k = celdas.size();
        float[] peso = new float[k];
        float total = 0;
        for (int i = 0; i < k; i++) {
            String[] c = celdas.get(i);
            peso[i] = Math.max(10, Math.min(42, Math.max(c[0].length() * 0.8f, c[1].length())));
            total += peso[i];
        }
        float[] ancho = new float[k];
        List<List<String>> valores = new ArrayList<>();
        float alto = 0;
        for (int i = 0; i < k; i++) {
            ancho[i] = CONTENIDO * peso[i] / total;
            List<String> v = partir(celdas.get(i)[1], false, 9.5f, ancho[i] - 8);
            valores.add(v);
            float a = (celdas.get(i)[0].isBlank() ? 6 : 15) + v.size() * 12 + 4;
            alto = Math.max(alto, a);
        }
        cabe(alto);
        float x = MARGEN;
        pagina.append("0.6 G 0.5 w\n");
        for (int i = 0; i < k; i++) {
            pagina.append(n(x)).append(' ').append(n(y - alto)).append(' ').append(n(ancho[i])).append(' ')
                .append(n(alto)).append(" re S\n");
            float yy = y;
            String etiqueta = celdas.get(i)[0];
            if (!etiqueta.isBlank()) {
                texto(x + 4, yy - 9, true, 6.8f, etiqueta.toUpperCase(Locale.of("es", "CO")), GRIS_TEXTO);
                yy -= 11;
            } else {
                yy -= 2;
            }
            for (String r : valores.get(i)) {
                texto(x + 4, yy - 10, false, 9.5f, r, NEGRO);
                yy -= 12;
            }
            x += ancho[i];
        }
        y -= alto;
    }

    /**
     * Un bloque de firmas: una columna por firmante, con el renglón para firmar
     * y debajo la leyenda; el primer renglón de cada leyenda va en negrita.
     */
    public void firmas(List<String> leyendas) {
        int k = leyendas.size();
        int lineas = 1;
        for (String l : leyendas) lineas = Math.max(lineas, l.split("\n").length);
        float alto = 46 + lineas * 12;
        cabe(alto);
        float col = CONTENIDO / k, linea = Math.min(170, col - 24);
        pagina.append("0 G 0.7 w\n");
        for (int i = 0; i < k; i++) {
            float centro = MARGEN + col * i + col / 2;
            float yl = y - 40;
            pagina.append(n(centro - linea / 2)).append(' ').append(n(yl)).append(" m ")
                .append(n(centro + linea / 2)).append(' ').append(n(yl)).append(" l S\n");
            String[] partes = leyendas.get(i).split("\n");
            for (int j = 0; j < partes.length; j++) {
                boolean negrita = j == 0;
                float tam = negrita ? 9 : 8.5f;
                String t = partes[j];
                texto(centro - medir(t, negrita, tam) / 2, yl - 11 - j * 12, negrita, tam, t, j == 0 ? NEGRO : GRIS_TEXTO);
            }
        }
        y -= alto;
    }

    /** Termina el documento: pone el encabezado y el pie en cada página y devuelve el PDF. */
    public byte[] cerrar() {
        List<byte[]> objetos = new ArrayList<>();
        objetos.add(null);   // 1 catálogo
        objetos.add(null);   // 2 árbol de páginas
        objetos.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));
        objetos.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>"));
        StringBuilder hijos = new StringBuilder();
        int total = paginas.size();
        for (int p = 0; p < total; p++) {
            StringBuilder flujo = new StringBuilder();
            marco(flujo, p + 1, total);
            flujo.append(paginas.get(p));
            byte[] contenido = flujo.toString().getBytes(WINANSI);
            int numPagina = objetos.size() + 1;
            hijos.append(numPagina).append(" 0 R ");
            objetos.add(ascii("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + (numPagina + 1) + " 0 R >>"));
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            s.writeBytes(ascii("<< /Length " + contenido.length + " >>\nstream\n"));
            s.writeBytes(contenido);
            s.writeBytes(ascii("\nendstream"));
            objetos.add(s.toByteArray());
        }
        objetos.set(0, ascii("<< /Type /Catalog /Pages 2 0 R >>"));
        objetos.set(1, ascii("<< /Type /Pages /Kids [" + hijos.toString().trim() + "] /Count " + total + " >>"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(ascii("%PDF-1.4\n"));
        out.writeBytes(new byte[] { '%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n' });
        long[] posicion = new long[objetos.size()];
        for (int i = 0; i < objetos.size(); i++) {
            posicion[i] = out.size();
            out.writeBytes(ascii((i + 1) + " 0 obj\n"));
            out.writeBytes(objetos.get(i));
            out.writeBytes(ascii("\nendobj\n"));
        }
        long xref = out.size();
        StringBuilder tabla = new StringBuilder("xref\n0 " + (objetos.size() + 1) + "\n0000000000 65535 f \n");
        for (long p : posicion) tabla.append(String.format("%010d 00000 n \n", p));
        tabla.append("trailer\n<< /Size ").append(objetos.size() + 1).append(" /Root 1 0 R >>\nstartxref\n")
            .append(xref).append("\n%%EOF\n");
        out.writeBytes(ascii(tabla.toString()));
        return out.toByteArray();
    }

    /** El número de páginas que lleva el documento. */
    public int paginas() {
        return paginas.size();
    }

    // ----------------------------------------------------------------- apoyo

    private void nuevaPagina() {
        pagina = new StringBuilder();
        paginas.add(pagina);
        y = TOPE;
    }

    /** Si lo que sigue no cabe en la página, continúa en la siguiente. */
    private void cabe(float alto) {
        if (y - alto < PISO) nuevaPagina();
    }

    /** El encabezado del formato y el pie de página. */
    private void marco(StringBuilder f, int numero, int total) {
        float arriba = ALTO - MARGEN, abajo = arriba - ALTO_ENCABEZADO;
        float c1 = 92, c3 = 120, c2 = CONTENIDO - c1 - c3;
        f.append("0 G 0.8 w\n");
        f.append(n(MARGEN)).append(' ').append(n(abajo)).append(' ').append(n(CONTENIDO)).append(' ')
            .append(n(ALTO_ENCABEZADO)).append(" re S\n");
        f.append(n(MARGEN + c1)).append(' ').append(n(abajo)).append(" m ").append(n(MARGEN + c1)).append(' ')
            .append(n(arriba)).append(" l S\n");
        f.append(n(MARGEN + c1 + c2)).append(' ').append(n(abajo)).append(" m ").append(n(MARGEN + c1 + c2)).append(' ')
            .append(n(arriba)).append(" l S\n");
        // la sigla de la universidad, en texto: el software no reproduce su logotipo
        texto(f, MARGEN + c1 / 2 - medir("UDI", true, 22) / 2, abajo + 22, true, 22, "UDI", NEGRO);
        String[] centro = { "UNIVERSIDAD DE INVESTIGACIÓN Y DESARROLLO", "Dirección de Proyección Social y Extensión", titulo };
        float[] tam = { 8.5f, 8, 9.5f };
        boolean[] negrita = { true, false, true };
        for (int i = 0; i < 3; i++) {
            List<String> r = partir(centro[i], negrita[i], tam[i], c2 - 10);
            String t = r.get(0);
            texto(f, MARGEN + c1 + c2 / 2 - medir(t, negrita[i], tam[i]) / 2, arriba - 15 - i * 15, negrita[i], tam[i], t,
                i == 1 ? GRIS_TEXTO : NEGRO);
        }
        String[] derecha = { "Código: " + codigo, "Versión: " + version, "Página " + numero + " de " + total };
        for (int i = 0; i < 3; i++)
            texto(f, MARGEN + c1 + c2 + 8, arriba - 15 - i * 15, i == 0, 8.5f, derecha[i], NEGRO);
        String p = pie + " · Página " + numero + " de " + total;
        texto(f, MARGEN + CONTENIDO / 2 - medir(p, false, 7) / 2, MARGEN - 2, false, 7, p, GRIS_TEXTO);
    }

    private void texto(float x, float yy, boolean negrita, float tam, String t, String color) {
        texto(pagina, x, yy, negrita, tam, t, color);
    }

    private static void texto(StringBuilder f, float x, float yy, boolean negrita, float tam, String t, String color) {
        f.append(color).append(" BT /").append(negrita ? "F2 " : "F1 ").append(n(tam)).append(" Tf ")
            .append(n(x)).append(' ').append(n(yy)).append(" Td (").append(escapar(t)).append(") Tj ET\n");
    }

    /** Parte el texto en renglones que caben en el ancho; respeta los saltos de línea del texto. */
    static List<String> partir(String texto, boolean negrita, float tam, float ancho) {
        List<String> renglones = new ArrayList<>();
        for (String bloque : (texto == null ? "" : texto).split("\n", -1)) {
            StringBuilder linea = new StringBuilder();
            for (String palabra : bloque.trim().split(" +")) {
                if (palabra.isEmpty()) continue;
                String prueba = linea.isEmpty() ? palabra : linea + " " + palabra;
                if (medir(prueba, negrita, tam) <= ancho) {
                    linea.setLength(0);
                    linea.append(prueba);
                    continue;
                }
                if (!linea.isEmpty()) renglones.add(linea.toString());
                linea.setLength(0);
                // una palabra más ancha que el renglón se corta donde haga falta
                while (medir(palabra, negrita, tam) > ancho && palabra.length() > 1) {
                    int k = palabra.length() - 1;
                    while (k > 1 && medir(palabra.substring(0, k), negrita, tam) > ancho) k--;
                    renglones.add(palabra.substring(0, k));
                    palabra = palabra.substring(k);
                }
                linea.append(palabra);
            }
            renglones.add(linea.toString());
        }
        return renglones;
    }

    /** El ancho del texto en puntos, con las métricas de la fuente. */
    static float medir(String t, boolean negrita, float tam) {
        short[] tabla = negrita ? ANCHO_NEGRITA : ANCHO_NORMAL;
        float total = 0;
        for (byte b : t.getBytes(WINANSI)) {
            int c = b & 0xFF;
            total += c >= 32 ? tabla[c - 32] : 0;
        }
        return total * tam / 1000f;
    }

    /** El texto en WinAnsi, con los paréntesis y la barra invertida escapados; lo que no existe en WinAnsi sale como «?». */
    private static String escapar(String t) {
        String limpio = new String(t.getBytes(WINANSI), WINANSI);
        return limpio.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static String n(float v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private static byte[] ascii(String s) {
        return s.getBytes(WINANSI);
    }
}
