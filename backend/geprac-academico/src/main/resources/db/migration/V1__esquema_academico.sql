-- ═══════════════════════════════════════════════════════════════
-- GEPRAC · Microservicio de Gestión Académica
-- Esquema base: identidades, oferta académica, convenios y asignación
-- ═══════════════════════════════════════════════════════════════

-- ── Roles del software ──────────────────────────────────────────
CREATE TABLE rol (
    id_rol       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre       VARCHAR(50)  NOT NULL UNIQUE,
    descripcion  VARCHAR(200)
);

-- ── Usuarios del negocio ────────────────────────────────────────
-- id_auth enlaza con el usuario de Supabase Auth. No se declara
-- clave foránea porque el esquema auth lo administra Supabase.
CREATE TABLE usuario (
    id_usuario       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_auth          UUID UNIQUE,
    documento        VARCHAR(20)  NOT NULL UNIQUE,
    nombre           VARCHAR(100) NOT NULL,
    apellido         VARCHAR(100) NOT NULL,
    correo           VARCHAR(150) NOT NULL UNIQUE,
    telefono         VARCHAR(20),
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion   TIMESTAMP    NOT NULL DEFAULT NOW(),
    creado_por       VARCHAR(50)
);

CREATE TABLE usuario_rol (
    id_usuario        INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    id_rol            INTEGER NOT NULL REFERENCES rol(id_rol),
    fecha_asignacion  TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id_usuario, id_rol)
);

-- ── Oferta académica ────────────────────────────────────────────
CREATE TABLE programa (
    id_programa     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo          VARCHAR(20)  NOT NULL UNIQUE,
    nombre          VARCHAR(150) NOT NULL,
    nivel           VARCHAR(50),
    horas_practica  INTEGER      NOT NULL DEFAULT 320
);

CREATE TABLE practica (
    id_practica       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_programa       INTEGER      NOT NULL REFERENCES programa(id_programa),
    nombre            VARCHAR(150) NOT NULL,
    tipo              VARCHAR(50),
    horas_requeridas  INTEGER      NOT NULL,
    periodo           VARCHAR(20)  NOT NULL
);

CREATE TABLE grupo (
    id_grupo      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_practica   INTEGER     NOT NULL REFERENCES practica(id_practica),
    nombre        VARCHAR(100) NOT NULL,
    fecha_inicio  DATE,
    fecha_fin     DATE,
    estado        VARCHAR(20) NOT NULL DEFAULT 'ABIERTO'
        CHECK (estado IN ('ABIERTO', 'CERRADO'))
);

CREATE TABLE estudiante_grupo (
    id_usuario         INTEGER NOT NULL REFERENCES usuario(id_usuario),
    id_grupo           INTEGER NOT NULL REFERENCES grupo(id_grupo),
    fecha_vinculacion  TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id_usuario, id_grupo)
);

-- ── Instituciones receptoras y convenios ────────────────────────
CREATE TABLE institucion (
    id_institucion  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nit             VARCHAR(20)  NOT NULL UNIQUE,
    nombre          VARCHAR(150) NOT NULL,
    direccion       VARCHAR(200),
    municipio       VARCHAR(100),
    rector          VARCHAR(150),
    telefono        VARCHAR(20),
    correo          VARCHAR(150)
);

CREATE TABLE convenio (
    id_convenio     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_institucion  INTEGER     NOT NULL REFERENCES institucion(id_institucion),
    numero          VARCHAR(30) NOT NULL UNIQUE,
    fecha_inicio    DATE        NOT NULL,
    fecha_fin       DATE        NOT NULL,
    cupos           INTEGER     NOT NULL CHECK (cupos > 0),
    estado          VARCHAR(20) NOT NULL DEFAULT 'VIGENTE'
        CHECK (estado IN ('VIGENTE', 'VENCIDO', 'CANCELADO')),
    CHECK (fecha_fin > fecha_inicio)
);

-- ── Asignación: el vínculo que sostiene el seguimiento ──────────
-- El microservicio de Seguimiento referencia este identificador,
-- pero sin clave foránea: son bases de datos distintas.
CREATE TABLE asignacion (
    id_asignacion      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_estudiante      INTEGER   NOT NULL REFERENCES usuario(id_usuario),
    id_docente         INTEGER   REFERENCES usuario(id_usuario),
    id_convenio        INTEGER   NOT NULL REFERENCES convenio(id_convenio),
    id_practica        INTEGER   NOT NULL REFERENCES practica(id_practica),
    fecha_asignacion   TIMESTAMP NOT NULL DEFAULT NOW(),
    estado_aprobacion  VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado_aprobacion IN ('PENDIENTE', 'APROBADA', 'DEVUELTA', 'INCOMPLETA')),
    aprobada_por       INTEGER   REFERENCES usuario(id_usuario),
    fecha_aprobacion   TIMESTAMP,
    observacion        VARCHAR(500)
);

-- Un estudiante no puede tener dos asignaciones aprobadas
-- sobre la misma práctica
CREATE UNIQUE INDEX ux_asignacion_activa
    ON asignacion (id_estudiante, id_practica)
    WHERE estado_aprobacion = 'APROBADA';

-- ── Traza de auditoría ──────────────────────────────────────────
CREATE TABLE auditoria (
    id_auditoria    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario      INTEGER,
    tabla_afectada  VARCHAR(50)  NOT NULL,
    operacion       VARCHAR(20)  NOT NULL,
    fecha_hora      TIMESTAMP    NOT NULL DEFAULT NOW(),
    detalle         VARCHAR(1000)
);

CREATE INDEX ix_auditoria_usuario ON auditoria (id_usuario);
CREATE INDEX ix_auditoria_fecha   ON auditoria (fecha_hora);

-- ── Roles iniciales ─────────────────────────────────────────────
INSERT INTO rol (nombre, descripcion) VALUES
    ('DIRECTOR',      'Director del Programa de Licenciatura'),
    ('COORDINADOR',   'Coordinador de Práctica'),
    ('DOCENTE',       'Docente Asesor'),
    ('ESTUDIANTE',    'Estudiante Practicante'),
    ('INSTITUCION',   'Institución Receptora'),
    ('SUPERUSUARIO',  'Administración del software');
