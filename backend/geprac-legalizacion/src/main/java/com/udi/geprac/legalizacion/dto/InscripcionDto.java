package com.udi.geprac.legalizacion.dto;

import com.udi.geprac.legalizacion.domain.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Una inscripción con su estado y la copia completa del expediente: es lo que
 * presentan la revisión del tutor (P-10 y P-11) y el aval de la Dirección
 * (P-17 y P-18). Trae sus revisiones y su aval, con quién los hizo y cuándo,
 * y si está devuelta, el motivo.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record InscripcionDto(
    Long id, String estado, String semestre, Long practicaId, Integer ordenPractica,
    LocalDate fechaInicio, LocalDate fechaFin, LocalDateTime creadaEn, LocalDateTime enviadaEn,
    String motivoDevolucion, DatosDto datos, List<String> objetivos, List<String> actividades,
    List<Formacion> formaciones, List<Experiencia> experiencias, List<Referencia> referencias,
    List<RevisionResumen> revisiones, AvalResumen aval
) {
    /** Una entrada de formación académica copiada de la hoja de vida. */
    public record Formacion(String tipo, String institucion, String nombre, Integer anio) { }

    /** Una entrada de experiencia laboral copiada de la hoja de vida. */
    public record Experiencia(String empresa, String cargo, String jefeInmediato, String cargoJefe,
                              String telefonoEmpresa, LocalDate fechaInicio, LocalDate fechaFin,
                              String funciones, String logros) { }

    /** Una referencia personal copiada de la hoja de vida. */
    public record Referencia(String nombre, String empresa, String cargo, String telefono, String ciudad) { }

    /** Una revisión del tutor: quién la hizo, su resultado, su motivo si la devolvió, y su fecha. */
    public record RevisionResumen(UUID revisorId, String resultado, String motivo, LocalDateTime fecha) { }

    /** El aval de la Dirección: quién lo dio y cuándo. */
    public record AvalResumen(UUID directorId, LocalDateTime fecha) { }

    public static InscripcionDto de(Inscripcion i) {
        return new InscripcionDto(i.getId(), i.getEstado().name(), i.getSemestre().getCodigo(),
            i.getPractica().getId(), i.getPractica().getOrden(),
            i.getFechaInicio(), i.getFechaFin(), i.getCreadaEn(), i.getEnviadaEn(), i.motivoDevolucion(),
            DatosDto.de(i.getDatos()),
            i.getObjetivos().stream().map(InscripcionObjetivo::getTexto).toList(),
            i.getActividades().stream().map(InscripcionActividad::getTexto).toList(),
            i.getFormaciones().stream().map(f -> new Formacion(f.getTipo(), f.getInstitucion(), f.getNombre(), f.getAnio())).toList(),
            i.getExperiencias().stream().map(x -> new Experiencia(x.getEmpresa(), x.getCargo(), x.getJefeInmediato(),
                x.getCargoJefe(), x.getTelefonoEmpresa(), x.getFechaInicio(), x.getFechaFin(), x.getFunciones(), x.getLogros())).toList(),
            i.getReferencias().stream().map(r -> new Referencia(r.getNombre(), r.getEmpresa(), r.getCargo(),
                r.getTelefono(), r.getCiudad())).toList(),
            i.getRevisiones().stream().map(r -> new RevisionResumen(r.getRevisorId(), r.getResultado().name(),
                r.getMotivo(), r.getFecha())).toList(),
            i.getAval() == null ? null : new AvalResumen(i.getAval().getDirectorId(), i.getAval().getFecha()));
    }
}
