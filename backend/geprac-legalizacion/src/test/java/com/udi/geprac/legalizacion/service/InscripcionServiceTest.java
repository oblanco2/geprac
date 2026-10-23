package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.ContactoInstitucion;
import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.domain.Inscripcion;
import com.udi.geprac.legalizacion.domain.Institucion;
import com.udi.geprac.legalizacion.domain.Practica;
import com.udi.geprac.legalizacion.domain.ResultadoRevision;
import com.udi.geprac.legalizacion.domain.Revision;
import com.udi.geprac.legalizacion.domain.Semestre;
import com.udi.geprac.legalizacion.dto.ExpedienteDto;
import com.udi.geprac.legalizacion.dto.InscripcionCambiosDto;
import com.udi.geprac.legalizacion.dto.InscripcionDto;
import com.udi.geprac.legalizacion.repository.InscripcionRepository;
import com.udi.geprac.legalizacion.repository.InstitucionRepository;
import com.udi.geprac.legalizacion.repository.PracticaRepository;
import com.udi.geprac.legalizacion.repository.SemestreRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static com.udi.geprac.legalizacion.Fabrica.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * Las reglas de CU-05 sin base de datos ni MS-01: la inscripción se crea una
 * sola vez por práctica y semestre, con la copia del expediente completo; el
 * segundo paso exige juntos el contacto y las fechas; solo se envía completa,
 * y solo el borrador y la devuelta admiten cambios.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class InscripcionServiceTest {

    private final IdentidadCliente identidad = mock(IdentidadCliente.class);
    private final SemestreRepository semestres = mock(SemestreRepository.class);
    private final PracticaRepository practicas = mock(PracticaRepository.class);
    private final InscripcionRepository inscripciones = mock(InscripcionRepository.class);
    private final InstitucionRepository instituciones = mock(InstitucionRepository.class);
    private final InscripcionService servicio =
        new InscripcionService(identidad, semestres, practicas, inscripciones, instituciones);

    private final Semestre abierto = semestre(2L, "2026-2", true);
    private final Practica tercera = practica(3L, 3);
    private final Institucion cerezos = institucion(5L, true, 50L, 51L);

    @BeforeEach
    void prepara() {
        ingresa(UUID.randomUUID(), "ESTUDIANTE", "programa", "LEI", "estudiante_id", 7L);
        given(semestres.findByAbiertoTrue()).willReturn(Optional.of(abierto));
        given(practicas.findByCodigoProgramaAndOrden("LEI", 3)).willReturn(Optional.of(tercera));
        given(inscripciones.saveAndFlush(any(Inscripcion.class))).willAnswer(x -> x.getArgument(0));
    }

    @AfterEach
    void cierra() {
        sale();
    }

    // ------------------------------------------------------------ iniciar (pasos 1 a 4)

    @Test
    void creaLaInscripcionEnBorradorConLaCopiaDelExpedienteYDeLaPractica() {
        given(identidad.consultarExpediente()).willReturn(expediente(true));
        given(inscripciones.findByEstudianteId(7L)).willReturn(List.of());

        InscripcionDto r = servicio.iniciarInscripcion();

        assertThat(r.estado()).isEqualTo("BORRADOR");
        assertThat(r.semestre()).isEqualTo("2026-2");
        assertThat(r.ordenPractica()).isEqualTo(3);
        assertThat(r.datos().nombres()).isEqualTo("Valeria");
        assertThat(r.datos().estadoCivil()).isEqualTo("Unión marital de hecho");
        assertThat(r.datos().semestreCursado()).isEqualTo(3);
        assertThat(r.datos().perfilProfesional()).startsWith("Estudiante de licenciatura");
        assertThat(r.datos().nombrePractica()).isEqualTo("Práctica pedagógica 3");
        assertThat(r.datos().institucionRazonSocial()).isNull();
        assertThat(r.objetivos()).containsExactly("Reconocer el contexto institucional.");
        assertThat(r.actividades()).containsExactly("Observar la jornada escolar.");
        assertThat(r.formaciones()).extracting(InscripcionDto.Formacion::nombre).containsExactly("Bachiller académico");
        assertThat(r.experiencias()).extracting(InscripcionDto.Experiencia::empresa).containsExactly("Jardín Mis Pequeños");
        assertThat(r.referencias()).extracting(InscripcionDto.Referencia::nombre).containsExactly("Lic. Carlos Gómez");
        assertThat(r.semestreInicio()).isEqualTo(LocalDate.of(2026, 7, 27));
        assertThat(r.semestreCierre()).isEqualTo(LocalDate.of(2026, 12, 4));
        then(inscripciones).should().saveAndFlush(argThat(i -> i.getEstudianteId().equals(7L)));
    }

    @Test
    void sinSemestreAbiertoNoSeCrea() {
        given(identidad.consultarExpediente()).willReturn(expediente(true));
        given(semestres.findByAbiertoTrue()).willReturn(Optional.empty());

        assertThatThrownBy(servicio::iniciarInscripcion).isInstanceOf(IllegalStateException.class)
            .hasMessageStartingWith("La Dirección del Programa todavía no ha abierto el semestre");
        then(inscripciones).should(never()).saveAndFlush(any());
    }

    @Test
    void sinLaPracticaDelSemestreQueCursaNoSeCrea() {
        given(identidad.consultarExpediente()).willReturn(expediente(true));
        given(practicas.findByCodigoProgramaAndOrden("LEI", 3)).willReturn(Optional.empty());

        assertThatThrownBy(servicio::iniciarInscripcion).isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("todavía no ha cargado la práctica 3 de su plan");
        then(inscripciones).should(never()).saveAndFlush(any());
    }

    @Test
    void conLaPracticaRetiradaDelCatalogoTampoco() {
        given(identidad.consultarExpediente()).willReturn(expediente(true));
        poner(tercera, "activa", false);
        given(inscripciones.findByEstudianteId(7L)).willReturn(List.of());

        assertThatThrownBy(servicio::iniciarInscripcion).isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("la tiene retirada del catálogo");
        then(inscripciones).should(never()).saveAndFlush(any());
    }

    @Test
    void conElRegistroIncompletoNoSeCreaYDiceLoQueFalta() {
        given(identidad.consultarExpediente()).willReturn(expediente(false, "Celular", "Referencias personales"));
        given(inscripciones.findByEstudianteId(7L)).willReturn(List.of());

        assertThatThrownBy(servicio::iniciarInscripcion).isInstanceOfSatisfying(RegistroIncompletoException.class,
            e -> assertThat(e.getFaltan()).containsExactly("Celular", "Referencias personales"));
        then(inscripciones).should(never()).saveAndFlush(any());
    }

    @Test
    void sinRegistroEnMs01FaltaTodoElPerfil() {
        ExpedienteDto.Perfil vacio = new ExpedienteDto.Perfil(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null,
            new ExpedienteDto.Completitud(0, false, List.of("Nombres", "Apellidos", "Programa")));
        given(identidad.consultarExpediente()).willReturn(new ExpedienteDto(vacio, null));

        assertThatThrownBy(servicio::iniciarInscripcion).isInstanceOfSatisfying(RegistroIncompletoException.class,
            e -> assertThat(e.getFaltan()).contains("Programa"));
        then(inscripciones).should(never()).saveAndFlush(any());
    }

    @Test
    void laYaIniciadaSeRetomaSinCopiarNadaDeNuevo() {
        Inscripcion borrador = inscripcion(40L, 7L, tercera, abierto, EstadoInscripcion.BORRADOR);
        given(identidad.consultarExpediente()).willReturn(expediente(true));
        given(inscripciones.findByEstudianteId(7L)).willReturn(List.of(
            inscripcion(39L, 7L, practica(2L, 2), semestre(1L, "2026-1", false), EstadoInscripcion.AVALADA), borrador));

        InscripcionDto r = servicio.iniciarInscripcion();

        assertThat(r.id()).isEqualTo(40L);
        assertThat(r.estado()).isEqualTo("BORRADOR");
        then(inscripciones).should(never()).saveAndFlush(any());
    }

    @Test
    void laYaEnviadaPresentaSuEstadoAunqueElRegistroHayaCambiado() {
        given(identidad.consultarExpediente()).willReturn(expediente(false, "Celular"));
        given(inscripciones.findByEstudianteId(7L)).willReturn(List.of(
            inscripcion(40L, 7L, tercera, abierto, EstadoInscripcion.ENVIADA)));

        assertThat(servicio.iniciarInscripcion().estado()).isEqualTo("ENVIADA");
        then(inscripciones).should(never()).saveAndFlush(any());
    }

    @Test
    void siOtraSesionLaCreoPrimeroNoSeCreaOtra() {
        given(identidad.consultarExpediente()).willReturn(expediente(true));
        given(inscripciones.findByEstudianteId(7L)).willReturn(List.of());
        given(inscripciones.saveAndFlush(any(Inscripcion.class)))
            .willThrow(new DataIntegrityViolationException("uq_inscripcion_periodo"));

        assertThatThrownBy(servicio::iniciarInscripcion).isInstanceOf(IllegalStateException.class)
            .hasMessageStartingWith("La inscripción ya se había iniciado desde otra sesión");
    }

    // ------------------------------------------------------------ actualizar (pasos 6 a 11)

    @Test
    void elPrimerPasoGuardaLaInstitucionYCopiaSusDatos() {
        borrador(EstadoInscripcion.BORRADOR);
        given(instituciones.findById(5L)).willReturn(Optional.of(cerezos));

        InscripcionDto r = servicio.actualizarInscripcion(40L, institucionEscogida(5L));

        assertThat(r.institucionId()).isEqualTo(5L);
        assertThat(r.datos().institucionRazonSocial()).isEqualTo("Jardín Los Cerezos");
        assertThat(r.datos().institucionNit()).isEqualTo("9005");
        assertThat(r.contactoId()).isNull();
        assertThat(r.datos().contactoNombre()).isNull();
        assertThat(r.estado()).isEqualTo("BORRADOR");
    }

    @Test
    void otraInstitucionQuitaElContactoEscogido() {
        Institucion americas = institucion(4L, true, 40L);
        Inscripcion i = borrador(EstadoInscripcion.BORRADOR);
        poner(i, "institucion", americas);
        poner(i, "contacto", americas.getContactos().get(0));
        given(instituciones.findById(5L)).willReturn(Optional.of(cerezos));

        InscripcionDto r = servicio.actualizarInscripcion(40L, institucionEscogida(5L));

        assertThat(r.institucionId()).isEqualTo(5L);
        assertThat(r.contactoId()).isNull();
        assertThat(r.datos().contactoNombre()).isNull();
    }

    @Test
    void laMismaInstitucionConservaElContactoAunqueLaHayanRetirado() {
        Institucion retirada = institucion(6L, false, 60L);
        Inscripcion i = borrador(EstadoInscripcion.BORRADOR);
        poner(i, "institucion", retirada);
        poner(i, "contacto", retirada.getContactos().get(0));
        given(instituciones.findById(6L)).willReturn(Optional.of(retirada));

        InscripcionDto r = servicio.actualizarInscripcion(40L, institucionEscogida(6L));

        assertThat(r.institucionId()).isEqualTo(6L);
        assertThat(r.contactoId()).isEqualTo(60L);
    }

    @Test
    void unaInstitucionRetiradaNoSePuedeEscoger() {
        borrador(EstadoInscripcion.BORRADOR);
        given(instituciones.findById(6L)).willReturn(Optional.of(institucion(6L, false, 60L)));

        assertThatThrownBy(() -> servicio.actualizarInscripcion(40L, institucionEscogida(6L)))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("institucionId"));
    }

    @Test
    void elSegundoPasoGuardaElContactoYLasFechasConSusDatos() {
        poner(borrador(EstadoInscripcion.BORRADOR), "institucion", cerezos);

        InscripcionDto r = servicio.actualizarInscripcion(40L,
            escenario(51L, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 11, 27)));

        assertThat(r.contactoId()).isEqualTo(51L);
        assertThat(r.fechaInicio()).isEqualTo(LocalDate.of(2026, 8, 3));
        assertThat(r.fechaFin()).isEqualTo(LocalDate.of(2026, 11, 27));
        assertThat(r.datos().contactoNombre()).isEqualTo("Contacto 51");
        assertThat(r.datos().institucionRazonSocial()).isEqualTo("Jardín Los Cerezos");
    }

    @Test
    void sinUnaFechaNoSeGuardaElPasoYSeSenalanLasQueFaltan() {
        poner(borrador(EstadoInscripcion.BORRADOR), "institucion", cerezos);

        assertThatThrownBy(() -> servicio.actualizarInscripcion(40L, escenario(51L, null, null)))
            .isInstanceOfSatisfying(CampoInvalidoException.class,
                e -> assertThat(e.getCampos()).containsOnlyKeys("fechaInicio", "fechaFin"));
    }

    @Test
    void sinElContactoTampoco() {
        poner(borrador(EstadoInscripcion.BORRADOR), "institucion", cerezos);

        assertThatThrownBy(() -> servicio.actualizarInscripcion(40L,
                escenario(null, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 11, 27))))
            .isInstanceOfSatisfying(CampoInvalidoException.class,
                e -> assertThat(e.getCampos()).containsOnlyKeys("contactoId"));
    }

    @Test
    void laFechaDeFinalizacionDebeSerPosteriorALaDeInicio() {
        poner(borrador(EstadoInscripcion.BORRADOR), "institucion", cerezos);

        assertThatThrownBy(() -> servicio.actualizarInscripcion(40L,
                escenario(51L, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 5))))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampos())
                .containsExactly(java.util.Map.entry("fechaFin", "La fecha de finalización debe ser posterior a la de inicio.")));
    }

    @Test
    void lasFechasDebenQuedarDentroDelSemestre() {
        poner(borrador(EstadoInscripcion.BORRADOR), "institucion", cerezos);

        assertThatThrownBy(() -> servicio.actualizarInscripcion(40L,
                escenario(51L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 20))))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampos())
                .containsEntry("fechaInicio", "La fecha debe quedar dentro del semestre 2026-2: del 27/07/2026 al 04/12/2026.")
                .containsKey("fechaFin"));
    }

    @Test
    void elContactoDebeSerDeLaInstitucionEscogida() {
        poner(borrador(EstadoInscripcion.BORRADOR), "institucion", cerezos);

        assertThatThrownBy(() -> servicio.actualizarInscripcion(40L,
                escenario(99L, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 11, 27))))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("contactoId"));
    }

    @Test
    void sinInstitucionNoSeEscogeContacto() {
        borrador(EstadoInscripcion.BORRADOR);

        assertThatThrownBy(() -> servicio.actualizarInscripcion(40L,
                escenario(51L, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 11, 27))))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("institucionId"));
    }

    @Test
    void elTercerPasoAjustaSusDatosSoloEnLaInscripcion() {
        borrador(EstadoInscripcion.BORRADOR);

        InscripcionDto r = servicio.actualizarInscripcion(40L, new InscripcionCambiosDto(null, null, null, null,
            " Carrera 27 # 10-20 ", "3110000000", "valeria.nueva@correo.com"));

        assertThat(r.datos().direccion()).isEqualTo("Carrera 27 # 10-20");
        assertThat(r.datos().celular()).isEqualTo("3110000000");
        assertThat(r.datos().correoPersonal()).isEqualTo("valeria.nueva@correo.com");
        then(identidad).shouldHaveNoInteractions();
    }

    @Test
    void laDevueltaSigueDevueltaAlGuardar() {
        borrador(EstadoInscripcion.DEVUELTA);

        InscripcionDto r = servicio.actualizarInscripcion(40L, new InscripcionCambiosDto(null, null, null, null,
            "Carrera 27 # 10-20", null, null));

        assertThat(r.estado()).isEqualTo("DEVUELTA");
    }

    @Test
    void unaEnviadaNoAdmiteCambios() {
        borrador(EstadoInscripcion.ENVIADA);

        assertThatThrownBy(() -> servicio.actualizarInscripcion(40L, institucionEscogida(5L)))
            .isInstanceOf(IllegalStateException.class).hasMessageEndingWith("Está enviada.");
    }

    @Test
    void laDeOtroEstudianteNoExisteParaEl() {
        given(inscripciones.findById(41L)).willReturn(Optional.of(
            inscripcion(41L, 8L, tercera, abierto, EstadoInscripcion.BORRADOR)));

        assertThatThrownBy(() -> servicio.actualizarInscripcion(41L, institucionEscogida(5L)))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void conElSemestreCerradoNoAdmiteCambios() {
        given(inscripciones.findById(40L)).willReturn(Optional.of(
            inscripcion(40L, 7L, tercera, semestre(1L, "2026-1", false), EstadoInscripcion.BORRADOR)));

        assertThatThrownBy(() -> servicio.actualizarInscripcion(40L, institucionEscogida(5L)))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("2026-1 ya está cerrado");
    }

    // ------------------------------------------------------------ enviar (pasos 12 a 15)

    @Test
    void laCompletaPasaALaBandejaDelTutorConLaFechaDelEnvio() {
        Inscripcion i = completa(EstadoInscripcion.BORRADOR);

        InscripcionDto r = servicio.enviarARevision(40L);

        assertThat(r.estado()).isEqualTo("ENVIADA");
        assertThat(r.enviadaEn()).isNotNull();
        assertThat(i.getEstado()).isEqualTo(EstadoInscripcion.ENVIADA);
    }

    @Test
    void laIncompletaNoSeEnviaYDiceLoQueFalta() {
        Inscripcion i = borrador(EstadoInscripcion.BORRADOR);
        poner(i, "fechaFin", null);

        assertThatThrownBy(() -> servicio.enviarARevision(40L)).isInstanceOfSatisfying(RegistroIncompletoException.class,
            e -> assertThat(e.getFaltan())
                .containsExactly("la institución receptora", "el contacto del escenario", "las fechas de la práctica"));
        assertThat(i.getEstado()).isEqualTo(EstadoInscripcion.BORRADOR);
    }

    @Test
    void laYaEnviadaNoSeEnviaDeNuevo() {
        completa(EstadoInscripcion.ENVIADA);

        assertThatThrownBy(() -> servicio.enviarARevision(40L)).isInstanceOf(IllegalStateException.class)
            .hasMessageStartingWith("La inscripción ya fue enviada al tutor académico");
        then(inscripciones).should(never()).saveAndFlush(any());
    }

    @Test
    void laDevueltaSeReenviaConLaFechaDelReenvio() {
        Inscripcion i = completa(EstadoInscripcion.DEVUELTA);
        LocalDateTime primerEnvio = i.getEnviadaEn();

        InscripcionDto r = servicio.enviarARevision(40L);

        assertThat(r.estado()).isEqualTo("ENVIADA");
        assertThat(r.enviadaEn()).isAfter(primerEnvio);
    }

    // ------------------------------------------------------------ estado (variación 16.1)

    @Test
    void elEstadoEsElDeLaInscripcionDelSemestreAbiertoConElMotivo() {
        Inscripcion devuelta = inscripcion(40L, 7L, tercera, abierto, EstadoInscripcion.DEVUELTA);
        devuelta.getRevisiones().add(new Revision(devuelta, UUID.randomUUID(), ResultadoRevision.DEVUELTA,
            "Ajuste las fechas al calendario de la institución."));
        given(inscripciones.findByEstudianteId(7L)).willReturn(List.of(
            inscripcion(39L, 7L, practica(2L, 2), semestre(1L, "2026-1", false), EstadoInscripcion.AVALADA), devuelta));

        InscripcionDto r = servicio.consultarEstado();

        assertThat(r.id()).isEqualTo(40L);
        assertThat(r.motivoDevolucion()).isEqualTo("Ajuste las fechas al calendario de la institución.");
    }

    @Test
    void sinInscripcionEnElSemestreLoDice() {
        given(inscripciones.findByEstudianteId(7L)).willReturn(List.of(
            inscripcion(39L, 7L, practica(2L, 2), semestre(1L, "2026-1", false), EstadoInscripcion.AVALADA)));

        assertThatThrownBy(servicio::consultarEstado).isInstanceOf(NoSuchElementException.class)
            .hasMessageStartingWith("No tiene inscripción en el semestre 2026-2");
    }

    @Test
    void sinPerfilNoHayInscripcionQueConsultar() {
        ingresa(UUID.randomUUID(), "ESTUDIANTE", "programa", "LEI");

        assertThatThrownBy(servicio::consultarEstado).isInstanceOf(IllegalStateException.class)
            .hasMessageStartingWith("Todavía no ha registrado su perfil");
    }

    // ------------------------------------------------------------------ apoyo

    /** La inscripción 40 del estudiante 7 en el semestre abierto, sin institución ni contacto escogidos. */
    private Inscripcion borrador(EstadoInscripcion estado) {
        Inscripcion i = inscripcion(40L, 7L, tercera, abierto, estado);
        given(inscripciones.findById(40L)).willReturn(Optional.of(i));
        return i;
    }

    /** La inscripción 40 con la institución, el contacto y las fechas. */
    private Inscripcion completa(EstadoInscripcion estado) {
        Inscripcion i = borrador(estado);
        poner(i, "institucion", cerezos);
        return poner(i, "contacto", cerezos.getContactos().get(0));
    }

    private static Institucion institucion(long id, boolean activa, long... contactos) {
        Institucion i = poner(new Institucion(), "id", id);
        i.setRazonSocial(id == 5L ? "Jardín Los Cerezos" : "Institución " + id);
        i.setNit("900" + id);
        i.setDireccion("Calle " + id + " # 10-20");
        i.setCiudad("Bucaramanga");
        i.setTelefono("607634120" + id);
        i.setRepresentanteLegal("Rosa Elena Díaz");
        i.setActiva(activa);
        for (long c : contactos) {
            ContactoInstitucion x = poner(new ContactoInstitucion(i), "id", c);
            x.setNombre("Contacto " + c);
            x.setCargo("Coordinadora académica");
            x.setTelefono("60763412" + c);
            i.getContactos().add(x);
        }
        return i;
    }

    private static InscripcionCambiosDto institucionEscogida(Long id) {
        return new InscripcionCambiosDto(id, null, null, null, null, null, null);
    }

    private static InscripcionCambiosDto escenario(Long contacto, LocalDate inicio, LocalDate fin) {
        return new InscripcionCambiosDto(null, contacto, inicio, fin, null, null, null);
    }

    /** El expediente de Valeria como lo responde MS-01: completo, o con lo que le falta y sin hoja de vida. */
    private static ExpedienteDto expediente(boolean completo, String... faltan) {
        ExpedienteDto.Perfil p = new ExpedienteDto.Perfil(7L, "Valeria", "Cruz Rangel", "C.C.", "1099331208",
            "Bucaramanga", LocalDate.of(2004, 3, 12), "Bucaramanga", "Femenino", "Unión marital de hecho", "Nueva EPS",
            "Calle 45 # 27-18", "Cabecera", "Bucaramanga", "6076000000", "3001234567", "valeria.cruz@correo.com",
            "LEI", "Licenciatura en Educación Infantil", 3,
            new ExpedienteDto.Completitud(completo ? 100 : 91, completo, List.of(faltan)));
        if (!completo) return new ExpedienteDto(p, null);
        return new ExpedienteDto(p, new ExpedienteDto.HojaVida(
            "Estudiante de licenciatura con interés en la primera infancia.", "Ofimática y recursos didácticos digitales.",
            List.of(new ExpedienteDto.Formacion("Secundaria", "Colegio Santander", "Bachiller académico", 2021)),
            List.of(new ExpedienteDto.Experiencia("Jardín Mis Pequeños", "Auxiliar pedagógica", "Ana Ruiz", "Coordinadora",
                "6076000000", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30), "Apoyo en actividades lúdicas.", null)),
            List.of(new ExpedienteDto.Referencia("Lic. Carlos Gómez", "Colegio Santander", "Docente", "3009876543",
                "Bucaramanga"))));
    }
}
