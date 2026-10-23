package com.udi.geprac.legalizacion.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Los cuatro formatos institucionales que el software emite (CU-08). El PR-03,
 * modelo de convenio, queda fuera: solo se usa cuando la institución no tiene
 * convenio previo. En la base cada formato se guarda con su código oficial,
 * con guion: PR-01, PR-02, PR-04 o PR-05.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public enum TipoFormato {
    PR01("PR-01", "Hoja de vida del estudiante"),
    PR02("PR-02", "Acta de conocimiento de los términos"),
    PR04("PR-04", "Solicitud y aprobación de práctica"),
    PR05("PR-05", "Acta de compromiso");

    private final String codigo;
    private final String nombre;

    TipoFormato(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String codigo() { return codigo; }
    public String nombre() { return nombre; }

    public static TipoFormato deCodigo(String codigo) {
        for (TipoFormato t : values()) if (t.codigo.equals(codigo)) return t;
        throw new IllegalArgumentException("Formato desconocido: " + codigo);
    }

    /** Guarda y lee el formato con su código oficial. */
    @Converter(autoApply = true)
    public static class Convertidor implements AttributeConverter<TipoFormato, String> {
        @Override
        public String convertToDatabaseColumn(TipoFormato t) { return t == null ? null : t.codigo; }

        @Override
        public TipoFormato convertToEntityAttribute(String codigo) { return codigo == null ? null : deCodigo(codigo); }
    }
}
