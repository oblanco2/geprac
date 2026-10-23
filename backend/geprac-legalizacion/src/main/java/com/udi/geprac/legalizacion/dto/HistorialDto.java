package com.udi.geprac.legalizacion.dto;

import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.InscripcionDatos;
import com.udi.geprac.legalizacion.domain.Practica;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Una línea del historial de prácticas (P-02 y P-19): una inscripción con su
 * práctica, su institución y su estado, o una práctica del plan que el
 * estudiante todavía no ha inscrito, que viene sin inscripción ni estado.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record HistorialDto(
    Integer ordenPractica, String nombrePractica, String objetivoGeneral,
    Long inscripcionId, String estado, String semestre, String estudiante, String documento,
    String institucion, LocalDate fechaInicio, LocalDate fechaFin, LocalDateTime enviadaEn,
    LocalDateTime avaladaEn, String motivoDevolucion
) {
    /** Una práctica del plan sin inscripción. */
    public static HistorialDto noInscrita(Practica p) {
        return new HistorialDto(p.getOrden(), p.getNombre(), p.getObjetivoGeneral(),
            null, null, null, null, null, null, null, null, null, null, null);
    }

    public static HistorialDto de(Inscripcion i) {
        InscripcionDatos d = i.getDatos();
        return new HistorialDto(i.getPractica().getOrden(), d.getNombrePractica(), d.getObjetivoGeneral(),
            i.getId(), i.getEstado().name(), i.getSemestre().getCodigo(),
            d.getNombres() + " " + d.getApellidos(), d.getTipoDocumento() + " " + d.getNumeroDocumento(),
            d.getInstitucionRazonSocial(), i.getFechaInicio(), i.getFechaFin(), i.getEnviadaEn(),
            i.getAval() == null ? null : i.getAval().getFecha(), i.motivoDevolucion());
    }
}
