-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · MS-01 Identidad y Perfil Académico
-- Esquema identidad: lo permanente del estudiante —usuarios, programas,
-- estudiantes y hoja de vida—. Seis tablas.
--
-- Implementa el modelo de datos del segundo avance (punto 8: modelo
-- relacional y diccionario de datos). Cada tabla y cada campo llevan como
-- comentario su descripción del diccionario.
--
-- No hay claves foráneas hacia el esquema auth de Supabase: usuario.id es
-- el identificador que asigna el proveedor de identidad y se guarda como
-- valor, igual que los identificadores que cruzan entre microservicios.
--
-- Autor: Darien Asdrwal Pesca Ojeda · modelo de datos
-- ══════════════════════════════════════════════════════════════════

CREATE TABLE identidad.usuario (
    id                    uuid         NOT NULL,
    nombre_presentacion   varchar(150) NOT NULL,
    correo_institucional  varchar(150) NOT NULL,
    rol                   varchar(20),
    creado_en             timestamp    NOT NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_correo_institucional UNIQUE (correo_institucional)
);

COMMENT ON TABLE identidad.usuario IS 'Cuenta con la que una persona ingresa al software. La crea el proveedor de identidad institucional en el primer ingreso.';
COMMENT ON COLUMN identidad.usuario.id IS 'Identificador que asigna el proveedor de identidad';
COMMENT ON COLUMN identidad.usuario.nombre_presentacion IS 'Nombre con el que el software identifica al usuario en pantalla';
COMMENT ON COLUMN identidad.usuario.correo_institucional IS 'Correo institucional con el que ingresa';
COMMENT ON COLUMN identidad.usuario.rol IS 'DIRECTOR, TUTOR o ESTUDIANTE. Nulo mientras la Dirección no lo asigne';
COMMENT ON COLUMN identidad.usuario.creado_en IS 'Fecha del primer ingreso';

CREATE TABLE identidad.programa (
    codigo  varchar(10)  NOT NULL,
    nombre  varchar(150) NOT NULL,
    CONSTRAINT pk_programa PRIMARY KEY (codigo)
);

COMMENT ON TABLE identidad.programa IS 'Programas académicos de licenciatura. Dato de referencia del microservicio de identidad.';
COMMENT ON COLUMN identidad.programa.codigo IS 'Código institucional del programa';
COMMENT ON COLUMN identidad.programa.nombre IS 'Nombre del programa';

CREATE TABLE identidad.estudiante (
    id                    bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    usuario_id            uuid         NOT NULL,
    nombres               varchar(100) NOT NULL,
    apellidos             varchar(100) NOT NULL,
    tipo_documento        varchar(4)   NOT NULL,
    numero_documento      varchar(20)  NOT NULL,
    lugar_expedicion      varchar(80),
    fecha_nacimiento      date,
    lugar_nacimiento      varchar(80),
    genero                varchar(10),
    estado_civil          varchar(20),
    eps                   varchar(80),
    direccion             varchar(150),
    barrio                varchar(80),
    ciudad                varchar(80),
    telefono_fijo         varchar(20),
    celular               varchar(20),
    correo_personal       varchar(150),
    codigo_programa       varchar(10)  NOT NULL,
    semestre_actual       smallint     NOT NULL,
    perfil_profesional    text,
    herramientas_trabajo  text,
    creado_en             timestamp    NOT NULL,
    actualizado_en        timestamp,
    CONSTRAINT pk_estudiante PRIMARY KEY (id),
    CONSTRAINT uq_estudiante_usuario UNIQUE (usuario_id),
    CONSTRAINT uq_estudiante_documento UNIQUE (tipo_documento, numero_documento),
    CONSTRAINT fk_estudiante_usuario FOREIGN KEY (usuario_id) REFERENCES identidad.usuario (id),
    CONSTRAINT fk_estudiante_programa FOREIGN KEY (codigo_programa) REFERENCES identidad.programa (codigo)
);

COMMENT ON TABLE identidad.estudiante IS 'Registro único y persistente del estudiante practicante, con sus datos personales y los dos bloques narrativos de la hoja de vida.';
COMMENT ON COLUMN identidad.estudiante.id IS 'Identificador del estudiante';
COMMENT ON COLUMN identidad.estudiante.usuario_id IS 'Cuenta con la que ingresa. Referencia a usuario';
COMMENT ON COLUMN identidad.estudiante.nombres IS 'Nombres, separados de los apellidos porque PR-01 y PR-04 lo exigen';
COMMENT ON COLUMN identidad.estudiante.apellidos IS 'Apellidos';
COMMENT ON COLUMN identidad.estudiante.tipo_documento IS 'T.I., C.C. o C.E.';
COMMENT ON COLUMN identidad.estudiante.numero_documento IS 'Número del documento de identidad';
COMMENT ON COLUMN identidad.estudiante.lugar_expedicion IS 'Ciudad de expedición del documento';
COMMENT ON COLUMN identidad.estudiante.fecha_nacimiento IS 'Fecha de nacimiento';
COMMENT ON COLUMN identidad.estudiante.lugar_nacimiento IS 'Ciudad de nacimiento';
COMMENT ON COLUMN identidad.estudiante.genero IS 'Masculino o Femenino, que son las opciones de PR-04';
COMMENT ON COLUMN identidad.estudiante.estado_civil IS 'Estado civil';
COMMENT ON COLUMN identidad.estudiante.eps IS 'EPS vigente';
COMMENT ON COLUMN identidad.estudiante.direccion IS 'Dirección de residencia';
COMMENT ON COLUMN identidad.estudiante.barrio IS 'Barrio de residencia';
COMMENT ON COLUMN identidad.estudiante.ciudad IS 'Ciudad de residencia';
COMMENT ON COLUMN identidad.estudiante.telefono_fijo IS 'Teléfono fijo';
COMMENT ON COLUMN identidad.estudiante.celular IS 'Teléfono celular';
COMMENT ON COLUMN identidad.estudiante.correo_personal IS 'Correo personal, que es el que imprimen los formatos';
COMMENT ON COLUMN identidad.estudiante.codigo_programa IS 'Programa al que pertenece. Referencia a programa';
COMMENT ON COLUMN identidad.estudiante.semestre_actual IS 'Semestre que cursa, de 1 a 8';
COMMENT ON COLUMN identidad.estudiante.perfil_profesional IS 'Perfil profesional redactado en tercera persona';
COMMENT ON COLUMN identidad.estudiante.herramientas_trabajo IS 'Programas y recursos que domina';
COMMENT ON COLUMN identidad.estudiante.creado_en IS 'Fecha de creación del registro';
COMMENT ON COLUMN identidad.estudiante.actualizado_en IS 'Fecha de la última modificación';

CREATE TABLE identidad.formacion_academica (
    id             bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    estudiante_id  bigint       NOT NULL,
    tipo           varchar(15)  NOT NULL,
    institucion    varchar(150) NOT NULL,
    nombre         varchar(150) NOT NULL,
    anio           smallint,
    CONSTRAINT pk_formacion_academica PRIMARY KEY (id),
    CONSTRAINT fk_formacion_academica_estudiante FOREIGN KEY (estudiante_id) REFERENCES identidad.estudiante (id)
);

COMMENT ON TABLE identidad.formacion_academica IS 'Entradas de formación académica de la hoja de vida.';
COMMENT ON COLUMN identidad.formacion_academica.id IS 'Identificador de la entrada';
COMMENT ON COLUMN identidad.formacion_academica.estudiante_id IS 'Estudiante dueño de la entrada';
COMMENT ON COLUMN identidad.formacion_academica.tipo IS 'Secundaria, Universitaria u Otros';
COMMENT ON COLUMN identidad.formacion_academica.institucion IS 'Institución donde cursó';
COMMENT ON COLUMN identidad.formacion_academica.nombre IS 'Nombre del título o del programa';
COMMENT ON COLUMN identidad.formacion_academica.anio IS 'Año de terminación. Vacío cuando está en curso';

CREATE TABLE identidad.experiencia_laboral (
    id                bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    estudiante_id     bigint       NOT NULL,
    empresa           varchar(150) NOT NULL,
    cargo             varchar(100) NOT NULL,
    jefe_inmediato    varchar(150),
    cargo_jefe        varchar(100),
    telefono_empresa  varchar(20),
    fecha_inicio      date         NOT NULL,
    fecha_fin         date,
    funciones         text,
    logros            text,
    CONSTRAINT pk_experiencia_laboral PRIMARY KEY (id),
    CONSTRAINT fk_experiencia_laboral_estudiante FOREIGN KEY (estudiante_id) REFERENCES identidad.estudiante (id)
);

COMMENT ON TABLE identidad.experiencia_laboral IS 'Entradas de experiencia laboral de la hoja de vida.';
COMMENT ON COLUMN identidad.experiencia_laboral.id IS 'Identificador de la entrada';
COMMENT ON COLUMN identidad.experiencia_laboral.estudiante_id IS 'Estudiante dueño de la entrada';
COMMENT ON COLUMN identidad.experiencia_laboral.empresa IS 'Nombre de la empresa';
COMMENT ON COLUMN identidad.experiencia_laboral.cargo IS 'Cargo desempeñado';
COMMENT ON COLUMN identidad.experiencia_laboral.jefe_inmediato IS 'Nombre del jefe inmediato';
COMMENT ON COLUMN identidad.experiencia_laboral.cargo_jefe IS 'Cargo del jefe inmediato';
COMMENT ON COLUMN identidad.experiencia_laboral.telefono_empresa IS 'Teléfono de la empresa';
COMMENT ON COLUMN identidad.experiencia_laboral.fecha_inicio IS 'Fecha de ingreso';
COMMENT ON COLUMN identidad.experiencia_laboral.fecha_fin IS 'Fecha de retiro. Vacía si el empleo está vigente';
COMMENT ON COLUMN identidad.experiencia_laboral.funciones IS 'Funciones. Opcional en el formato';
COMMENT ON COLUMN identidad.experiencia_laboral.logros IS 'Logros. Opcional en el formato';

CREATE TABLE identidad.referencia_personal (
    id             bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    estudiante_id  bigint       NOT NULL,
    nombre         varchar(150) NOT NULL,
    empresa        varchar(150) NOT NULL,
    cargo          varchar(100) NOT NULL,
    telefono       varchar(20)  NOT NULL,
    ciudad         varchar(80)  NOT NULL,
    CONSTRAINT pk_referencia_personal PRIMARY KEY (id),
    CONSTRAINT fk_referencia_personal_estudiante FOREIGN KEY (estudiante_id) REFERENCES identidad.estudiante (id)
);

COMMENT ON TABLE identidad.referencia_personal IS 'Referencias personales de la hoja de vida. El software aplica el máximo de tres que recomienda PR-01.';
COMMENT ON COLUMN identidad.referencia_personal.id IS 'Identificador de la referencia';
COMMENT ON COLUMN identidad.referencia_personal.estudiante_id IS 'Estudiante dueño de la referencia';
COMMENT ON COLUMN identidad.referencia_personal.nombre IS 'Nombre con su tratamiento';
COMMENT ON COLUMN identidad.referencia_personal.empresa IS 'Empresa donde trabaja';
COMMENT ON COLUMN identidad.referencia_personal.cargo IS 'Cargo que ocupa';
COMMENT ON COLUMN identidad.referencia_personal.telefono IS 'Teléfono de contacto';
COMMENT ON COLUMN identidad.referencia_personal.ciudad IS 'Ciudad';
