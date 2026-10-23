package com.udi.geprac.academico.service;

import com.udi.geprac.academico.domain.Estudiante;
import com.udi.geprac.academico.domain.ExperienciaLaboral;
import com.udi.geprac.academico.domain.FormacionAcademica;
import com.udi.geprac.academico.domain.Programa;
import com.udi.geprac.academico.domain.ReferenciaPersonal;
import com.udi.geprac.academico.domain.Usuario;
import com.udi.geprac.academico.dto.BloquesDto;
import com.udi.geprac.academico.dto.CompletitudDto;
import com.udi.geprac.academico.dto.EntradaDto;
import com.udi.geprac.academico.dto.EstudianteDto;
import com.udi.geprac.academico.dto.HojaVidaDto;
import com.udi.geprac.academico.repository.EstudianteRepository;
import com.udi.geprac.academico.repository.ProgramaRepository;
import com.udi.geprac.academico.repository.UsuarioRepository;
import com.udi.geprac.academico.service.EstudianteService.Bloque;
import java.time.LocalDate;
import java.time.Year;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Las reglas de CU-01 sobre el perfil y la hoja de vida, sin base de datos:
 * el formulario vacío, los datos mínimos, el documento repetido, el programa
 * que no existe, el año futuro, las fechas de la experiencia, el tope de tres
 * referencias, la entrada que ya no existe y el indicador de completitud.
 *
 * @author Oscar Iván Blanco Díaz
 */
class EstudianteServiceTest {

    private static final UUID CUENTA = UUID.fromString("00000000-0000-0000-0000-0000000000e1");

    private final EstudianteRepository estudiantes = mock(EstudianteRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final ProgramaRepository programas = mock(ProgramaRepository.class);
    private final EstudianteService servicio = new EstudianteService(estudiantes, usuarios, programas);

    private final Usuario usuario = new Usuario(CUENTA, "José Rincón", "jrincon32@udi.edu.co");
    private final Programa lei = mock(Programa.class);

    @BeforeEach
    void ingresa() {
        Jwt token = Jwt.withTokenValue("t").header("alg", "ES256").subject(CUENTA.toString())
            .claim("email", "jrincon32@udi.edu.co").claim("rol", "ESTUDIANTE").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token));
        given(lei.getCodigo()).willReturn("LEI");
        given(lei.getNombre()).willReturn("Licenciatura en Educación Infantil");
        given(usuarios.findById(CUENTA)).willReturn(Optional.of(usuario));
        given(programas.findByCodigoIgnoreCase("LEI")).willReturn(Optional.of(lei));
        given(estudiantes.save(any(Estudiante.class))).willAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void cerrarSesion() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------------------------------------------------ datos personales

    @Test
    void sinRegistroPresentaElFormularioVacioConElCorreoDeLaCuenta() {
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.empty());

        EstudianteDto perfil = servicio.consultarPerfil();

        assertThat(perfil.id()).isNull();
        assertThat(perfil.correoInstitucional()).isEqualTo("jrincon32@udi.edu.co");
        assertThat(perfil.completitud().porcentaje()).isZero();
        assertThat(perfil.completitud().faltan()).hasSize(22);
    }

    @Test
    void elPrimerGuardadoCreaElRegistroConLosDatosMinimos() {
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.empty());

        EstudianteDto perfil = servicio.guardarPerfil(minimo("1098765432"));

        assertThat(perfil.nombres()).isEqualTo("José Fernando");
        assertThat(perfil.numeroDocumento()).isEqualTo("1.098.765.432");
        assertThat(perfil.nombrePrograma()).isEqualTo("Licenciatura en Educación Infantil");
        assertThat(perfil.correoInstitucional()).isEqualTo("jrincon32@udi.edu.co");
        assertThat(perfil.completitud().porcentaje()).isEqualTo(27);   // 6 de los 22 datos
        assertThat(perfil.completitud().faltan()).contains("Lugar de expedición", "Perfil profesional", "Referencias personales");
    }

    @Test
    void unDocumentoDeOtroEstudianteNoSeGuarda() {
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.empty());
        given(estudiantes.existsByTipoDocumentoAndNumeroDocumento("C.C.", "1.098.765.432")).willReturn(true);

        assertThatThrownBy(() -> servicio.guardarPerfil(minimo("1.098.765.432")))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> {
                assertThat(e.getCampo()).isEqualTo("numeroDocumento");
                assertThat(e.getEstado()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(e.getMessage()).contains("Dirección del Programa");
            });
        verify(estudiantes, never()).save(any());
    }

    @Test
    void conservarElMismoDocumentoNoSeTomaComoRepetido() {
        Estudiante actual = registro();
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.of(actual));

        EstudianteDto perfil = servicio.guardarPerfil(minimo("1098765432"));

        assertThat(perfil.numeroDocumento()).isEqualTo("1.098.765.432");
        verify(estudiantes, never()).existsByTipoDocumentoAndNumeroDocumento(anyString(), anyString());
    }

    @Test
    void unProgramaQueNoExisteNoSeGuarda() {
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.empty());
        given(programas.findByCodigoIgnoreCase("LEI")).willReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.guardarPerfil(minimo("1098765432")))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("codigoPrograma"));
        verify(estudiantes, never()).save(any());
    }

    // ------------------------------------------------------------------ hoja de vida

    @Test
    void laHojaDeVidaExigeLosDatosPersonales() {
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.empty());

        assertThatThrownBy(servicio::consultarHojaVida)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Mi perfil");
    }

    @Test
    void losDosBloquesSeGuardanSinEspaciosSobrantes() {
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.of(registro()));

        HojaVidaDto hoja = servicio.guardarBloques(new BloquesDto("  Estudiante de LEI.  ", "   "));

        assertThat(hoja.perfilProfesional()).isEqualTo("Estudiante de LEI.");
        assertThat(hoja.herramientasTrabajo()).isNull();
    }

    @Test
    void unaFormacionDeUnAnioFuturoNoSeGuarda() {
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.of(registro()));
        int siguiente = Year.now().getValue() + 1;

        assertThatThrownBy(() -> servicio.crearFormacion(new EntradaDto.Formacion(null, "Universitaria", "UDI", "LEI", siguiente)))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("anio"));
    }

    @Test
    void unaExperienciaQueTerminaAntesDeEmpezarNoSeGuarda() {
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.of(registro()));
        EntradaDto.Experiencia x = new EntradaDto.Experiencia(null, "Biblioteca", "Auxiliar", null, null, null,
            LocalDate.of(2024, 5, 1), LocalDate.of(2024, 2, 1), null, null);

        assertThatThrownBy(() -> servicio.crearExperiencia(x))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("fechaFin"));
    }

    @Test
    void unaCuartaReferenciaNoSeGuarda() {
        Estudiante e = registro();
        for (int i = 0; i < 3; i++) e.getReferencias().add(new ReferenciaPersonal(e));
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.of(e));

        assertThatThrownBy(() -> servicio.crearReferencia(
                new EntradaDto.Referencia(null, "Lic. Ana Ruiz", "UDI", "Docente", "300 000 0000", "Bucaramanga")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("tres referencias");
        assertThat(e.getReferencias()).hasSize(3);
    }

    @Test
    void eliminarUnaEntradaQueYaNoExisteRespondeQueNoExiste() {
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.of(registro()));

        assertThatThrownBy(() -> servicio.eliminarEntrada(Bloque.EXPERIENCIAS, 99L))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessageContaining("La experiencia laboral ya no existe");
    }

    @Test
    void eliminarUnaEntradaLaRetiraDeSuBloque() {
        Estudiante e = registro();
        FormacionAcademica f = new FormacionAcademica(e);
        ReflectionTestUtils.setField(f, "id", 7L);
        e.getFormaciones().add(f);
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.of(e));

        servicio.eliminarEntrada(Bloque.FORMACIONES, 7L);

        assertThat(e.getFormaciones()).isEmpty();
    }

    @Test
    void actualizarConDatosDeOtroBloqueNoSeAcepta() {
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.of(registro()));
        EntradaDto.Referencia r = new EntradaDto.Referencia(null, "Lic. Ana Ruiz", "UDI", "Docente", "300 000 0000", "Bucaramanga");

        assertThatThrownBy(() -> servicio.actualizarEntrada(Bloque.FORMACIONES, 1L, r))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lasFormacionesSeOrdenanPorTipoYAnio() {
        Estudiante e = registro();
        e.getFormaciones().add(formacion(e, "Universitaria", 2020));
        e.getFormaciones().add(formacion(e, "Otros", 2022));
        e.getFormaciones().add(formacion(e, "Secundaria", 2018));
        e.getFormaciones().add(formacion(e, "Universitaria", null));
        given(estudiantes.findByUsuarioId(CUENTA)).willReturn(Optional.of(e));

        assertThat(servicio.consultarHojaVida().formaciones())
            .extracting(f -> f.tipo() + " " + f.anio())
            .containsExactly("Secundaria 2018", "Universitaria null", "Universitaria 2020", "Otros 2022");
    }

    // ------------------------------------------------------------------ indicador y apoyo

    @Test
    void elPerfilCompletoLlegaAlCienPorCiento() {
        Estudiante e = registro();
        e.setLugarExpedicion("Bucaramanga");
        e.setFechaNacimiento(LocalDate.of(2003, 5, 20));
        e.setLugarNacimiento("Bucaramanga");
        e.setGenero("Masculino");
        e.setEstadoCivil("Soltero(a)");
        e.setEps("Nueva EPS");
        e.setDireccion("Calle 9 # 25-40");
        e.setBarrio("La Universidad");
        e.setCiudad("Bucaramanga");
        e.setTelefonoFijo("607 600 0001");
        e.setCelular("300 000 0001");
        e.setCorreoPersonal("jose.rincon@example.com");
        e.setPerfilProfesional("Estudiante de Licenciatura en Educación Infantil.");
        e.setHerramientasTrabajo("Procesador de texto y hoja de cálculo.");
        e.getFormaciones().add(formacion(e, "Secundaria", 2022));
        e.getReferencias().add(new ReferenciaPersonal(e));

        CompletitudDto c = EstudianteService.completitud(e);

        assertThat(c.porcentaje()).isEqualTo(100);
        assertThat(c.completo()).isTrue();
        assertThat(c.faltan()).isEmpty();
    }

    @Test
    void laExperienciaLaboralNoCuentaParaElIndicador() {
        Estudiante e = registro();
        int antes = EstudianteService.completitud(e).porcentaje();
        e.getExperiencias().add(new ExperienciaLaboral(e));

        assertThat(EstudianteService.completitud(e).porcentaje()).isEqualTo(antes);
    }

    @Test
    void elDocumentoLlevaLosPuntosDeMiles() {
        assertThat(EstudianteService.conPuntos("1000000001")).isEqualTo("1.000.000.001");
        assertThat(EstudianteService.conPuntos("12.345")).isEqualTo("12.345");
        assertThat(EstudianteService.conPuntos("98765")).isEqualTo("98.765");
    }

    @Test
    void elBloqueSeReconocePorSuRuta() {
        assertThat(Bloque.de("referencias")).isEqualTo(Bloque.REFERENCIAS);
        assertThatThrownBy(() -> Bloque.de("otros")).isInstanceOf(NoSuchElementException.class);
    }

    // ------------------------------------------------------------------ apoyo

    private static EstudianteDto minimo(String numero) {
        return new EstudianteDto(null, null, "José Fernando", "Rincón Barrios", "C.C.", numero, null, null, null,
            null, null, null, null, null, null, null, null, null, "LEI", null, 3, null);
    }

    /** Un registro guardado con los datos mínimos. */
    private Estudiante registro() {
        Estudiante e = new Estudiante(usuario);
        ReflectionTestUtils.setField(e, "id", 1L);
        e.setNombres("José Fernando");
        e.setApellidos("Rincón Barrios");
        e.setTipoDocumento("C.C.");
        e.setNumeroDocumento("1.098.765.432");
        e.setPrograma(lei);
        e.setSemestreActual(3);
        return e;
    }

    private static FormacionAcademica formacion(Estudiante e, String tipo, Integer anio) {
        FormacionAcademica f = new FormacionAcademica(e);
        f.setTipo(tipo);
        f.setInstitucion("Institución");
        f.setNombre("Título");
        f.setAnio(anio);
        return f;
    }
}
