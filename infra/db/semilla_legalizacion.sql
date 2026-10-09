-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · Datos de la demostración del prototipo funcional · MS-02
--
-- Prepara en el esquema legalizacion lo que los cinco casos de uso del
-- punto 12 (CU-03, CU-06, CU-07, CU-08 y CU-09) necesitan y no crean:
--   1. Los semestres 2025-2 y 2026-1, cerrados, y el 2026-2, abierto, y la
--      designación del tutor en las prácticas de cada uno (CU-04).
--   2. El catálogo de las ocho prácticas de LEI, con sus objetivos y sus
--      actividades (CU-02), y las instituciones receptoras con sus
--      contactos, que la Dirección sigue manteniendo en CU-03.
--   3. Catorce inscripciones en los cinco estados (CU-05), con la copia
--      completa de su expediente, sus revisiones y sus avales: tres del
--      estudiante de la demostración y once de estudiantes ficticios, que
--      no tienen cuenta y solo aparecen en las bandejas y en el historial.
-- Las plantillas de los cuatro formatos no van aquí: las pone en vigencia
-- la migración V2 de MS-02.
--
-- Antes:
--   1. Ejecutar semilla_identidad.sql en el proyecto de MS-01 y copiar
--      las tres líneas de su última columna en las líneas marcadas abajo.
--   2. Que MS-02 se haya desplegado con la migración V2.
--
-- Dónde se ejecuta: SQL Editor del proyecto de Supabase de MS-02. Se pega
-- completo y se da Run. Se puede ejecutar las veces que haga falta: no
-- repite nada de lo que ya existe. Para repetir la demostración desde el
-- principio, se pone en true la línea volver_a_empezar: borra antes las
-- inscripciones de la demostración, con sus revisiones, avales y formatos.
--
-- Resultado: una fila por inscripción de la demostración, con su estado.
--
-- Autor: Darien Asdrwal Pesca Ojeda · modelo de datos
-- ══════════════════════════════════════════════════════════════════

-- Funciones de apoyo de este archivo: viven solo mientras dura la ejecución

CREATE OR REPLACE FUNCTION pg_temp.semestre(p_codigo text) RETURNS bigint LANGUAGE sql AS
$f$ SELECT id FROM legalizacion.semestre WHERE codigo = p_codigo $f$;

CREATE OR REPLACE FUNCTION pg_temp.practica(p_orden int, p_nombre text, p_horario text, p_objetivo text, p_objetivos text[], p_actividades text[])
RETURNS void LANGUAGE plpgsql AS $f$
DECLARE v bigint;
BEGIN
    INSERT INTO legalizacion.practica (codigo_programa, orden, nombre, objetivo_general, horario_estandar, activa)
    VALUES ('LEI', p_orden, p_nombre, p_objetivo, p_horario, true)
    ON CONFLICT (codigo_programa, orden) DO NOTHING
    RETURNING id INTO v;
    IF v IS NULL THEN RETURN; END IF;   -- ya estaba en el catálogo: se respeta como está
    INSERT INTO legalizacion.objetivo_practica (practica_id, posicion, texto)
    SELECT v, k, p_objetivos[k] FROM generate_subscripts(p_objetivos, 1) AS k;
    INSERT INTO legalizacion.actividad_practica (practica_id, posicion, texto)
    SELECT v, k, p_actividades[k] FROM generate_subscripts(p_actividades, 1) AS k;
END $f$;

CREATE OR REPLACE FUNCTION pg_temp.institucion(p_razon text, p_nit text, p_direccion text, p_ciudad text, p_telefono text, p_correo text,
                                    p_web text, p_representante text, p_activa boolean, p_contactos jsonb)
RETURNS void LANGUAGE plpgsql AS $f$
DECLARE v bigint;
BEGIN
    INSERT INTO legalizacion.institucion (razon_social, nit, direccion, ciudad, telefono, correo, sitio_web, representante_legal, activa)
    VALUES (p_razon, p_nit, p_direccion, p_ciudad, p_telefono, p_correo, p_web, p_representante, p_activa)
    ON CONFLICT (razon_social, ciudad) DO NOTHING
    RETURNING id INTO v;
    IF v IS NULL THEN RETURN; END IF;   -- ya estaba en el catálogo: se respeta como está
    INSERT INTO legalizacion.contacto_institucion (institucion_id, nombre, cargo, telefono, celular, correo)
    SELECT v, c->>'nombre', c->>'cargo', c->>'telefono', c->>'celular', c->>'correo' FROM jsonb_array_elements(p_contactos) AS c;
END $f$;

-- Una inscripción con la copia de su expediente, sus revisiones y su aval, si todavía no existe
CREATE OR REPLACE FUNCTION pg_temp.inscribir(p_estudiante bigint, d jsonb, p_orden int, p_semestre text, p_institucion text, p_contacto text,
                                  p_inicio date, p_fin date, p_estado text, p_enviada timestamp, p_revisada timestamp,
                                  p_avalada timestamp, p_motivo text, p_tutor uuid, p_director uuid)
RETURNS void LANGUAGE plpgsql AS $f$
DECLARE
    v bigint; pr legalizacion.practica; ins legalizacion.institucion; con legalizacion.contacto_institucion;
BEGIN
    SELECT * INTO pr FROM legalizacion.practica WHERE codigo_programa = 'LEI' AND orden = p_orden;
    SELECT * INTO ins FROM legalizacion.institucion WHERE razon_social = p_institucion;
    SELECT * INTO con FROM legalizacion.contacto_institucion WHERE institucion_id = ins.id AND nombre = p_contacto;

    INSERT INTO legalizacion.inscripcion (estudiante_id, practica_id, semestre_id, institucion_id, contacto_id, fecha_inicio,
           fecha_fin, estado, creada_en, enviada_en)
    VALUES (p_estudiante, pr.id, pg_temp.semestre(p_semestre), ins.id, con.id, p_inicio, p_fin, p_estado,
            coalesce(p_enviada, localtimestamp) - interval '2 days', p_enviada)
    ON CONFLICT (estudiante_id, practica_id, semestre_id) DO NOTHING
    RETURNING id INTO v;
    IF v IS NULL THEN RETURN; END IF;   -- ya existe: conserva el estado al que llegó en la demostración

    INSERT INTO legalizacion.inscripcion_datos (inscripcion_id, nombres, apellidos, tipo_documento, numero_documento,
           lugar_expedicion, fecha_nacimiento, lugar_nacimiento, genero, estado_civil, eps, direccion, barrio, ciudad,
           telefono_fijo, celular, correo_personal, nombre_programa, semestre_cursado, perfil_profesional, herramientas_trabajo,
           institucion_razon_social, institucion_nit, institucion_direccion, institucion_ciudad, institucion_telefono,
           institucion_correo, institucion_sitio_web, institucion_representante, contacto_nombre, contacto_cargo,
           contacto_telefono, contacto_celular, contacto_correo, nombre_practica, objetivo_general, horario_estandar)
    VALUES (v, d->>'nombres', d->>'apellidos', d->>'tipoDocumento', d->>'numeroDocumento', d->>'lugarExpedicion',
            (d->>'fechaNacimiento')::date, d->>'lugarNacimiento', d->>'genero', d->>'estadoCivil', d->>'eps', d->>'direccion',
            d->>'barrio', d->>'ciudad', d->>'telefonoFijo', d->>'celular', d->>'correoPersonal', d->>'nombrePrograma',
            (d->>'semestreCursado')::smallint, d->>'perfilProfesional', d->>'herramientasTrabajo',
            ins.razon_social, ins.nit, ins.direccion, ins.ciudad, ins.telefono, ins.correo, ins.sitio_web, ins.representante_legal,
            con.nombre, con.cargo, con.telefono, con.celular, con.correo, pr.nombre, pr.objetivo_general, pr.horario_estandar);

    INSERT INTO legalizacion.inscripcion_objetivo (inscripcion_id, posicion, texto)
    SELECT v, o.posicion, o.texto FROM legalizacion.objetivo_practica o WHERE o.practica_id = pr.id;
    INSERT INTO legalizacion.inscripcion_actividad (inscripcion_id, posicion, texto)
    SELECT v, a.posicion, a.texto FROM legalizacion.actividad_practica a WHERE a.practica_id = pr.id;
    INSERT INTO legalizacion.inscripcion_formacion (inscripcion_id, tipo, institucion, nombre, anio)
    SELECT v, x->>'tipo', x->>'institucion', x->>'nombre', (x->>'anio')::smallint FROM jsonb_array_elements(d->'formaciones') x;
    INSERT INTO legalizacion.inscripcion_experiencia (inscripcion_id, empresa, cargo, jefe_inmediato, cargo_jefe, telefono_empresa,
           fecha_inicio, fecha_fin, funciones, logros)
    SELECT v, x->>'empresa', x->>'cargo', x->>'jefeInmediato', x->>'cargoJefe', x->>'telefonoEmpresa',
           (x->>'fechaInicio')::date, (x->>'fechaFin')::date, x->>'funciones', x->>'logros' FROM jsonb_array_elements(d->'experiencias') x;
    INSERT INTO legalizacion.inscripcion_referencia (inscripcion_id, nombre, empresa, cargo, telefono, ciudad)
    SELECT v, x->>'nombre', x->>'empresa', x->>'cargo', x->>'telefono', x->>'ciudad' FROM jsonb_array_elements(d->'referencias') x;

    IF p_revisada IS NOT NULL THEN
        INSERT INTO legalizacion.revision (inscripcion_id, revisor_id, resultado, motivo, fecha)
        VALUES (v, p_tutor, CASE WHEN p_estado = 'DEVUELTA' THEN 'DEVUELTA' ELSE 'APROBADA' END, p_motivo, p_revisada);
    END IF;
    IF p_avalada IS NOT NULL THEN
        INSERT INTO legalizacion.aval (inscripcion_id, director_id, fecha) VALUES (v, p_director, p_avalada);
    END IF;
END $f$;

DO $$
DECLARE
    -- ▼ las tres líneas que entregó semilla_identidad.sql, en su última columna
    director constant uuid := '00000000-0000-0000-0000-000000000000';
    tutor constant uuid := '00000000-0000-0000-0000-000000000000';
    estudiante constant bigint := 0;
    -- ▼ true para borrar las inscripciones de la demostración y empezarla de nuevo
    volver_a_empezar constant boolean := false;
BEGIN
    IF director = '00000000-0000-0000-0000-000000000000' OR tutor = '00000000-0000-0000-0000-000000000000' OR estudiante = 0 THEN
        RAISE EXCEPTION 'Copie en las líneas marcadas los tres valores que entregó semilla_identidad.sql.';
    END IF;

    IF volver_a_empezar THEN
        CREATE TEMP TABLE demo ON COMMIT DROP AS
        SELECT id FROM legalizacion.inscripcion
         WHERE estudiante_id = estudiante OR estudiante_id BETWEEN 9001 AND 9010;
        DELETE FROM legalizacion.formato_generado WHERE inscripcion_id IN (SELECT id FROM demo);
        DELETE FROM legalizacion.aval WHERE inscripcion_id IN (SELECT id FROM demo);
        DELETE FROM legalizacion.revision WHERE inscripcion_id IN (SELECT id FROM demo);
        DELETE FROM legalizacion.inscripcion_objetivo WHERE inscripcion_id IN (SELECT id FROM demo);
        DELETE FROM legalizacion.inscripcion_actividad WHERE inscripcion_id IN (SELECT id FROM demo);
        DELETE FROM legalizacion.inscripcion_formacion WHERE inscripcion_id IN (SELECT id FROM demo);
        DELETE FROM legalizacion.inscripcion_experiencia WHERE inscripcion_id IN (SELECT id FROM demo);
        DELETE FROM legalizacion.inscripcion_referencia WHERE inscripcion_id IN (SELECT id FROM demo);
        DELETE FROM legalizacion.inscripcion_datos WHERE inscripcion_id IN (SELECT id FROM demo);
        DELETE FROM legalizacion.inscripcion WHERE id IN (SELECT id FROM demo);
    END IF;

    -- 1. Semestres: solo uno abierto, el 2026-2
    INSERT INTO legalizacion.semestre (codigo, fecha_inicio, fecha_cierre, abierto)
    SELECT v.codigo, v.inicio::date, v.cierre::date, v.abierto
      FROM (VALUES ('2025-2', '2025-08-01', '2025-12-05', false),
                   ('2026-1', '2026-02-02', '2026-06-05', false),
                   ('2026-2', '2026-08-01', '2026-12-05', true)) AS v (codigo, inicio, cierre, abierto)
     WHERE NOT EXISTS (SELECT 1 FROM legalizacion.semestre s WHERE s.codigo = v.codigo)
       AND NOT (v.abierto AND EXISTS (SELECT 1 FROM legalizacion.semestre s WHERE s.abierto));

    -- 2. Las ocho prácticas de LEI y las instituciones receptoras
    PERFORM pg_temp.practica(1, 'Observación del ambiente pedagógico', 'Lunes y miércoles de 7:00 a 11:00',
        'Reconocer el ambiente pedagógico de una institución de educación inicial a partir de la observación de sus espacios, rutinas y materiales.',
        ARRAY['Describir la organización de los espacios físicos y su relación con las actividades de los niños.', 'Identificar las rutinas diarias y los momentos de la jornada.', 'Registrar los materiales didácticos disponibles y su uso.', 'Relacionar lo observado con los referentes técnicos de la educación inicial.'],
        ARRAY['Observar la jornada completa en el aula asignada.', 'Diligenciar el diario de campo de cada sesión.', 'Elaborar el informe de caracterización del ambiente pedagógico.']);
    PERFORM pg_temp.practica(2, 'Observación del maestro en el aula', 'Martes y jueves de 7:00 a 11:00',
        'Analizar la práctica del maestro de educación inicial en el aula: sus estrategias de enseñanza y su interacción con los niños.',
        ARRAY['Identificar las estrategias didácticas que emplea el maestro.', 'Describir la comunicación y el manejo del grupo.', 'Reconocer cómo planea y evalúa las actividades.', 'Contrastar lo observado con los fundamentos pedagógicos del programa.'],
        ARRAY['Observar al maestro titular durante las sesiones programadas.', 'Entrevistar al maestro sobre su planeación.', 'Registrar las observaciones en el diario de campo.', 'Presentar el informe de análisis de la práctica docente.']);
    PERFORM pg_temp.practica(3, 'Investigación en gerencia educativa', 'Martes y jueves de 7:00 a 12:00',
        'Analizar los procesos de administración y gestión educativa de una institución de educación inicial, identificando las funciones de sus instancias directivas y sus instrumentos de registro.',
        ARRAY['Identificar la estructura administrativa de la institución y las funciones de cada instancia.', 'Describir el proceso de matrícula y los libros reglamentarios que la institución mantiene.', 'Contrastar lo observado con los lineamientos del Ministerio de Educación Nacional.'],
        ARRAY['Entrevistar al rector y al coordinador sobre sus funciones.', 'Revisar los instrumentos de registro académico de la institución.', 'Elaborar el informe de la práctica con las conclusiones del ejercicio.']);
    PERFORM pg_temp.practica(4, 'Práctica en espacios no convencionales', 'Sábados de 8:00 a 12:00',
        'Diseñar y desarrollar experiencias pedagógicas para la primera infancia en escenarios distintos al aula escolar.',
        ARRAY['Caracterizar el escenario no convencional y la población que atiende.', 'Diseñar experiencias pedagógicas acordes con el escenario.', 'Evaluar la participación de los niños en las experiencias desarrolladas.'],
        ARRAY['Visitar el escenario y elaborar su caracterización.', 'Planear las experiencias pedagógicas.', 'Desarrollar las experiencias con el grupo asignado.', 'Elaborar el informe con la evaluación de las experiencias.']);
    PERFORM pg_temp.practica(5, 'Primera infancia de 0 a 3 años', 'Lunes a miércoles de 8:00 a 11:00',
        'Acompañar los procesos de desarrollo de niños de 0 a 3 años mediante experiencias de cuidado, juego y exploración.',
        ARRAY['Identificar los hitos del desarrollo de los niños del grupo.', 'Planear experiencias de juego y exploración para la edad.', 'Participar en las rutinas de cuidado con criterio pedagógico.', 'Registrar los avances de los niños en los instrumentos de seguimiento.'],
        ARRAY['Observar al grupo y elaborar su caracterización.', 'Desarrollar experiencias de juego y exploración.', 'Llevar el registro de seguimiento del desarrollo.']);
    PERFORM pg_temp.practica(6, 'Niños de 3 a 6 años y anteproyecto', 'Martes y jueves de 7:00 a 12:00',
        'Desarrollar experiencias pedagógicas con niños de 3 a 6 años y formular el anteproyecto de investigación de la práctica.',
        ARRAY['Caracterizar al grupo de niños de 3 a 6 años.', 'Planear y desarrollar experiencias pedagógicas para el grupo.', 'Identificar una situación problema del contexto.', 'Formular el anteproyecto con su pregunta, objetivos y metodología.', 'Socializar el anteproyecto con el tutor académico.'],
        ARRAY['Elaborar la caracterización del grupo.', 'Desarrollar las experiencias pedagógicas planeadas.', 'Redactar el anteproyecto.', 'Presentar el anteproyecto ante el tutor académico.']);
    PERFORM pg_temp.practica(7, 'Implementación en institución educativa I', 'Lunes a jueves de 7:00 a 11:00',
        'Implementar en una institución educativa la propuesta derivada del anteproyecto, con su planeación y su registro.',
        ARRAY['Ajustar la propuesta al contexto de la institución.', 'Planear las sesiones de implementación.', 'Desarrollar las sesiones con el grupo asignado.', 'Registrar la evidencia de cada sesión.'],
        ARRAY['Concertar con la institución el cronograma de implementación.', 'Elaborar la planeación de las sesiones.', 'Desarrollar las sesiones de implementación.', 'Registrar la evidencia en el diario de campo.', 'Presentar el informe de avance.']);
    PERFORM pg_temp.practica(8, 'Implementación en institución educativa II', 'Lunes a jueves de 7:00 a 11:00',
        'Culminar la implementación de la propuesta pedagógica y evaluar sus resultados.',
        ARRAY['Completar las sesiones de implementación.', 'Evaluar los resultados de la propuesta.', 'Sistematizar la experiencia.', 'Socializar los resultados con la institución.'],
        ARRAY['Desarrollar las sesiones restantes.', 'Aplicar los instrumentos de evaluación.', 'Redactar el informe final.', 'Socializar los resultados con la comunidad educativa.', 'Entregar el informe final al tutor académico.']);

    PERFORM pg_temp.institucion('Institución Educativa Las Américas', '890.205.114-3', 'Carrera 22 # 104-40', 'Bucaramanga', '607 643 2200', 'rectoria@lasamericas.edu.co', 'www.lasamericas.edu.co', 'Álvaro Pineda Suárez', true,
        '[{"nombre": "María Patricia Flores", "cargo": "Coordinadora académica", "telefono": "607 643 2210", "celular": "311 764 0098", "correo": "coordinacion@lasamericas.edu.co"}, {"nombre": "Álvaro Pineda Suárez", "cargo": "Rector", "telefono": "607 643 2200", "celular": "310 228 7741", "correo": "rectoria@lasamericas.edu.co"}]'::jsonb);
    PERFORM pg_temp.institucion('Jardín Infantil Los Cerezos', '901.442.087-1', 'Calle 30 # 9-55, Cañaveral', 'Floridablanca', '607 648 2210', 'contacto@loscerezos.edu.co', NULL, 'Hernán Darío Vesga', true,
        '[{"nombre": "Marta Rueda", "cargo": "Coordinadora", "telefono": "607 648 2210", "celular": "317 880 4412", "correo": "coordinacion@loscerezos.edu.co"}]'::jsonb);
    PERFORM pg_temp.institucion('Centro de Desarrollo Infantil Villa del Sol', '900.778.331-6', 'Carrera 15 # 56-21', 'Bucaramanga', '607 632 1180', 'cdivilladelsol@gmail.com', NULL, 'Luz Marina Sepúlveda', true,
        '[{"nombre": "Luz Dary Cáceres", "cargo": "Coordinadora pedagógica", "telefono": "607 632 1180", "celular": "313 509 2264", "correo": "coordinacion.villadelsol@gmail.com"}]'::jsonb);
    PERFORM pg_temp.institucion('Institución Educativa Santander', '890.201.552-9', 'Carrera 27 # 36-48', 'Bucaramanga', '607 635 4400', 'secretaria@iesantander.edu.co', 'www.iesantander.edu.co', 'Hernando Silva Prada', true,
        '[{"nombre": "Gloria Inés Parra", "cargo": "Coordinadora académica", "telefono": "607 635 4412", "celular": "312 776 0941", "correo": "coordinacion@iesantander.edu.co"}, {"nombre": "Hernando Silva Prada", "cargo": "Rector", "telefono": "607 635 4400", "celular": "310 662 1830", "correo": "rectoria@iesantander.edu.co"}, {"nombre": "Claudia Patricia Mora", "cargo": "Docente de preescolar", "telefono": "607 635 4415", "celular": "318 204 7765", "correo": "cmora@iesantander.edu.co"}]'::jsonb);
    PERFORM pg_temp.institucion('Institución Educativa Dámaso Zapata', '890.203.117-4', 'Calle 10 # 27-32', 'Bucaramanga', '607 634 5560', 'rectoria@damasozapata.edu.co', 'www.damasozapata.edu.co', 'Elsa Marina Díaz', true,
        '[{"nombre": "Rubén Darío Quintero", "cargo": "Coordinador académico", "telefono": "607 634 5571", "celular": "316 448 2093", "correo": "coordinacion@damasozapata.edu.co"}, {"nombre": "Elsa Marina Díaz", "cargo": "Rectora", "telefono": "607 634 5560", "celular": "315 880 1276", "correo": "rectoria@damasozapata.edu.co"}]'::jsonb);
    PERFORM pg_temp.institucion('Ludoteca Comunitaria Los Naranjos', NULL, 'Calle 4 # 22-10, El Poblado', 'Girón', '607 646 9012', 'ludotecalosnaranjos@gmail.com', NULL, 'Jorge Enrique Becerra', true,
        '[{"nombre": "Yolanda Becerra", "cargo": "Coordinadora de la ludoteca", "telefono": "607 646 9012", "celular": "311 590 8834", "correo": "ludotecalosnaranjos@gmail.com"}]'::jsonb);
    PERFORM pg_temp.institucion('Centro Educativo Rural La Esperanza', NULL, 'Vereda La Esperanza, kilómetro 8', 'Lebrija', '607 656 2245', 'cerlaesperanza@gmail.com', NULL, 'Pedro Antonio Gualdrón', true,
        '[{"nombre": "Pedro Antonio Gualdrón", "cargo": "Director rural", "telefono": "607 656 2245", "celular": "320 447 1902", "correo": "cerlaesperanza@gmail.com"}]'::jsonb);
    PERFORM pg_temp.institucion('Hogar Infantil Pequeños Exploradores', '900.315.774-2', 'Carrera 6 # 11-20', 'Piedecuesta', '607 655 3021', 'hipexploradores@gmail.com', NULL, 'Martha Cecilia Rojas', false,
        '[{"nombre": "Martha Cecilia Rojas", "cargo": "Directora", "telefono": "607 655 3021", "celular": "314 902 6617", "correo": "hipexploradores@gmail.com"}]'::jsonb);

    -- 3. El tutor de la demostración, designado en las prácticas de cada semestre; la 8 del 2026-2 queda sin tutor
    INSERT INTO legalizacion.tutor_practica (semestre_id, practica_id, tutor_id)
    SELECT s.id, p.id, tutor
      FROM legalizacion.semestre s
      JOIN legalizacion.practica p ON p.codigo_programa = 'LEI'
     WHERE s.codigo IN ('2025-2', '2026-1', '2026-2')
       AND NOT (s.codigo = '2026-2' AND p.orden = 8)
        ON CONFLICT (semestre_id, practica_id) DO NOTHING;

    -- 4. Las inscripciones, con su copia, sus revisiones y sus avales
    PERFORM pg_temp.inscribir(estudiante, '{"nombres": "José Fernando", "apellidos": "Rincón Barrios", "tipoDocumento": "C.C.", "numeroDocumento": "1.000.000.001", "lugarExpedicion": "Bucaramanga", "fechaNacimiento": "2003-05-20", "lugarNacimiento": "Bucaramanga", "genero": "Masculino", "estadoCivil": "Soltero(a)", "eps": "Nueva EPS", "direccion": "Calle 9 # 25-40", "barrio": "La Universidad", "ciudad": "Bucaramanga", "telefonoFijo": "607 600 0001", "celular": "300 000 0001", "correoPersonal": "jose.rincon@example.com", "perfilProfesional": "Estudiante de Licenciatura en Educación Infantil interesado en la gestión de instituciones de educación inicial y en el trabajo con familias. Ha acompañado jornadas de lectura para niños de preescolar.", "herramientasTrabajo": "Procesador de texto, hoja de cálculo, presentaciones y plataformas de aula virtual.", "formaciones": [{"tipo": "Secundaria", "institucion": "Institución Educativa Santander", "nombre": "Bachiller académico", "anio": 2022}, {"tipo": "Universitaria", "institucion": "Universidad de Investigación y Desarrollo", "nombre": "Licenciatura en Educación Infantil", "anio": null}], "experiencias": [{"empresa": "Biblioteca Pública Gabriel Turbay", "cargo": "Auxiliar de promoción de lectura", "jefeInmediato": "Lic. Diana Carolina Ortiz", "cargoJefe": "Coordinadora de promoción de lectura", "telefonoEmpresa": "607 634 6500", "fechaInicio": "2024-02-01", "fechaFin": "2024-11-30", "funciones": "Apoyo en la hora del cuento y en los talleres de lectura para niños de 3 a 6 años.", "logros": "Organizó el club de lectura de los sábados."}], "referencias": [{"nombre": "Lic. Gloria Inés Parra", "empresa": "Institución Educativa Santander", "cargo": "Coordinadora académica", "telefono": "312 776 0941", "ciudad": "Bucaramanga"}, {"nombre": "Mg. Liliana Patiño", "empresa": "Universidad de Investigación y Desarrollo", "cargo": "Docente", "telefono": "316 220 9931", "ciudad": "Bucaramanga"}], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 1}'::jsonb, 1, '2025-2', 'Jardín Infantil Los Cerezos', 'Marta Rueda',
        '2025-10-06'::date, '2025-11-14'::date, 'AVALADA', '2025-09-10 10:20'::timestamp, '2025-09-15 16:05'::timestamp, '2025-09-18 09:40'::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(estudiante, '{"nombres": "José Fernando", "apellidos": "Rincón Barrios", "tipoDocumento": "C.C.", "numeroDocumento": "1.000.000.001", "lugarExpedicion": "Bucaramanga", "fechaNacimiento": "2003-05-20", "lugarNacimiento": "Bucaramanga", "genero": "Masculino", "estadoCivil": "Soltero(a)", "eps": "Nueva EPS", "direccion": "Calle 9 # 25-40", "barrio": "La Universidad", "ciudad": "Bucaramanga", "telefonoFijo": "607 600 0001", "celular": "300 000 0001", "correoPersonal": "jose.rincon@example.com", "perfilProfesional": "Estudiante de Licenciatura en Educación Infantil interesado en la gestión de instituciones de educación inicial y en el trabajo con familias. Ha acompañado jornadas de lectura para niños de preescolar.", "herramientasTrabajo": "Procesador de texto, hoja de cálculo, presentaciones y plataformas de aula virtual.", "formaciones": [{"tipo": "Secundaria", "institucion": "Institución Educativa Santander", "nombre": "Bachiller académico", "anio": 2022}, {"tipo": "Universitaria", "institucion": "Universidad de Investigación y Desarrollo", "nombre": "Licenciatura en Educación Infantil", "anio": null}], "experiencias": [{"empresa": "Biblioteca Pública Gabriel Turbay", "cargo": "Auxiliar de promoción de lectura", "jefeInmediato": "Lic. Diana Carolina Ortiz", "cargoJefe": "Coordinadora de promoción de lectura", "telefonoEmpresa": "607 634 6500", "fechaInicio": "2024-02-01", "fechaFin": "2024-11-30", "funciones": "Apoyo en la hora del cuento y en los talleres de lectura para niños de 3 a 6 años.", "logros": "Organizó el club de lectura de los sábados."}], "referencias": [{"nombre": "Lic. Gloria Inés Parra", "empresa": "Institución Educativa Santander", "cargo": "Coordinadora académica", "telefono": "312 776 0941", "ciudad": "Bucaramanga"}, {"nombre": "Mg. Liliana Patiño", "empresa": "Universidad de Investigación y Desarrollo", "cargo": "Docente", "telefono": "316 220 9931", "ciudad": "Bucaramanga"}], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 2}'::jsonb, 2, '2026-1', 'Institución Educativa Dámaso Zapata', 'Rubén Darío Quintero',
        '2026-04-06'::date, '2026-05-15'::date, 'AVALADA', '2026-03-10 11:15'::timestamp, '2026-03-13 15:30'::timestamp, '2026-03-17 10:10'::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(estudiante, '{"nombres": "José Fernando", "apellidos": "Rincón Barrios", "tipoDocumento": "C.C.", "numeroDocumento": "1.000.000.001", "lugarExpedicion": "Bucaramanga", "fechaNacimiento": "2003-05-20", "lugarNacimiento": "Bucaramanga", "genero": "Masculino", "estadoCivil": "Soltero(a)", "eps": "Nueva EPS", "direccion": "Calle 9 # 25-40", "barrio": "La Universidad", "ciudad": "Bucaramanga", "telefonoFijo": "607 600 0001", "celular": "300 000 0001", "correoPersonal": "jose.rincon@example.com", "perfilProfesional": "Estudiante de Licenciatura en Educación Infantil interesado en la gestión de instituciones de educación inicial y en el trabajo con familias. Ha acompañado jornadas de lectura para niños de preescolar.", "herramientasTrabajo": "Procesador de texto, hoja de cálculo, presentaciones y plataformas de aula virtual.", "formaciones": [{"tipo": "Secundaria", "institucion": "Institución Educativa Santander", "nombre": "Bachiller académico", "anio": 2022}, {"tipo": "Universitaria", "institucion": "Universidad de Investigación y Desarrollo", "nombre": "Licenciatura en Educación Infantil", "anio": null}], "experiencias": [{"empresa": "Biblioteca Pública Gabriel Turbay", "cargo": "Auxiliar de promoción de lectura", "jefeInmediato": "Lic. Diana Carolina Ortiz", "cargoJefe": "Coordinadora de promoción de lectura", "telefonoEmpresa": "607 634 6500", "fechaInicio": "2024-02-01", "fechaFin": "2024-11-30", "funciones": "Apoyo en la hora del cuento y en los talleres de lectura para niños de 3 a 6 años.", "logros": "Organizó el club de lectura de los sábados."}], "referencias": [{"nombre": "Lic. Gloria Inés Parra", "empresa": "Institución Educativa Santander", "cargo": "Coordinadora académica", "telefono": "312 776 0941", "ciudad": "Bucaramanga"}, {"nombre": "Mg. Liliana Patiño", "empresa": "Universidad de Investigación y Desarrollo", "cargo": "Docente", "telefono": "316 220 9931", "ciudad": "Bucaramanga"}], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 3}'::jsonb, 3, '2026-2', 'Institución Educativa Las Américas', 'María Patricia Flores',
        '2026-10-05'::date, '2026-11-13'::date, 'ENVIADA', '2026-09-25 18:45'::timestamp, NULL::timestamp, NULL::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(9001, '{"nombres": "Laura Valentina", "apellidos": "Gómez Ardila", "tipoDocumento": "C.C.", "numeroDocumento": "1.098.765.432", "celular": "300 412 7765", "ciudad": "Bucaramanga", "formaciones": [], "experiencias": [], "referencias": [], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 3}'::jsonb, 3, '2026-2', 'Institución Educativa Las Américas', 'María Patricia Flores',
        '2026-09-21'::date, '2026-10-30'::date, 'DEVUELTA', '2026-09-16 09:00'::timestamp, '2026-09-18 14:20'::timestamp, NULL::timestamp,
        'Las fechas de la práctica se salen del corte. Ajústelas al periodo del tercer corte, del 05/10/2026 al 13/11/2026, y confirme el horario con la coordinadora de la institución.', tutor, director);
    PERFORM pg_temp.inscribir(9002, '{"nombres": "Camila Andrea", "apellidos": "Rueda Pinto", "tipoDocumento": "C.C.", "numeroDocumento": "1.095.832.114", "celular": "315 602 1187", "ciudad": "Bucaramanga", "formaciones": [], "experiencias": [], "referencias": [], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 3}'::jsonb, 3, '2026-2', 'Jardín Infantil Los Cerezos', 'Marta Rueda',
        '2026-10-05'::date, '2026-11-13'::date, 'AVALADA', '2026-09-16 10:30'::timestamp, '2026-09-19 11:00'::timestamp, '2026-09-23 08:50'::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(9003, '{"nombres": "Valeria", "apellidos": "Cruz Méndez", "tipoDocumento": "C.C.", "numeroDocumento": "1.099.331.208", "celular": "318 455 2093", "ciudad": "Bucaramanga", "formaciones": [], "experiencias": [], "referencias": [], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 3}'::jsonb, 3, '2026-2', 'Institución Educativa Las Américas', 'María Patricia Flores',
        '2026-10-05'::date, '2026-11-13'::date, 'APROBADA', '2026-09-19 17:10'::timestamp, '2026-09-22 09:30'::timestamp, NULL::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(9004, '{"nombres": "Daniela", "apellidos": "Ariza Toloza", "tipoDocumento": "C.C.", "numeroDocumento": "1.098.120.775", "celular": "301 778 4410", "ciudad": "Bucaramanga", "formaciones": [], "experiencias": [], "referencias": [], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 3}'::jsonb, 3, '2026-2', 'Centro de Desarrollo Infantil Villa del Sol', 'Luz Dary Cáceres',
        '2026-10-05'::date, '2026-11-13'::date, 'APROBADA', '2026-09-17 12:00'::timestamp, '2026-09-23 16:45'::timestamp, NULL::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(9005, '{"nombres": "Sofía", "apellidos": "Mantilla Reyes", "tipoDocumento": "C.C.", "numeroDocumento": "1.005.432.876", "celular": "320 114 9032", "ciudad": "Bucaramanga", "formaciones": [], "experiencias": [], "referencias": [], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 3}'::jsonb, 3, '2026-2', 'Institución Educativa Santander', 'Gloria Inés Parra',
        '2026-10-05'::date, '2026-11-13'::date, 'ENVIADA', '2026-09-24 08:15'::timestamp, NULL::timestamp, NULL::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(9006, '{"nombres": "Mariana", "apellidos": "Duarte Gélvez", "tipoDocumento": "C.C.", "numeroDocumento": "1.098.556.019", "celular": "316 903 2251", "ciudad": "Bucaramanga", "formaciones": [], "experiencias": [], "referencias": [], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 3}'::jsonb, 3, '2026-2', 'Institución Educativa Las Américas', 'María Patricia Flores',
        '2026-10-05'::date, '2026-11-13'::date, 'ENVIADA', '2026-09-23 19:40'::timestamp, NULL::timestamp, NULL::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(9007, '{"nombres": "Sara", "apellidos": "Villamizar León", "tipoDocumento": "T.I.", "numeroDocumento": "1.097.204.388", "celular": "312 448 7790", "ciudad": "Bucaramanga", "formaciones": [], "experiencias": [], "referencias": [], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 1}'::jsonb, 1, '2026-2', 'Jardín Infantil Los Cerezos', 'Marta Rueda',
        '2026-10-05'::date, '2026-11-13'::date, 'ENVIADA', '2026-09-22 10:05'::timestamp, NULL::timestamp, NULL::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(9008, '{"nombres": "Natalia", "apellidos": "Serrano Díaz", "tipoDocumento": "C.C.", "numeroDocumento": "1.102.358.664", "celular": "319 220 5546", "ciudad": "Bucaramanga", "formaciones": [], "experiencias": [], "referencias": [], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 2}'::jsonb, 2, '2026-2', 'Institución Educativa Dámaso Zapata', 'Rubén Darío Quintero',
        '2026-10-05'::date, '2026-11-13'::date, 'ENVIADA', '2026-09-24 11:25'::timestamp, NULL::timestamp, NULL::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(9009, '{"nombres": "Juliana", "apellidos": "Prada Quintero", "tipoDocumento": "C.C.", "numeroDocumento": "1.098.774.312", "celular": "317 336 0128", "ciudad": "Bucaramanga", "formaciones": [], "experiencias": [], "referencias": [], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 4}'::jsonb, 4, '2026-2', 'Ludoteca Comunitaria Los Naranjos', 'Yolanda Becerra',
        '2026-10-05'::date, '2026-11-13'::date, 'APROBADA', '2026-09-15 15:00'::timestamp, '2026-09-21 10:40'::timestamp, NULL::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(9010, '{"nombres": "Paula Andrea", "apellidos": "Ríos Castro", "tipoDocumento": "C.C.", "numeroDocumento": "1.095.991.437", "celular": "314 287 6603", "ciudad": "Bucaramanga", "formaciones": [], "experiencias": [], "referencias": [], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 2}'::jsonb, 2, '2026-1', 'Hogar Infantil Pequeños Exploradores', 'Martha Cecilia Rojas',
        '2026-04-06'::date, '2026-05-15'::date, 'AVALADA', '2026-03-09 09:30'::timestamp, '2026-03-12 14:00'::timestamp, '2026-03-16 11:20'::timestamp,
        NULL, tutor, director);
    PERFORM pg_temp.inscribir(9010, '{"nombres": "Paula Andrea", "apellidos": "Ríos Castro", "tipoDocumento": "C.C.", "numeroDocumento": "1.095.991.437", "celular": "314 287 6603", "ciudad": "Bucaramanga", "formaciones": [], "experiencias": [], "referencias": [], "nombrePrograma": "Licenciatura en Educación Infantil", "semestreCursado": 3}'::jsonb, 3, '2026-2', NULL, NULL,
        NULL::date, NULL::date, 'BORRADOR', NULL::timestamp, NULL::timestamp, NULL::timestamp,
        NULL, tutor, director);
END $$;

-- Las inscripciones de la demostración, con su estado
SELECT s.codigo AS semestre, p.orden AS practica, d.nombres || ' ' || d.apellidos AS estudiante,
       d.institucion_razon_social AS institucion, i.estado
  FROM legalizacion.inscripcion i
  JOIN legalizacion.semestre s ON s.id = i.semestre_id
  JOIN legalizacion.practica p ON p.id = i.practica_id
  JOIN legalizacion.inscripcion_datos d ON d.inscripcion_id = i.id
 ORDER BY s.codigo DESC, p.orden, estudiante;
