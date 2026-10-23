-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · MS-02 Legalización de Prácticas
-- Esquema legalizacion: lo transaccional, que se repite cada periodo
-- —catálogos, semestres, inscripciones, revisiones, avales y formatos
-- emitidos—. Dieciocho tablas.
--
-- Implementa el modelo de datos del segundo avance (punto 8: modelo
-- relacional y diccionario de datos). Cada tabla y cada campo llevan como
-- comentario su descripción del diccionario.
--
-- No hay claves foráneas hacia MS-01: estudiante_id, tutor_id, revisor_id y
-- director_id son identificadores de MS-01 que se guardan como valores, y
-- el programa se nombra por su código institucional.
--
-- Autor: Darien Asdrwal Pesca Ojeda · modelo de datos
-- ══════════════════════════════════════════════════════════════════

CREATE TABLE legalizacion.semestre (
    id            bigint      NOT NULL GENERATED ALWAYS AS IDENTITY,
    codigo        varchar(10) NOT NULL,
    fecha_inicio  date        NOT NULL,
    fecha_cierre  date        NOT NULL,
    abierto       boolean     NOT NULL,
    CONSTRAINT pk_semestre PRIMARY KEY (id),
    CONSTRAINT uq_semestre_codigo UNIQUE (codigo)
);

CREATE UNIQUE INDEX ux_semestre_abierto ON legalizacion.semestre (abierto) WHERE abierto;

COMMENT ON TABLE legalizacion.semestre IS 'Periodo académico en el que se inscriben las prácticas. Solo uno puede estar abierto.';
COMMENT ON COLUMN legalizacion.semestre.id IS 'Identificador del semestre';
COMMENT ON COLUMN legalizacion.semestre.codigo IS 'Código del periodo, por ejemplo 2026-2';
COMMENT ON COLUMN legalizacion.semestre.fecha_inicio IS 'Fecha de apertura';
COMMENT ON COLUMN legalizacion.semestre.fecha_cierre IS 'Fecha de cierre';
COMMENT ON COLUMN legalizacion.semestre.abierto IS 'Verdadero en un solo semestre a la vez';

CREATE TABLE legalizacion.practica (
    id                bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    codigo_programa   varchar(10)  NOT NULL,
    orden             smallint     NOT NULL,
    nombre            varchar(150) NOT NULL,
    objetivo_general  text         NOT NULL,
    horario_estandar  varchar(150),
    activa            boolean      NOT NULL,
    CONSTRAINT pk_practica PRIMARY KEY (id),
    CONSTRAINT uq_practica_posicion UNIQUE (codigo_programa, orden)
);

COMMENT ON TABLE legalizacion.practica IS 'Práctica del plan de estudios de un programa, con su contenido estandarizado.';
COMMENT ON COLUMN legalizacion.practica.id IS 'Identificador de la práctica';
COMMENT ON COLUMN legalizacion.practica.codigo_programa IS 'Programa al que pertenece, por su código institucional';
COMMENT ON COLUMN legalizacion.practica.orden IS 'Posición en el plan de estudios, de 1 a 8';
COMMENT ON COLUMN legalizacion.practica.nombre IS 'Nombre de la práctica';
COMMENT ON COLUMN legalizacion.practica.objetivo_general IS 'Objetivo general que se precarga en la inscripción';
COMMENT ON COLUMN legalizacion.practica.horario_estandar IS 'Horario estándar de la práctica';
COMMENT ON COLUMN legalizacion.practica.activa IS 'Indica si la práctica se ofrece para inscripción en el catálogo';

CREATE TABLE legalizacion.objetivo_practica (
    id           bigint   NOT NULL GENERATED ALWAYS AS IDENTITY,
    practica_id  bigint   NOT NULL,
    posicion     smallint NOT NULL,
    texto        text     NOT NULL,
    CONSTRAINT pk_objetivo_practica PRIMARY KEY (id),
    CONSTRAINT fk_objetivo_practica_practica FOREIGN KEY (practica_id) REFERENCES legalizacion.practica (id)
);

COMMENT ON TABLE legalizacion.objetivo_practica IS 'Objetivos específicos de una práctica, en el orden en que salen impresos.';
COMMENT ON COLUMN legalizacion.objetivo_practica.id IS 'Identificador del objetivo';
COMMENT ON COLUMN legalizacion.objetivo_practica.practica_id IS 'Práctica a la que pertenece';
COMMENT ON COLUMN legalizacion.objetivo_practica.posicion IS 'Orden dentro de la lista';
COMMENT ON COLUMN legalizacion.objetivo_practica.texto IS 'Enunciado del objetivo específico';

CREATE TABLE legalizacion.actividad_practica (
    id           bigint   NOT NULL GENERATED ALWAYS AS IDENTITY,
    practica_id  bigint   NOT NULL,
    posicion     smallint NOT NULL,
    texto        text     NOT NULL,
    CONSTRAINT pk_actividad_practica PRIMARY KEY (id),
    CONSTRAINT fk_actividad_practica_practica FOREIGN KEY (practica_id) REFERENCES legalizacion.practica (id)
);

COMMENT ON TABLE legalizacion.actividad_practica IS 'Actividades de una práctica, en el orden en que salen impresas.';
COMMENT ON COLUMN legalizacion.actividad_practica.id IS 'Identificador de la actividad';
COMMENT ON COLUMN legalizacion.actividad_practica.practica_id IS 'Práctica a la que pertenece';
COMMENT ON COLUMN legalizacion.actividad_practica.posicion IS 'Orden dentro de la lista';
COMMENT ON COLUMN legalizacion.actividad_practica.texto IS 'Enunciado de la actividad';

CREATE TABLE legalizacion.institucion (
    id                   bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    razon_social         varchar(150) NOT NULL,
    nit                  varchar(20),
    direccion            varchar(150) NOT NULL,
    ciudad               varchar(80)  NOT NULL,
    telefono             varchar(20)  NOT NULL,
    correo               varchar(150),
    sitio_web            varchar(150),
    representante_legal  varchar(150),
    activa               boolean      NOT NULL,
    CONSTRAINT pk_institucion PRIMARY KEY (id),
    CONSTRAINT uq_institucion_nombre_ciudad UNIQUE (razon_social, ciudad)
);

COMMENT ON TABLE legalizacion.institucion IS 'Institución receptora donde el estudiante desarrolla la práctica.';
COMMENT ON COLUMN legalizacion.institucion.id IS 'Identificador de la institución';
COMMENT ON COLUMN legalizacion.institucion.razon_social IS 'Razón social, que es como la nombran los formatos';
COMMENT ON COLUMN legalizacion.institucion.nit IS 'NIT. Opcional: no toda institución de educación inicial lo aporta';
COMMENT ON COLUMN legalizacion.institucion.direccion IS 'Dirección';
COMMENT ON COLUMN legalizacion.institucion.ciudad IS 'Ciudad';
COMMENT ON COLUMN legalizacion.institucion.telefono IS 'Teléfono';
COMMENT ON COLUMN legalizacion.institucion.correo IS 'Correo institucional';
COMMENT ON COLUMN legalizacion.institucion.sitio_web IS 'Sitio web institucional';
COMMENT ON COLUMN legalizacion.institucion.representante_legal IS 'Nombre del representante legal, que PR-04 exige en la información de la institución';
COMMENT ON COLUMN legalizacion.institucion.activa IS 'Indica si se ofrece en la selección del estudiante';

CREATE TABLE legalizacion.contacto_institucion (
    id              bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    institucion_id  bigint       NOT NULL,
    nombre          varchar(150) NOT NULL,
    cargo           varchar(100) NOT NULL,
    telefono        varchar(20)  NOT NULL,
    celular         varchar(20),
    correo          varchar(150),
    CONSTRAINT pk_contacto_institucion PRIMARY KEY (id),
    CONSTRAINT fk_contacto_institucion_institucion FOREIGN KEY (institucion_id) REFERENCES legalizacion.institucion (id)
);

COMMENT ON TABLE legalizacion.contacto_institucion IS 'Contactos de una institución. Uno de ellos figura como tutor del escenario en PR-04. La dirección que ese formato pide para el tutor se diligencia con la de la institución.';
COMMENT ON COLUMN legalizacion.contacto_institucion.id IS 'Identificador del contacto';
COMMENT ON COLUMN legalizacion.contacto_institucion.institucion_id IS 'Institución a la que pertenece';
COMMENT ON COLUMN legalizacion.contacto_institucion.nombre IS 'Nombre del contacto';
COMMENT ON COLUMN legalizacion.contacto_institucion.cargo IS 'Cargo que ocupa';
COMMENT ON COLUMN legalizacion.contacto_institucion.telefono IS 'Teléfono fijo';
COMMENT ON COLUMN legalizacion.contacto_institucion.celular IS 'Teléfono celular';
COMMENT ON COLUMN legalizacion.contacto_institucion.correo IS 'Correo electrónico';

CREATE TABLE legalizacion.tutor_practica (
    id           bigint NOT NULL GENERATED ALWAYS AS IDENTITY,
    semestre_id  bigint NOT NULL,
    practica_id  bigint NOT NULL,
    tutor_id     uuid   NOT NULL,
    CONSTRAINT pk_tutor_practica PRIMARY KEY (id),
    CONSTRAINT uq_tutor_practica_semestre UNIQUE (semestre_id, practica_id),
    CONSTRAINT fk_tutor_practica_semestre FOREIGN KEY (semestre_id) REFERENCES legalizacion.semestre (id),
    CONSTRAINT fk_tutor_practica_practica FOREIGN KEY (practica_id) REFERENCES legalizacion.practica (id)
);

COMMENT ON TABLE legalizacion.tutor_practica IS 'Designación del tutor académico responsable de una práctica en un semestre.';
COMMENT ON COLUMN legalizacion.tutor_practica.id IS 'Identificador de la designación';
COMMENT ON COLUMN legalizacion.tutor_practica.semestre_id IS 'Semestre de la designación';
COMMENT ON COLUMN legalizacion.tutor_practica.practica_id IS 'Práctica designada';
COMMENT ON COLUMN legalizacion.tutor_practica.tutor_id IS 'Identificador del usuario tutor en el microservicio de identidad';

CREATE TABLE legalizacion.inscripcion (
    id              bigint      NOT NULL GENERATED ALWAYS AS IDENTITY,
    estudiante_id   bigint      NOT NULL,
    practica_id     bigint      NOT NULL,
    semestre_id     bigint      NOT NULL,
    institucion_id  bigint,
    contacto_id     bigint,
    fecha_inicio    date,
    fecha_fin       date,
    estado          varchar(10) NOT NULL,
    creada_en       timestamp   NOT NULL,
    enviada_en      timestamp,
    CONSTRAINT pk_inscripcion PRIMARY KEY (id),
    CONSTRAINT uq_inscripcion_periodo UNIQUE (estudiante_id, practica_id, semestre_id),
    CONSTRAINT fk_inscripcion_practica FOREIGN KEY (practica_id) REFERENCES legalizacion.practica (id),
    CONSTRAINT fk_inscripcion_semestre FOREIGN KEY (semestre_id) REFERENCES legalizacion.semestre (id),
    CONSTRAINT fk_inscripcion_institucion FOREIGN KEY (institucion_id) REFERENCES legalizacion.institucion (id),
    CONSTRAINT fk_inscripcion_contacto FOREIGN KEY (contacto_id) REFERENCES legalizacion.contacto_institucion (id)
);

COMMENT ON TABLE legalizacion.inscripcion IS 'Inscripción de una práctica por un estudiante en un semestre. Es el expediente que recorre los cinco estados.';
COMMENT ON COLUMN legalizacion.inscripcion.id IS 'Identificador de la inscripción';
COMMENT ON COLUMN legalizacion.inscripcion.estudiante_id IS 'Identificador del estudiante en el microservicio de identidad';
COMMENT ON COLUMN legalizacion.inscripcion.practica_id IS 'Práctica inscrita';
COMMENT ON COLUMN legalizacion.inscripcion.semestre_id IS 'Semestre de la inscripción';
COMMENT ON COLUMN legalizacion.inscripcion.institucion_id IS 'Institución receptora seleccionada';
COMMENT ON COLUMN legalizacion.inscripcion.contacto_id IS 'Contacto que figura como tutor del escenario';
COMMENT ON COLUMN legalizacion.inscripcion.fecha_inicio IS 'Fecha de inicio de la práctica';
COMMENT ON COLUMN legalizacion.inscripcion.fecha_fin IS 'Fecha de finalización de la práctica';
COMMENT ON COLUMN legalizacion.inscripcion.estado IS 'BORRADOR, ENVIADA, APROBADA, DEVUELTA o AVALADA';
COMMENT ON COLUMN legalizacion.inscripcion.creada_en IS 'Fecha en que se inició la inscripción';
COMMENT ON COLUMN legalizacion.inscripcion.enviada_en IS 'Fecha del último envío a revisión';

CREATE TABLE legalizacion.inscripcion_datos (
    inscripcion_id             bigint       NOT NULL,
    nombres                    varchar(100) NOT NULL,
    apellidos                  varchar(100) NOT NULL,
    tipo_documento             varchar(4)   NOT NULL,
    numero_documento           varchar(20)  NOT NULL,
    lugar_expedicion           varchar(80),
    fecha_nacimiento           date,
    lugar_nacimiento           varchar(80),
    genero                     varchar(10),
    estado_civil               varchar(20),
    eps                        varchar(80),
    direccion                  varchar(150),
    barrio                     varchar(80),
    ciudad                     varchar(80),
    telefono_fijo              varchar(20),
    celular                    varchar(20),
    correo_personal            varchar(150),
    nombre_programa            varchar(150) NOT NULL,
    semestre_cursado           smallint     NOT NULL,
    perfil_profesional         text,
    herramientas_trabajo       text,
    institucion_razon_social   varchar(150),
    institucion_nit            varchar(20),
    institucion_direccion      varchar(150),
    institucion_ciudad         varchar(80),
    institucion_telefono       varchar(20),
    institucion_correo         varchar(150),
    institucion_sitio_web      varchar(150),
    institucion_representante  varchar(150),
    contacto_nombre            varchar(150),
    contacto_cargo             varchar(100),
    contacto_telefono          varchar(20),
    contacto_celular           varchar(20),
    contacto_correo            varchar(150),
    nombre_practica            varchar(150) NOT NULL,
    objetivo_general           text         NOT NULL,
    horario_estandar           varchar(150),
    CONSTRAINT pk_inscripcion_datos PRIMARY KEY (inscripcion_id),
    CONSTRAINT fk_inscripcion_datos_inscripcion FOREIGN KEY (inscripcion_id) REFERENCES legalizacion.inscripcion (id)
);

COMMENT ON TABLE legalizacion.inscripcion_datos IS 'Copia de los datos con los que se diligencian los formatos: los del estudiante y el contenido de la práctica, tomados al iniciar la inscripción, y los de la institución receptora y su contacto, tomados al seleccionarlos.';
COMMENT ON COLUMN legalizacion.inscripcion_datos.inscripcion_id IS 'Inscripción a la que pertenece la copia';
COMMENT ON COLUMN legalizacion.inscripcion_datos.nombres IS 'Nombres copiados del registro del estudiante';
COMMENT ON COLUMN legalizacion.inscripcion_datos.apellidos IS 'Apellidos copiados';
COMMENT ON COLUMN legalizacion.inscripcion_datos.tipo_documento IS 'Tipo de documento copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.numero_documento IS 'Número de documento copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.lugar_expedicion IS 'Lugar de expedición copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.fecha_nacimiento IS 'Fecha de nacimiento copiada';
COMMENT ON COLUMN legalizacion.inscripcion_datos.lugar_nacimiento IS 'Lugar de nacimiento copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.genero IS 'Género copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.estado_civil IS 'Estado civil copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.eps IS 'EPS copiada';
COMMENT ON COLUMN legalizacion.inscripcion_datos.direccion IS 'Dirección copiada';
COMMENT ON COLUMN legalizacion.inscripcion_datos.barrio IS 'Barrio copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.ciudad IS 'Ciudad copiada';
COMMENT ON COLUMN legalizacion.inscripcion_datos.telefono_fijo IS 'Teléfono fijo copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.celular IS 'Celular copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.correo_personal IS 'Correo personal copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.nombre_programa IS 'Nombre del programa copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.semestre_cursado IS 'Semestre que cursaba el estudiante';
COMMENT ON COLUMN legalizacion.inscripcion_datos.perfil_profesional IS 'Perfil profesional copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.herramientas_trabajo IS 'Herramientas de trabajo copiadas';
COMMENT ON COLUMN legalizacion.inscripcion_datos.institucion_razon_social IS 'Razón social de la institución copiada al seleccionarla';
COMMENT ON COLUMN legalizacion.inscripcion_datos.institucion_nit IS 'NIT de la institución copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.institucion_direccion IS 'Dirección de la institución copiada';
COMMENT ON COLUMN legalizacion.inscripcion_datos.institucion_ciudad IS 'Ciudad de la institución copiada';
COMMENT ON COLUMN legalizacion.inscripcion_datos.institucion_telefono IS 'Teléfono de la institución copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.institucion_correo IS 'Correo de la institución copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.institucion_sitio_web IS 'Sitio web de la institución copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.institucion_representante IS 'Representante legal de la institución copiado, que PR-04 exige';
COMMENT ON COLUMN legalizacion.inscripcion_datos.contacto_nombre IS 'Nombre del tutor del escenario copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.contacto_cargo IS 'Cargo del tutor del escenario copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.contacto_telefono IS 'Teléfono del tutor del escenario copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.contacto_celular IS 'Celular del tutor del escenario copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.contacto_correo IS 'Correo del tutor del escenario copiado';
COMMENT ON COLUMN legalizacion.inscripcion_datos.nombre_practica IS 'Nombre de la práctica copiado del catálogo';
COMMENT ON COLUMN legalizacion.inscripcion_datos.objetivo_general IS 'Objetivo general copiado del catálogo';
COMMENT ON COLUMN legalizacion.inscripcion_datos.horario_estandar IS 'Horario estándar copiado del catálogo';

CREATE TABLE legalizacion.inscripcion_objetivo (
    id              bigint   NOT NULL GENERATED ALWAYS AS IDENTITY,
    inscripcion_id  bigint   NOT NULL,
    posicion        smallint NOT NULL,
    texto           text     NOT NULL,
    CONSTRAINT pk_inscripcion_objetivo PRIMARY KEY (id),
    CONSTRAINT fk_inscripcion_objetivo_inscripcion FOREIGN KEY (inscripcion_id) REFERENCES legalizacion.inscripcion (id)
);

COMMENT ON TABLE legalizacion.inscripcion_objetivo IS 'Copia de los objetivos específicos de la práctica en la inscripción.';
COMMENT ON COLUMN legalizacion.inscripcion_objetivo.id IS 'Identificador';
COMMENT ON COLUMN legalizacion.inscripcion_objetivo.inscripcion_id IS 'Inscripción a la que pertenece';
COMMENT ON COLUMN legalizacion.inscripcion_objetivo.posicion IS 'Orden dentro de la lista';
COMMENT ON COLUMN legalizacion.inscripcion_objetivo.texto IS 'Enunciado copiado';

CREATE TABLE legalizacion.inscripcion_actividad (
    id              bigint   NOT NULL GENERATED ALWAYS AS IDENTITY,
    inscripcion_id  bigint   NOT NULL,
    posicion        smallint NOT NULL,
    texto           text     NOT NULL,
    CONSTRAINT pk_inscripcion_actividad PRIMARY KEY (id),
    CONSTRAINT fk_inscripcion_actividad_inscripcion FOREIGN KEY (inscripcion_id) REFERENCES legalizacion.inscripcion (id)
);

COMMENT ON TABLE legalizacion.inscripcion_actividad IS 'Copia de las actividades de la práctica en la inscripción.';
COMMENT ON COLUMN legalizacion.inscripcion_actividad.id IS 'Identificador';
COMMENT ON COLUMN legalizacion.inscripcion_actividad.inscripcion_id IS 'Inscripción a la que pertenece';
COMMENT ON COLUMN legalizacion.inscripcion_actividad.posicion IS 'Orden dentro de la lista';
COMMENT ON COLUMN legalizacion.inscripcion_actividad.texto IS 'Enunciado copiado';

CREATE TABLE legalizacion.inscripcion_formacion (
    id              bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    inscripcion_id  bigint       NOT NULL,
    tipo            varchar(15)  NOT NULL,
    institucion     varchar(150) NOT NULL,
    nombre          varchar(150) NOT NULL,
    anio            smallint,
    CONSTRAINT pk_inscripcion_formacion PRIMARY KEY (id),
    CONSTRAINT fk_inscripcion_formacion_inscripcion FOREIGN KEY (inscripcion_id) REFERENCES legalizacion.inscripcion (id)
);

COMMENT ON TABLE legalizacion.inscripcion_formacion IS 'Copia de la formación académica del estudiante para diligenciar PR-01.';
COMMENT ON COLUMN legalizacion.inscripcion_formacion.id IS 'Identificador';
COMMENT ON COLUMN legalizacion.inscripcion_formacion.inscripcion_id IS 'Inscripción a la que pertenece';
COMMENT ON COLUMN legalizacion.inscripcion_formacion.tipo IS 'Secundaria, Universitaria u Otros';
COMMENT ON COLUMN legalizacion.inscripcion_formacion.institucion IS 'Institución copiada';
COMMENT ON COLUMN legalizacion.inscripcion_formacion.nombre IS 'Nombre del título copiado';
COMMENT ON COLUMN legalizacion.inscripcion_formacion.anio IS 'Año copiado';

CREATE TABLE legalizacion.inscripcion_experiencia (
    id                bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    inscripcion_id    bigint       NOT NULL,
    empresa           varchar(150) NOT NULL,
    cargo             varchar(100) NOT NULL,
    jefe_inmediato    varchar(150),
    cargo_jefe        varchar(100),
    telefono_empresa  varchar(20),
    fecha_inicio      date         NOT NULL,
    fecha_fin         date,
    funciones         text,
    logros            text,
    CONSTRAINT pk_inscripcion_experiencia PRIMARY KEY (id),
    CONSTRAINT fk_inscripcion_experiencia_inscripcion FOREIGN KEY (inscripcion_id) REFERENCES legalizacion.inscripcion (id)
);

COMMENT ON TABLE legalizacion.inscripcion_experiencia IS 'Copia de la experiencia laboral del estudiante para diligenciar PR-01.';
COMMENT ON COLUMN legalizacion.inscripcion_experiencia.id IS 'Identificador';
COMMENT ON COLUMN legalizacion.inscripcion_experiencia.inscripcion_id IS 'Inscripción a la que pertenece';
COMMENT ON COLUMN legalizacion.inscripcion_experiencia.empresa IS 'Empresa copiada';
COMMENT ON COLUMN legalizacion.inscripcion_experiencia.cargo IS 'Cargo copiado';
COMMENT ON COLUMN legalizacion.inscripcion_experiencia.jefe_inmediato IS 'Jefe inmediato copiado';
COMMENT ON COLUMN legalizacion.inscripcion_experiencia.cargo_jefe IS 'Cargo del jefe copiado';
COMMENT ON COLUMN legalizacion.inscripcion_experiencia.telefono_empresa IS 'Teléfono de la empresa copiado';
COMMENT ON COLUMN legalizacion.inscripcion_experiencia.fecha_inicio IS 'Fecha de ingreso copiada';
COMMENT ON COLUMN legalizacion.inscripcion_experiencia.fecha_fin IS 'Fecha de retiro copiada';
COMMENT ON COLUMN legalizacion.inscripcion_experiencia.funciones IS 'Funciones copiadas';
COMMENT ON COLUMN legalizacion.inscripcion_experiencia.logros IS 'Logros copiados';

CREATE TABLE legalizacion.inscripcion_referencia (
    id              bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    inscripcion_id  bigint       NOT NULL,
    nombre          varchar(150) NOT NULL,
    empresa         varchar(150) NOT NULL,
    cargo           varchar(100) NOT NULL,
    telefono        varchar(20)  NOT NULL,
    ciudad          varchar(80)  NOT NULL,
    CONSTRAINT pk_inscripcion_referencia PRIMARY KEY (id),
    CONSTRAINT fk_inscripcion_referencia_inscripcion FOREIGN KEY (inscripcion_id) REFERENCES legalizacion.inscripcion (id)
);

COMMENT ON TABLE legalizacion.inscripcion_referencia IS 'Copia de las referencias personales del estudiante para diligenciar PR-01.';
COMMENT ON COLUMN legalizacion.inscripcion_referencia.id IS 'Identificador';
COMMENT ON COLUMN legalizacion.inscripcion_referencia.inscripcion_id IS 'Inscripción a la que pertenece';
COMMENT ON COLUMN legalizacion.inscripcion_referencia.nombre IS 'Nombre con tratamiento copiado';
COMMENT ON COLUMN legalizacion.inscripcion_referencia.empresa IS 'Empresa copiada';
COMMENT ON COLUMN legalizacion.inscripcion_referencia.cargo IS 'Cargo copiado';
COMMENT ON COLUMN legalizacion.inscripcion_referencia.telefono IS 'Teléfono copiado';
COMMENT ON COLUMN legalizacion.inscripcion_referencia.ciudad IS 'Ciudad copiada';

CREATE TABLE legalizacion.revision (
    id              bigint      NOT NULL GENERATED ALWAYS AS IDENTITY,
    inscripcion_id  bigint      NOT NULL,
    revisor_id      uuid        NOT NULL,
    resultado       varchar(10) NOT NULL,
    motivo          text,
    fecha           timestamp   NOT NULL,
    CONSTRAINT pk_revision PRIMARY KEY (id),
    CONSTRAINT fk_revision_inscripcion FOREIGN KEY (inscripcion_id) REFERENCES legalizacion.inscripcion (id),
    CONSTRAINT ck_revision_motivo CHECK (resultado <> 'DEVUELTA' OR (motivo IS NOT NULL AND btrim(motivo) <> ''))
);

COMMENT ON TABLE legalizacion.revision IS 'Resultado de la revisión que el tutor académico hace de una inscripción enviada.';
COMMENT ON COLUMN legalizacion.revision.id IS 'Identificador de la revisión';
COMMENT ON COLUMN legalizacion.revision.inscripcion_id IS 'Inscripción revisada';
COMMENT ON COLUMN legalizacion.revision.revisor_id IS 'Identificador del tutor en el microservicio de identidad';
COMMENT ON COLUMN legalizacion.revision.resultado IS 'APROBADA o DEVUELTA';
COMMENT ON COLUMN legalizacion.revision.motivo IS 'Obligatorio cuando el resultado es DEVUELTA';
COMMENT ON COLUMN legalizacion.revision.fecha IS 'Fecha de la revisión';

CREATE TABLE legalizacion.aval (
    id              bigint    NOT NULL GENERATED ALWAYS AS IDENTITY,
    inscripcion_id  bigint    NOT NULL,
    director_id     uuid      NOT NULL,
    fecha           timestamp NOT NULL,
    CONSTRAINT pk_aval PRIMARY KEY (id),
    CONSTRAINT uq_aval_inscripcion UNIQUE (inscripcion_id),
    CONSTRAINT fk_aval_inscripcion FOREIGN KEY (inscripcion_id) REFERENCES legalizacion.inscripcion (id)
);

COMMENT ON TABLE legalizacion.aval IS 'Aval que la Dirección del Programa da a una inscripción aprobada. Habilita la emisión de los formatos.';
COMMENT ON COLUMN legalizacion.aval.id IS 'Identificador del aval';
COMMENT ON COLUMN legalizacion.aval.inscripcion_id IS 'Inscripción avalada';
COMMENT ON COLUMN legalizacion.aval.director_id IS 'Identificador del director en el microservicio de identidad';
COMMENT ON COLUMN legalizacion.aval.fecha IS 'Fecha del aval';

CREATE TABLE legalizacion.plantilla_formato (
    id         bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    tipo       varchar(6)   NOT NULL,
    version    varchar(10)  NOT NULL,
    vigente    boolean      NOT NULL,
    archivo    varchar(255) NOT NULL,
    creada_en  timestamp    NOT NULL,
    CONSTRAINT pk_plantilla_formato PRIMARY KEY (id)
);

CREATE UNIQUE INDEX ux_plantilla_formato_vigente ON legalizacion.plantilla_formato (tipo) WHERE vigente;

COMMENT ON TABLE legalizacion.plantilla_formato IS 'Plantilla de un formato institucional. Reproduce el formato oficial y se versiona aparte del código.';
COMMENT ON COLUMN legalizacion.plantilla_formato.id IS 'Identificador de la plantilla';
COMMENT ON COLUMN legalizacion.plantilla_formato.tipo IS 'PR-01, PR-02, PR-04 o PR-05';
COMMENT ON COLUMN legalizacion.plantilla_formato.version IS 'Versión de la plantilla';
COMMENT ON COLUMN legalizacion.plantilla_formato.vigente IS 'Una sola plantilla vigente por tipo';
COMMENT ON COLUMN legalizacion.plantilla_formato.archivo IS 'Ubicación del archivo de la plantilla';
COMMENT ON COLUMN legalizacion.plantilla_formato.creada_en IS 'Fecha de carga';

CREATE TABLE legalizacion.formato_generado (
    id              bigint       NOT NULL GENERATED ALWAYS AS IDENTITY,
    inscripcion_id  bigint       NOT NULL,
    tipo            varchar(6)   NOT NULL,
    plantilla_id    bigint       NOT NULL,
    archivo         varchar(255) NOT NULL,
    fecha_emision   timestamp    NOT NULL,
    CONSTRAINT pk_formato_generado PRIMARY KEY (id),
    CONSTRAINT uq_formato_generado_tipo UNIQUE (inscripcion_id, tipo),
    CONSTRAINT fk_formato_generado_inscripcion FOREIGN KEY (inscripcion_id) REFERENCES legalizacion.inscripcion (id),
    CONSTRAINT fk_formato_generado_plantilla FOREIGN KEY (plantilla_id) REFERENCES legalizacion.plantilla_formato (id)
);

COMMENT ON TABLE legalizacion.formato_generado IS 'Formato institucional ya diligenciado y emitido a partir de una inscripción avalada.';
COMMENT ON COLUMN legalizacion.formato_generado.id IS 'Identificador del formato emitido';
COMMENT ON COLUMN legalizacion.formato_generado.inscripcion_id IS 'Inscripción de la que se emitió';
COMMENT ON COLUMN legalizacion.formato_generado.tipo IS 'PR-01, PR-02, PR-04 o PR-05';
COMMENT ON COLUMN legalizacion.formato_generado.plantilla_id IS 'Plantilla con la que se diligenció';
COMMENT ON COLUMN legalizacion.formato_generado.archivo IS 'Ubicación del documento emitido';
COMMENT ON COLUMN legalizacion.formato_generado.fecha_emision IS 'Fecha de emisión';
