package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.FormatoGenerado;
import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.PlantillaFormato;
import com.udi.geprac.legalizacion.domain.TipoFormato;
import com.udi.geprac.legalizacion.dto.FormatoDto;
import com.udi.geprac.legalizacion.formatos.DatosFormato;
import com.udi.geprac.legalizacion.formatos.Plantilla;
import com.udi.geprac.legalizacion.repository.FormatoGeneradoRepository;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import com.udi.geprac.legalizacion.repository.PlantillaFormatoRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-08 · Emitir los formatos institucionales. El estudiante obtiene sus
 * cuatro formatos —PR-01, PR-02, PR-04 y PR-05— diligenciados desde la
 * plantilla vigente de cada uno con la copia que guarda su inscripción, y
 * únicamente cuando la inscripción está avalada.
 *
 * Los cuatro se diligencian antes de registrar la emisión: si uno no se puede
 * generar, no queda una emisión parcial (RNF-08). Cada emisión guarda su
 * plantilla y su fecha; con ellas y con la copia de la inscripción, que después
 * del aval ya no cambia, la descarga entrega el documento tal como se emitió.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Service
@Transactional
public class FormatoService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final InscripcionRepository inscripciones;
    private final PlantillaFormatoRepository plantillas;
    private final FormatoGeneradoRepository formatos;
    private final Map<String, Plantilla> leidas = new ConcurrentHashMap<>();

    public FormatoService(InscripcionRepository inscripciones, PlantillaFormatoRepository plantillas,
                          FormatoGeneradoRepository formatos) {
        this.inscripciones = inscripciones;
        this.plantillas = plantillas;
        this.formatos = formatos;
    }

    /** Pasos 2 y 3: los cuatro formatos de la inscripción avalada, con su estado de emisión. */
    @Transactional(readOnly = true)
    public List<FormatoDto> consultarFormatos(Long inscripcionId) {
        Inscripcion i = propiaYAvalada(inscripcionId);
        Map<TipoFormato, FormatoGenerado> emitidos = emitidos(i);
        List<FormatoDto> r = new ArrayList<>();
        for (TipoFormato t : TipoFormato.values()) r.add(FormatoDto.de(t, emitidos.get(t)));
        return r;
    }

    /** Pasos 4 a 8 y variación 4.1: diligencia los cuatro con su plantilla vigente y registra su emisión. */
    public List<FormatoDto> emitirFormatos(Long inscripcionId) {
        Inscripcion i = propiaYAvalada(inscripcionId);
        Map<TipoFormato, PlantillaFormato> vigentes = new EnumMap<>(TipoFormato.class);
        plantillas.findByVigenteTrue().forEach(p -> vigentes.put(p.getTipo(), p));
        for (TipoFormato t : TipoFormato.values())
            if (!vigentes.containsKey(t))
                throw new IllegalStateException("No hay plantilla vigente del formato " + t.codigo() + " · " + t.nombre()
                    + ": la Dirección del Programa debe ponerla en vigencia. No se emitió ningún formato.");

        LocalDateTime ahora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        // primero se diligencian los cuatro; si uno falla, no se registra ninguno
        for (TipoFormato t : TipoFormato.values()) diligenciar(i, vigentes.get(t), ahora);

        Map<TipoFormato, FormatoGenerado> emitidos = emitidos(i);
        List<FormatoGenerado> lista = new ArrayList<>();
        for (TipoFormato t : TipoFormato.values()) {
            FormatoGenerado f = emitidos.getOrDefault(t, new FormatoGenerado(i, t));
            f.emitir(vigentes.get(t), nombreDelArchivo(t, i), ahora);
            lista.add(f);
        }
        List<FormatoGenerado> guardados = formatos.saveAll(lista);
        formatos.flush();
        return guardados.stream().map(f -> FormatoDto.de(f.getTipo(), f)).toList();
    }

    /** Variación 3.1: el documento tal como fue emitido, con su plantilla y su fecha de emisión. */
    @Transactional(readOnly = true)
    public byte[] descargarFormato(Long formatoId) {
        FormatoGenerado f = formatos.findById(formatoId)
            .filter(x -> x.getInscripcion().getEstudianteId().equals(UsuarioActual.estudianteId()))
            .orElseThrow(() -> new NoSuchElementException("El formato no existe entre los suyos."));
        Inscripcion i = propiaYAvalada(f.getInscripcion().getId());
        return diligenciar(i, f.getPlantilla(), f.getFechaEmision());
    }

    // ----------------------------------------------------------------- apoyo

    /** Excepciones 2.1 y 2.2: la inscripción debe ser del estudiante del token y estar avalada. */
    private Inscripcion propiaYAvalada(Long inscripcionId) {
        Long estudiante = UsuarioActual.estudianteId();
        Inscripcion i = inscripciones.findById(inscripcionId)
            .filter(x -> x.getEstudianteId().equals(estudiante))
            .orElseThrow(() -> new NoSuchElementException("La inscripción no está entre las suyas."));
        if (i.getEstado() != EstadoInscripcion.AVALADA)
            throw new IllegalStateException("Los formatos se habilitan cuando la Dirección del Programa avale la "
                + "inscripción; hoy está «" + i.getEstado().name().toLowerCase(Locale.ROOT) + "».");
        return i;
    }

    private Map<TipoFormato, FormatoGenerado> emitidos(Inscripcion i) {
        Map<TipoFormato, FormatoGenerado> m = new EnumMap<>(TipoFormato.class);
        formatos.findByInscripcionId(i.getId()).forEach(f -> m.put(f.getTipo(), f));
        return m;
    }

    /** Paso 6: la plantilla diligenciada con la copia de la inscripción, en PDF. */
    private byte[] diligenciar(Inscripcion i, PlantillaFormato p, LocalDateTime emision) {
        Plantilla plantilla = leidas.computeIfAbsent(p.getArchivo(), FormatoService::leer);
        String pie = "Emitido por GEPRAC el " + emision.toLocalDate().format(FECHA) + " con la plantilla "
            + p.getTipo().codigo() + " v" + p.getVersion();
        return plantilla.diligenciar(DatosFormato.de(i, emision), p.getVersion(), pie);
    }

    private static Plantilla leer(String archivo) {
        try (InputStream in = new ClassPathResource(archivo).getInputStream()) {
            return Plantilla.leer(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la plantilla " + archivo
                + ": no se emitió ningún formato. Intente de nuevo; si persiste, avise a la Dirección del Programa.");
        }
    }

    /** El nombre con que se entrega el documento: el formato, el documento del estudiante y el semestre. */
    private static String nombreDelArchivo(TipoFormato t, Inscripcion i) {
        return t.codigo() + "_" + i.getDatos().getNumeroDocumento() + "_" + i.getSemestre().getCodigo() + ".pdf";
    }
}
