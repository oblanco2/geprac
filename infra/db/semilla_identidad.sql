-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · Datos de la demostración del prototipo funcional · MS-01
--
-- Prepara en el esquema identidad lo que los cinco casos de uso del punto
-- 12 (CU-03, CU-06, CU-07, CU-08 y CU-09) necesitan y no crean:
--   1. El rol de las tres cuentas de la demostración: la Dirección del
--      Programa de LEI, el tutor académico y el estudiante. En el software
--      los asigna CU-04, que no entra en el prototipo funcional.
--   2. El registro del estudiante, con su hoja de vida. En el software lo
--      diligencia él mismo en CU-01, que tampoco entra. El documento y los
--      datos de contacto son ficticios; José puede cambiarlos por los suyos
--      antes de ejecutar el archivo, y entonces debe cambiarlos también en
--      semilla_legalizacion.sql, porque las inscripciones guardan su copia.
--
-- Antes: cada cuenta debe existir en Supabase Auth (proyecto de MS-01 >
-- Authentication > Users). La que falte se crea con Add user > Create
-- new user, con el correo institucional y Auto Confirm User. Ninguna
-- contraseña se escribe en este archivo.
--
-- Dónde se ejecuta: SQL Editor del proyecto de Supabase de MS-01. Se
-- pega completo y se da Run. Se puede ejecutar las veces que haga falta:
-- deja siempre los mismos roles y no duplica el registro del estudiante.
--
-- Resultado: una fila por cuenta, con su rol y su identificador, y en la
-- última columna el valor que se copia en semilla_legalizacion.sql. Quien
-- ya tenía la sesión abierta recibe su rol en el siguiente ingreso.
--
-- Autor: Darien Asdrwal Pesca Ojeda · modelo de datos
-- ══════════════════════════════════════════════════════════════════

-- 1. Las tres cuentas, con su nombre y su rol; el director, con su programa
WITH demostracion (correo, nombre, rol, programa) AS (VALUES
    ('oblanco2@udi.edu.co',  'Oscar Iván Blanco Díaz',       'DIRECTOR',   'LEI'),
    ('dpesca1@udi.edu.co',   'Darien Asdrwal Pesca Ojeda',   'TUTOR',      NULL),
    ('jrincon32@udi.edu.co', 'José Fernando Rincón Barrios', 'ESTUDIANTE', NULL)
)
INSERT INTO identidad.usuario (id, nombre_presentacion, correo_institucional, rol, codigo_programa, creado_en)
SELECT a.id, d.nombre, d.correo, d.rol, d.programa, localtimestamp
  FROM demostracion d
  JOIN auth.users a ON lower(a.email) = d.correo
    ON CONFLICT (id) DO UPDATE SET nombre_presentacion = EXCLUDED.nombre_presentacion,
                                   rol = EXCLUDED.rol, codigo_programa = EXCLUDED.codigo_programa;

-- 2. El registro del estudiante y su hoja de vida, si todavía no lo tiene
INSERT INTO identidad.estudiante (usuario_id, nombres, apellidos, tipo_documento, numero_documento, lugar_expedicion,
       fecha_nacimiento, lugar_nacimiento, genero, estado_civil, eps, direccion, barrio, ciudad, telefono_fijo, celular,
       correo_personal, codigo_programa, semestre_actual, perfil_profesional, herramientas_trabajo, creado_en)
SELECT u.id, 'José Fernando', 'Rincón Barrios', 'C.C.', '1.000.000.001', 'Bucaramanga',
       DATE '2003-05-20', 'Bucaramanga', 'Masculino', 'Soltero(a)', 'Nueva EPS',
       'Calle 9 # 25-40', 'La Universidad', 'Bucaramanga', '607 600 0001', '300 000 0001',
       'jose.rincon@example.com', 'LEI', 3,
       'Estudiante de Licenciatura en Educación Infantil interesado en la gestión de instituciones de educación inicial y en el trabajo con familias. Ha acompañado jornadas de lectura para niños de preescolar.',
       'Procesador de texto, hoja de cálculo, presentaciones y plataformas de aula virtual.', localtimestamp
  FROM identidad.usuario u
 WHERE u.correo_institucional = 'jrincon32@udi.edu.co'
    ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO identidad.formacion_academica (estudiante_id, tipo, institucion, nombre, anio)
SELECT e.id, v.tipo, v.institucion, v.nombre, v.anio
  FROM identidad.estudiante e
  JOIN identidad.usuario u ON u.id = e.usuario_id AND u.correo_institucional = 'jrincon32@udi.edu.co'
 CROSS JOIN (VALUES
    ('Secundaria', 'Institución Educativa Santander', 'Bachiller académico', 2022::smallint),
    ('Universitaria', 'Universidad de Investigación y Desarrollo', 'Licenciatura en Educación Infantil', NULL::smallint)
) AS v (tipo, institucion, nombre, anio)
 WHERE NOT EXISTS (SELECT 1 FROM identidad.formacion_academica f WHERE f.estudiante_id = e.id);

INSERT INTO identidad.experiencia_laboral (estudiante_id, empresa, cargo, jefe_inmediato, cargo_jefe, telefono_empresa,
       fecha_inicio, fecha_fin, funciones, logros)
SELECT e.id, v.empresa, v.cargo, v.jefe, v.cargo_jefe, v.telefono, v.inicio::date, v.fin::date, v.funciones, v.logros
  FROM identidad.estudiante e
  JOIN identidad.usuario u ON u.id = e.usuario_id AND u.correo_institucional = 'jrincon32@udi.edu.co'
 CROSS JOIN (VALUES
    ('Biblioteca Pública Gabriel Turbay', 'Auxiliar de promoción de lectura', 'Lic. Diana Carolina Ortiz', 'Coordinadora de promoción de lectura', '607 634 6500', '2024-02-01', '2024-11-30', 'Apoyo en la hora del cuento y en los talleres de lectura para niños de 3 a 6 años.', 'Organizó el club de lectura de los sábados.')
) AS v (empresa, cargo, jefe, cargo_jefe, telefono, inicio, fin, funciones, logros)
 WHERE NOT EXISTS (SELECT 1 FROM identidad.experiencia_laboral x WHERE x.estudiante_id = e.id);

INSERT INTO identidad.referencia_personal (estudiante_id, nombre, empresa, cargo, telefono, ciudad)
SELECT e.id, v.nombre, v.empresa, v.cargo, v.telefono, v.ciudad
  FROM identidad.estudiante e
  JOIN identidad.usuario u ON u.id = e.usuario_id AND u.correo_institucional = 'jrincon32@udi.edu.co'
 CROSS JOIN (VALUES
    ('Lic. Gloria Inés Parra', 'Institución Educativa Santander', 'Coordinadora académica', '312 776 0941', 'Bucaramanga'),
    ('Mg. Liliana Patiño', 'Universidad de Investigación y Desarrollo', 'Docente', '316 220 9931', 'Bucaramanga')
) AS v (nombre, empresa, cargo, telefono, ciudad)
 WHERE NOT EXISTS (SELECT 1 FROM identidad.referencia_personal r WHERE r.estudiante_id = e.id);

-- Las cuentas de la demostración. La que salga sin rol todavía no existe en Supabase Auth.
-- La última columna es la línea que se copia en semilla_legalizacion.sql.
SELECT d.correo, u.rol, u.codigo_programa AS programa, u.id AS identificador, e.id AS estudiante,
       CASE u.rol WHEN 'DIRECTOR' THEN 'director constant uuid := ''' || u.id || ''';'
                  WHEN 'TUTOR' THEN 'tutor constant uuid := ''' || u.id || ''';'
                  WHEN 'ESTUDIANTE' THEN 'estudiante constant bigint := ' || e.id || ';' END AS copiar_en_legalizacion
  FROM (VALUES ('oblanco2@udi.edu.co'), ('dpesca1@udi.edu.co'), ('jrincon32@udi.edu.co')) AS d(correo)
  LEFT JOIN identidad.usuario u ON u.correo_institucional = d.correo
  LEFT JOIN identidad.estudiante e ON e.usuario_id = u.id
 ORDER BY u.rol;
