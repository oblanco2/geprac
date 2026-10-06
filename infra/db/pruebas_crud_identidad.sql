-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · Pruebas CRUD del esquema identidad (MS-01)
--
-- Crea, consulta, actualiza y elimina un registro de prueba en cada una
-- de las seis tablas del esquema, y comprueba las restricciones del
-- modelo relacional (punto 8.2): unicidad, claves foráneas y campos
-- obligatorios.
--
-- Dónde se ejecuta: SQL Editor del proyecto de Supabase de MS-01
-- (organización GEPRAC). Se pega completo y se da Run.
--
-- La base queda igual que antes: todas las pruebas corren dentro de un
-- bloque que se deshace al final, y las funciones de apoyo son
-- temporales (desaparecen al cerrar la sesión). Se puede ejecutar las
-- veces que haga falta.
--
-- Cómo leer el resultado: una fila por prueba. En las de CRUD, C, R, U y
-- D valen 1 cuando la operación afectó exactamente un registro; en las
-- de integridad, «rechazada por» nombra la restricción que detuvo el
-- cambio. Todas las filas deben decir OK.
--
-- Autor: Darien Asdrwal Pesca Ojeda · modelo de datos
-- ══════════════════════════════════════════════════════════════════

-- Ejecuta una sentencia de prueba de integridad y devuelve qué pasó:
-- «aceptada», o la restricción que la detuvo. En los dos casos deshace la
-- sentencia, para que una prueba no afecte a las siguientes.
CREATE OR REPLACE FUNCTION pg_temp.intentar(sentencia text) RETURNS text
LANGUAGE plpgsql AS $$
DECLARE
    restriccion text;
    columna     text;
BEGIN
    EXECUTE sentencia;
    RAISE EXCEPTION USING ERRCODE = 'PZ998', MESSAGE = 'sentencia aceptada';
EXCEPTION
    WHEN SQLSTATE 'PZ998' THEN
        RETURN 'aceptada';
    WHEN integrity_constraint_violation THEN
        GET STACKED DIAGNOSTICS restriccion = CONSTRAINT_NAME, columna = COLUMN_NAME;
        RETURN 'rechazada por ' || coalesce(nullif(restriccion, ''), columna || ' (obligatorio)');
END $$;

-- Anota cuántos registros afectó una operación (C, R, U o D) en una tabla.
CREATE OR REPLACE FUNCTION pg_temp.anotar(conteo jsonb, tabla text, operacion text, filas bigint) RETURNS jsonb
LANGUAGE sql AS $$
    SELECT jsonb_set(conteo, ARRAY[tabla], coalesce(conteo -> tabla, '{}') || jsonb_build_object(operacion, filas))
$$;

CREATE OR REPLACE FUNCTION pg_temp.pruebas_crud_identidad()
RETURNS TABLE (n int, tabla text, prueba text, esperado text, obtenido text, resultado text)
LANGUAGE plpgsql AS $$
#variable_conflict use_column
DECLARE
    tablas      constant text[] := ARRAY['usuario', 'programa', 'estudiante',
                                         'formacion_academica', 'experiencia_laboral', 'referencia_personal'];
    conteo      jsonb := '{}';
    integridad  jsonb := '[]';
    filas_crud  jsonb;
    k           bigint;
    v_usuario   uuid := gen_random_uuid();
    v_otro      uuid := gen_random_uuid();
    v_est       bigint;
    v_form      bigint;
    v_exp       bigint;
    v_ref       bigint;
BEGIN
    BEGIN
        -- ── Crear (C) y consultar (R) ────────────────────────────────
        INSERT INTO identidad.usuario (id, nombre_presentacion, correo_institucional, rol, creado_en)
        VALUES (v_usuario, 'Laura Prueba', 'prueba.crud@udi.edu.co', NULL, localtimestamp);
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'usuario', 'C', k);
        SELECT count(*) INTO k FROM identidad.usuario WHERE id = v_usuario;
        conteo := pg_temp.anotar(conteo, 'usuario', 'R', k);
        -- otra cuenta, sin perfil, que usan las pruebas de integridad del estudiante
        INSERT INTO identidad.usuario (id, nombre_presentacion, correo_institucional, rol, creado_en)
        VALUES (v_otro, 'Cuenta auxiliar', 'auxiliar.crud@udi.edu.co', NULL, localtimestamp);

        INSERT INTO identidad.programa (codigo, nombre) VALUES ('PRB', 'Programa de prueba');
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'programa', 'C', k);
        SELECT count(*) INTO k FROM identidad.programa WHERE codigo = 'PRB';
        conteo := pg_temp.anotar(conteo, 'programa', 'R', k);

        INSERT INTO identidad.estudiante (usuario_id, nombres, apellidos, tipo_documento, numero_documento,
                                          codigo_programa, semestre_actual, creado_en)
        VALUES (v_usuario, 'Laura', 'Prueba Gómez', 'C.C.', '1000000001', 'PRB', 3, localtimestamp)
        RETURNING id INTO v_est;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'estudiante', 'C', k);
        SELECT count(*) INTO k FROM identidad.estudiante WHERE id = v_est;
        conteo := pg_temp.anotar(conteo, 'estudiante', 'R', k);

        INSERT INTO identidad.formacion_academica (estudiante_id, tipo, institucion, nombre, anio)
        VALUES (v_est, 'Secundaria', 'Colegio de prueba', 'Bachiller académico', 2022)
        RETURNING id INTO v_form;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'formacion_academica', 'C', k);
        SELECT count(*) INTO k FROM identidad.formacion_academica WHERE id = v_form;
        conteo := pg_temp.anotar(conteo, 'formacion_academica', 'R', k);

        INSERT INTO identidad.experiencia_laboral (estudiante_id, empresa, cargo, fecha_inicio)
        VALUES (v_est, 'Empresa de prueba', 'Auxiliar de aula', DATE '2024-01-15')
        RETURNING id INTO v_exp;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'experiencia_laboral', 'C', k);
        SELECT count(*) INTO k FROM identidad.experiencia_laboral WHERE id = v_exp;
        conteo := pg_temp.anotar(conteo, 'experiencia_laboral', 'R', k);

        INSERT INTO identidad.referencia_personal (estudiante_id, nombre, empresa, cargo, telefono, ciudad)
        VALUES (v_est, 'Sr. Carlos Prueba', 'Empresa de prueba', 'Docente', '3000000000', 'Bucaramanga')
        RETURNING id INTO v_ref;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'referencia_personal', 'C', k);
        SELECT count(*) INTO k FROM identidad.referencia_personal WHERE id = v_ref;
        conteo := pg_temp.anotar(conteo, 'referencia_personal', 'R', k);

        -- ── Actualizar (U) ───────────────────────────────────────────
        UPDATE identidad.usuario SET rol = 'ESTUDIANTE' WHERE id = v_usuario;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'usuario', 'U', k);
        UPDATE identidad.programa SET nombre = 'Programa de prueba actualizado' WHERE codigo = 'PRB';
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'programa', 'U', k);
        UPDATE identidad.estudiante SET celular = '3000000001', actualizado_en = localtimestamp WHERE id = v_est;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'estudiante', 'U', k);
        UPDATE identidad.formacion_academica SET anio = 2023 WHERE id = v_form;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'formacion_academica', 'U', k);
        UPDATE identidad.experiencia_laboral SET fecha_fin = DATE '2024-12-15' WHERE id = v_exp;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'experiencia_laboral', 'U', k);
        UPDATE identidad.referencia_personal SET telefono = '3000000002' WHERE id = v_ref;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'referencia_personal', 'U', k);

        -- ── Integridad: restricciones del punto 8.2 ──────────────────
        integridad := jsonb_build_array(
          jsonb_build_object('tabla', 'usuario', 'prueba', 'Crear un usuario sin rol asignado',
            'esperado', 'aceptada',
            'obtenido', pg_temp.intentar(
              $s$INSERT INTO identidad.usuario VALUES (gen_random_uuid(), 'Sin rol', 'sin.rol@udi.edu.co', NULL, localtimestamp)$s$)),
          jsonb_build_object('tabla', 'usuario', 'prueba', 'Repetir el correo institucional',
            'esperado', 'rechazada por uq_usuario_correo_institucional',
            'obtenido', pg_temp.intentar(
              $s$INSERT INTO identidad.usuario VALUES (gen_random_uuid(), 'Otra', 'prueba.crud@udi.edu.co', NULL, localtimestamp)$s$)),
          jsonb_build_object('tabla', 'estudiante', 'prueba', 'Repetir el tipo y el número de documento',
            'esperado', 'rechazada por uq_estudiante_documento',
            'obtenido', pg_temp.intentar(format(
              $s$INSERT INTO identidad.estudiante (usuario_id, nombres, apellidos, tipo_documento, numero_documento,
                 codigo_programa, semestre_actual, creado_en)
                 VALUES (%L, 'Otra', 'Persona', 'C.C.', '1000000001', 'PRB', 1, localtimestamp)$s$, v_otro))),
          jsonb_build_object('tabla', 'estudiante', 'prueba', 'Dar un segundo perfil a la misma cuenta',
            'esperado', 'rechazada por uq_estudiante_usuario',
            'obtenido', pg_temp.intentar(format(
              $s$INSERT INTO identidad.estudiante (usuario_id, nombres, apellidos, tipo_documento, numero_documento,
                 codigo_programa, semestre_actual, creado_en)
                 VALUES (%L, 'Laura', 'Prueba', 'T.I.', '1000000002', 'PRB', 3, localtimestamp)$s$, v_usuario))),
          jsonb_build_object('tabla', 'estudiante', 'prueba', 'Asignar un programa que no existe',
            'esperado', 'rechazada por fk_estudiante_programa',
            'obtenido', pg_temp.intentar(format(
              $s$INSERT INTO identidad.estudiante (usuario_id, nombres, apellidos, tipo_documento, numero_documento,
                 codigo_programa, semestre_actual, creado_en)
                 VALUES (%L, 'Otra', 'Persona', 'C.C.', '1000000003', 'NOEXISTE', 1, localtimestamp)$s$, v_otro))),
          jsonb_build_object('tabla', 'estudiante', 'prueba', 'Crear un estudiante sin nombres',
            'esperado', 'rechazada por nombres (obligatorio)',
            'obtenido', pg_temp.intentar(format(
              $s$INSERT INTO identidad.estudiante (usuario_id, nombres, apellidos, tipo_documento, numero_documento,
                 codigo_programa, semestre_actual, creado_en)
                 VALUES (%L, NULL, 'Persona', 'C.C.', '1000000004', 'PRB', 1, localtimestamp)$s$, v_otro))),
          jsonb_build_object('tabla', 'formacion_academica', 'prueba', 'Registrar formación de un estudiante que no existe',
            'esperado', 'rechazada por fk_formacion_academica_estudiante',
            'obtenido', pg_temp.intentar(
              $s$INSERT INTO identidad.formacion_academica (estudiante_id, tipo, institucion, nombre)
                 VALUES (-1, 'Otros', 'Ninguna', 'Ninguno')$s$)),
          jsonb_build_object('tabla', 'usuario', 'prueba', 'Eliminar un usuario que tiene perfil de estudiante',
            'esperado', 'rechazada por fk_estudiante_usuario',
            'obtenido', pg_temp.intentar(format($s$DELETE FROM identidad.usuario WHERE id = %L$s$, v_usuario))),
          jsonb_build_object('tabla', 'programa', 'prueba', 'Eliminar un programa que tiene estudiantes',
            'esperado', 'rechazada por fk_estudiante_programa',
            'obtenido', pg_temp.intentar($s$DELETE FROM identidad.programa WHERE codigo = 'PRB'$s$))
        );

        -- ── Eliminar (D): primero las tablas hijas ───────────────────
        DELETE FROM identidad.referencia_personal WHERE id = v_ref;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'referencia_personal', 'D', k);
        DELETE FROM identidad.experiencia_laboral WHERE id = v_exp;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'experiencia_laboral', 'D', k);
        DELETE FROM identidad.formacion_academica WHERE id = v_form;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'formacion_academica', 'D', k);
        DELETE FROM identidad.estudiante WHERE id = v_est;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'estudiante', 'D', k);
        DELETE FROM identidad.programa WHERE codigo = 'PRB';
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'programa', 'D', k);
        DELETE FROM identidad.usuario WHERE id = v_usuario;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'usuario', 'D', k);
        DELETE FROM identidad.usuario WHERE id = v_otro;

        SELECT jsonb_agg(jsonb_build_object(
                   'tabla', t, 'prueba', 'Crear, consultar, actualizar y eliminar',
                   'esperado', 'C=1 R=1 U=1 D=1',
                   'obtenido', format('C=%s R=%s U=%s D=%s', conteo->t->>'C', conteo->t->>'R',
                                      conteo->t->>'U', conteo->t->>'D')) ORDER BY o)
        INTO filas_crud
        FROM unnest(tablas) WITH ORDINALITY AS x(t, o);

        -- Deshace todo lo anterior: la base queda como estaba.
        RAISE EXCEPTION USING ERRCODE = 'PZ999', MESSAGE = 'fin de las pruebas';
    EXCEPTION WHEN SQLSTATE 'PZ999' THEN
        NULL;
    END;

    RETURN QUERY
    SELECT e.ord::int, e.f->>'tabla', e.f->>'prueba', e.f->>'esperado', e.f->>'obtenido',
           CASE WHEN e.f->>'esperado' = e.f->>'obtenido' THEN 'OK' ELSE 'FALLA' END
    FROM jsonb_array_elements(filas_crud || integridad) WITH ORDINALITY AS e(f, ord)
    ORDER BY e.ord;
END $$;

SELECT * FROM pg_temp.pruebas_crud_identidad();
