-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · Pruebas CRUD del esquema legalizacion (MS-02)
--
-- Crea, consulta, actualiza y elimina un registro de prueba en cada una
-- de las dieciocho tablas del esquema, y comprueba las restricciones del
-- modelo relacional (punto 8.2): unicidad, índices únicos parciales,
-- verificación del motivo de la revisión, claves foráneas y campos
-- obligatorios.
--
-- Dónde se ejecuta: SQL Editor del proyecto de Supabase de MS-02
-- (organización GEPRAC MS-02). Se pega completo y se da Run.
--
-- La base queda igual que antes: todas las pruebas corren dentro de un
-- bloque que se deshace al final, y las funciones de apoyo son
-- temporales (desaparecen al cerrar la sesión). Se puede ejecutar las
-- veces que haga falta, también cuando ya haya datos reales: los
-- registros de prueba usan valores que no chocan con ellos.
--
-- Cómo leer el resultado: una fila por prueba. En las de CRUD, C, R, U y
-- D valen 1 cuando la operación afectó exactamente un registro; en las
-- de integridad, «rechazada por» nombra la restricción que detuvo el
-- cambio. Todas las filas deben decir OK.
--
-- Autor: Darien Asdrwal Pesca Ojeda · modelo de datos y MS-02
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

-- Arma una fila de resultado de una prueba de integridad.
CREATE OR REPLACE FUNCTION pg_temp.fila(tabla text, prueba text, esperado text, obtenido text) RETURNS jsonb
LANGUAGE sql AS $$
    SELECT jsonb_build_array(jsonb_build_object('tabla', tabla, 'prueba', prueba,
                                                'esperado', esperado, 'obtenido', obtenido))
$$;

CREATE OR REPLACE FUNCTION pg_temp.pruebas_crud_legalizacion()
RETURNS TABLE (n int, tabla text, prueba text, esperado text, obtenido text, resultado text)
LANGUAGE plpgsql AS $$
#variable_conflict use_column
DECLARE
    tablas      constant text[] := ARRAY['semestre', 'practica', 'objetivo_practica', 'actividad_practica',
        'institucion', 'contacto_institucion', 'tutor_practica', 'inscripcion', 'inscripcion_datos',
        'inscripcion_objetivo', 'inscripcion_actividad', 'inscripcion_formacion', 'inscripcion_experiencia',
        'inscripcion_referencia', 'revision', 'aval', 'plantilla_formato', 'formato_generado'];
    conteo      jsonb := '{}';
    integridad  jsonb := '[]';
    filas_crud  jsonb;
    k           bigint;
    v_sem bigint; v_prac bigint; v_obj bigint; v_act bigint; v_inst bigint; v_cont bigint;
    v_tutor bigint; v_ins bigint; v_iobj bigint; v_iact bigint; v_iform bigint; v_iexp bigint;
    v_iref bigint; v_rev bigint; v_aval bigint; v_plant bigint; v_form bigint;
BEGIN
    BEGIN
        -- ── Crear (C) y consultar (R) ────────────────────────────────
        INSERT INTO legalizacion.semestre (codigo, fecha_inicio, fecha_cierre, abierto)
        VALUES ('2099-1', DATE '2099-02-01', DATE '2099-06-15', false)
        RETURNING id INTO v_sem;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'semestre', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.semestre WHERE id = v_sem;
        conteo := pg_temp.anotar(conteo, 'semestre', 'R', k);

        INSERT INTO legalizacion.practica (codigo_programa, orden, nombre, objetivo_general, horario_estandar, activa)
        VALUES ('PRB', 1, 'Práctica de prueba', 'Objetivo general de prueba', 'Lunes de 7:00 a 11:00', true)
        RETURNING id INTO v_prac;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'practica', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.practica WHERE id = v_prac;
        conteo := pg_temp.anotar(conteo, 'practica', 'R', k);

        INSERT INTO legalizacion.objetivo_practica (practica_id, posicion, texto)
        VALUES (v_prac, 1, 'Objetivo específico de prueba')
        RETURNING id INTO v_obj;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'objetivo_practica', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.objetivo_practica WHERE id = v_obj;
        conteo := pg_temp.anotar(conteo, 'objetivo_practica', 'R', k);

        INSERT INTO legalizacion.actividad_practica (practica_id, posicion, texto)
        VALUES (v_prac, 1, 'Actividad de prueba')
        RETURNING id INTO v_act;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'actividad_practica', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.actividad_practica WHERE id = v_act;
        conteo := pg_temp.anotar(conteo, 'actividad_practica', 'R', k);

        INSERT INTO legalizacion.institucion (razon_social, nit, direccion, ciudad, telefono, activa)
        VALUES ('Institución de prueba GEPRAC', NULL, 'Calle 1 # 2-3', 'Bucaramanga', '6070000000', true)
        RETURNING id INTO v_inst;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'institucion', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.institucion WHERE id = v_inst;
        conteo := pg_temp.anotar(conteo, 'institucion', 'R', k);

        INSERT INTO legalizacion.contacto_institucion (institucion_id, nombre, cargo, telefono)
        VALUES (v_inst, 'Marta Prueba', 'Coordinadora', '6070000001')
        RETURNING id INTO v_cont;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'contacto_institucion', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.contacto_institucion WHERE id = v_cont;
        conteo := pg_temp.anotar(conteo, 'contacto_institucion', 'R', k);

        INSERT INTO legalizacion.tutor_practica (semestre_id, practica_id, tutor_id)
        VALUES (v_sem, v_prac, gen_random_uuid())
        RETURNING id INTO v_tutor;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'tutor_practica', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.tutor_practica WHERE id = v_tutor;
        conteo := pg_temp.anotar(conteo, 'tutor_practica', 'R', k);

        INSERT INTO legalizacion.inscripcion (estudiante_id, practica_id, semestre_id, institucion_id, contacto_id,
                                              fecha_inicio, fecha_fin, estado, creada_en)
        VALUES (999999, v_prac, v_sem, v_inst, v_cont, DATE '2099-03-02', DATE '2099-04-30', 'BORRADOR', localtimestamp)
        RETURNING id INTO v_ins;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.inscripcion WHERE id = v_ins;
        conteo := pg_temp.anotar(conteo, 'inscripcion', 'R', k);

        INSERT INTO legalizacion.inscripcion_datos (inscripcion_id, nombres, apellidos, tipo_documento, numero_documento,
                                                    nombre_programa, semestre_cursado, nombre_practica, objetivo_general)
        VALUES (v_ins, 'Laura', 'Prueba Gómez', 'C.C.', '1000000001', 'Programa de prueba', 3,
                'Práctica de prueba', 'Objetivo general de prueba');
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_datos', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.inscripcion_datos WHERE inscripcion_id = v_ins;
        conteo := pg_temp.anotar(conteo, 'inscripcion_datos', 'R', k);

        INSERT INTO legalizacion.inscripcion_objetivo (inscripcion_id, posicion, texto)
        VALUES (v_ins, 1, 'Objetivo específico de prueba')
        RETURNING id INTO v_iobj;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_objetivo', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.inscripcion_objetivo WHERE id = v_iobj;
        conteo := pg_temp.anotar(conteo, 'inscripcion_objetivo', 'R', k);

        INSERT INTO legalizacion.inscripcion_actividad (inscripcion_id, posicion, texto)
        VALUES (v_ins, 1, 'Actividad de prueba')
        RETURNING id INTO v_iact;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_actividad', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.inscripcion_actividad WHERE id = v_iact;
        conteo := pg_temp.anotar(conteo, 'inscripcion_actividad', 'R', k);

        INSERT INTO legalizacion.inscripcion_formacion (inscripcion_id, tipo, institucion, nombre, anio)
        VALUES (v_ins, 'Secundaria', 'Colegio de prueba', 'Bachiller académico', 2022)
        RETURNING id INTO v_iform;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_formacion', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.inscripcion_formacion WHERE id = v_iform;
        conteo := pg_temp.anotar(conteo, 'inscripcion_formacion', 'R', k);

        INSERT INTO legalizacion.inscripcion_experiencia (inscripcion_id, empresa, cargo, fecha_inicio)
        VALUES (v_ins, 'Empresa de prueba', 'Auxiliar de aula', DATE '2024-01-15')
        RETURNING id INTO v_iexp;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_experiencia', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.inscripcion_experiencia WHERE id = v_iexp;
        conteo := pg_temp.anotar(conteo, 'inscripcion_experiencia', 'R', k);

        INSERT INTO legalizacion.inscripcion_referencia (inscripcion_id, nombre, empresa, cargo, telefono, ciudad)
        VALUES (v_ins, 'Sr. Carlos Prueba', 'Empresa de prueba', 'Docente', '3000000000', 'Bucaramanga')
        RETURNING id INTO v_iref;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_referencia', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.inscripcion_referencia WHERE id = v_iref;
        conteo := pg_temp.anotar(conteo, 'inscripcion_referencia', 'R', k);

        INSERT INTO legalizacion.revision (inscripcion_id, revisor_id, resultado, motivo, fecha)
        VALUES (v_ins, gen_random_uuid(), 'APROBADA', NULL, localtimestamp)
        RETURNING id INTO v_rev;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'revision', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.revision WHERE id = v_rev;
        conteo := pg_temp.anotar(conteo, 'revision', 'R', k);

        INSERT INTO legalizacion.aval (inscripcion_id, director_id, fecha)
        VALUES (v_ins, gen_random_uuid(), localtimestamp)
        RETURNING id INTO v_aval;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'aval', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.aval WHERE id = v_aval;
        conteo := pg_temp.anotar(conteo, 'aval', 'R', k);

        INSERT INTO legalizacion.plantilla_formato (tipo, version, vigente, archivo, creada_en)
        VALUES ('PR-01', 'prueba', false, 'plantillas/PR-01-prueba.docx', localtimestamp)
        RETURNING id INTO v_plant;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'plantilla_formato', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.plantilla_formato WHERE id = v_plant;
        conteo := pg_temp.anotar(conteo, 'plantilla_formato', 'R', k);

        INSERT INTO legalizacion.formato_generado (inscripcion_id, tipo, plantilla_id, archivo, fecha_emision)
        VALUES (v_ins, 'PR-01', v_plant, 'emitidos/prueba/PR-01.pdf', localtimestamp)
        RETURNING id INTO v_form;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'formato_generado', 'C', k);
        SELECT count(*) INTO k FROM legalizacion.formato_generado WHERE id = v_form;
        conteo := pg_temp.anotar(conteo, 'formato_generado', 'R', k);

        -- ── Actualizar (U) ───────────────────────────────────────────
        UPDATE legalizacion.semestre SET fecha_cierre = DATE '2099-06-30' WHERE id = v_sem;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'semestre', 'U', k);
        UPDATE legalizacion.practica SET horario_estandar = 'Martes de 7:00 a 11:00' WHERE id = v_prac;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'practica', 'U', k);
        UPDATE legalizacion.objetivo_practica SET texto = 'Objetivo específico corregido' WHERE id = v_obj;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'objetivo_practica', 'U', k);
        UPDATE legalizacion.actividad_practica SET posicion = 2 WHERE id = v_act;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'actividad_practica', 'U', k);
        UPDATE legalizacion.institucion SET correo = 'contacto@prueba.edu.co' WHERE id = v_inst;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'institucion', 'U', k);
        UPDATE legalizacion.contacto_institucion SET celular = '3000000003' WHERE id = v_cont;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'contacto_institucion', 'U', k);
        UPDATE legalizacion.tutor_practica SET tutor_id = gen_random_uuid() WHERE id = v_tutor;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'tutor_practica', 'U', k);
        UPDATE legalizacion.inscripcion SET estado = 'ENVIADA', enviada_en = localtimestamp WHERE id = v_ins;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion', 'U', k);
        UPDATE legalizacion.inscripcion_datos SET celular = '3000000004' WHERE inscripcion_id = v_ins;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_datos', 'U', k);
        UPDATE legalizacion.inscripcion_objetivo SET texto = 'Objetivo específico corregido' WHERE id = v_iobj;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_objetivo', 'U', k);
        UPDATE legalizacion.inscripcion_actividad SET texto = 'Actividad corregida' WHERE id = v_iact;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_actividad', 'U', k);
        UPDATE legalizacion.inscripcion_formacion SET anio = 2023 WHERE id = v_iform;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_formacion', 'U', k);
        UPDATE legalizacion.inscripcion_experiencia SET fecha_fin = DATE '2024-12-15' WHERE id = v_iexp;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_experiencia', 'U', k);
        UPDATE legalizacion.inscripcion_referencia SET telefono = '3000000005' WHERE id = v_iref;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_referencia', 'U', k);
        UPDATE legalizacion.revision SET resultado = 'DEVUELTA', motivo = 'Falta el horario de la práctica' WHERE id = v_rev;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'revision', 'U', k);
        UPDATE legalizacion.aval SET fecha = localtimestamp WHERE id = v_aval;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'aval', 'U', k);
        UPDATE legalizacion.plantilla_formato SET version = 'prueba-2' WHERE id = v_plant;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'plantilla_formato', 'U', k);
        UPDATE legalizacion.formato_generado SET archivo = 'emitidos/prueba/PR-01-v2.pdf' WHERE id = v_form;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'formato_generado', 'U', k);

        -- ── Integridad: restricciones del punto 8.2 ──────────────────
        integridad := integridad || pg_temp.fila('semestre', 'Repetir el código del semestre',
            'rechazada por uq_semestre_codigo',
            pg_temp.intentar($s$INSERT INTO legalizacion.semestre (codigo, fecha_inicio, fecha_cierre, abierto)
                                VALUES ('2099-1', DATE '2099-08-01', DATE '2099-12-01', false)$s$));

        -- Para probar que solo hay un semestre abierto hace falta uno abierto:
        -- si la base no tiene ninguno, se abre el de prueba.
        IF NOT EXISTS (SELECT 1 FROM legalizacion.semestre WHERE abierto) THEN
            UPDATE legalizacion.semestre SET abierto = true WHERE id = v_sem;
        END IF;
        integridad := integridad || pg_temp.fila('semestre', 'Abrir un segundo semestre',
            'rechazada por ux_semestre_abierto',
            pg_temp.intentar($s$INSERT INTO legalizacion.semestre (codigo, fecha_inicio, fecha_cierre, abierto)
                                VALUES ('2099-2', DATE '2099-08-01', DATE '2099-12-01', true)$s$));

        integridad := integridad || pg_temp.fila('practica', 'Ocupar una posición del plan que ya tiene práctica',
            'rechazada por uq_practica_posicion',
            pg_temp.intentar($s$INSERT INTO legalizacion.practica (codigo_programa, orden, nombre, objetivo_general, activa)
                                VALUES ('PRB', 1, 'Otra práctica', 'Otro objetivo', true)$s$));

        integridad := integridad || pg_temp.fila('practica', 'Crear una práctica sin nombre',
            'rechazada por nombre (obligatorio)',
            pg_temp.intentar($s$INSERT INTO legalizacion.practica (codigo_programa, orden, nombre, objetivo_general, activa)
                                VALUES ('PRB', 2, NULL, 'Otro objetivo', true)$s$));

        integridad := integridad || pg_temp.fila('institucion', 'Repetir la institución en la misma ciudad',
            'rechazada por uq_institucion_nombre_ciudad',
            pg_temp.intentar($s$INSERT INTO legalizacion.institucion (razon_social, direccion, ciudad, telefono, activa)
                                VALUES ('Institución de prueba GEPRAC', 'Otra dirección', 'Bucaramanga', '6070000002', true)$s$));

        integridad := integridad || pg_temp.fila('institucion', 'Registrar el mismo nombre en otra ciudad, sin NIT',
            'aceptada',
            pg_temp.intentar($s$INSERT INTO legalizacion.institucion (razon_social, direccion, ciudad, telefono, activa)
                                VALUES ('Institución de prueba GEPRAC', 'Otra dirección', 'Floridablanca', '6070000003', true)$s$));

        integridad := integridad || pg_temp.fila('tutor_practica', 'Designar un segundo tutor para la misma práctica y semestre',
            'rechazada por uq_tutor_practica_semestre',
            pg_temp.intentar(format($s$INSERT INTO legalizacion.tutor_practica (semestre_id, practica_id, tutor_id)
                                       VALUES (%s, %s, gen_random_uuid())$s$, v_sem, v_prac)));

        integridad := integridad || pg_temp.fila('inscripcion', 'Inscribir otra vez la misma práctica en el mismo semestre',
            'rechazada por uq_inscripcion_periodo',
            pg_temp.intentar(format($s$INSERT INTO legalizacion.inscripcion (estudiante_id, practica_id, semestre_id, estado, creada_en)
                                       VALUES (999999, %s, %s, 'BORRADOR', localtimestamp)$s$, v_prac, v_sem)));

        integridad := integridad || pg_temp.fila('inscripcion', 'Inscribir una práctica que no existe',
            'rechazada por fk_inscripcion_practica',
            pg_temp.intentar(format($s$INSERT INTO legalizacion.inscripcion (estudiante_id, practica_id, semestre_id, estado, creada_en)
                                       VALUES (999998, -1, %s, 'BORRADOR', localtimestamp)$s$, v_sem)));

        integridad := integridad || pg_temp.fila('revision', 'Devolver una inscripción sin motivo',
            'rechazada por ck_revision_motivo',
            pg_temp.intentar(format($s$INSERT INTO legalizacion.revision (inscripcion_id, revisor_id, resultado, motivo, fecha)
                                       VALUES (%s, gen_random_uuid(), 'DEVUELTA', NULL, localtimestamp)$s$, v_ins)));

        integridad := integridad || pg_temp.fila('revision', 'Devolver una inscripción con el motivo en blanco',
            'rechazada por ck_revision_motivo',
            pg_temp.intentar(format($s$INSERT INTO legalizacion.revision (inscripcion_id, revisor_id, resultado, motivo, fecha)
                                       VALUES (%s, gen_random_uuid(), 'DEVUELTA', '   ', localtimestamp)$s$, v_ins)));

        integridad := integridad || pg_temp.fila('aval', 'Avalar dos veces la misma inscripción',
            'rechazada por uq_aval_inscripcion',
            pg_temp.intentar(format($s$INSERT INTO legalizacion.aval (inscripcion_id, director_id, fecha)
                                       VALUES (%s, gen_random_uuid(), localtimestamp)$s$, v_ins)));

        -- Igual que con el semestre: si no hay plantilla PR-01 vigente, la de
        -- prueba pasa a serlo.
        IF NOT EXISTS (SELECT 1 FROM legalizacion.plantilla_formato WHERE tipo = 'PR-01' AND vigente) THEN
            UPDATE legalizacion.plantilla_formato SET vigente = true WHERE id = v_plant;
        END IF;
        integridad := integridad || pg_temp.fila('plantilla_formato', 'Tener dos plantillas vigentes del mismo formato',
            'rechazada por ux_plantilla_formato_vigente',
            pg_temp.intentar($s$INSERT INTO legalizacion.plantilla_formato (tipo, version, vigente, archivo, creada_en)
                                VALUES ('PR-01', 'prueba-3', true, 'plantillas/PR-01-otra.docx', localtimestamp)$s$));

        integridad := integridad || pg_temp.fila('formato_generado', 'Emitir dos veces el mismo formato para una inscripción',
            'rechazada por uq_formato_generado_tipo',
            pg_temp.intentar(format($s$INSERT INTO legalizacion.formato_generado (inscripcion_id, tipo, plantilla_id, archivo, fecha_emision)
                                       VALUES (%s, 'PR-01', %s, 'emitidos/prueba/otro.pdf', localtimestamp)$s$, v_ins, v_plant)));

        -- ── Eliminar (D): primero las tablas hijas ───────────────────
        DELETE FROM legalizacion.formato_generado WHERE id = v_form;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'formato_generado', 'D', k);
        DELETE FROM legalizacion.aval WHERE id = v_aval;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'aval', 'D', k);
        DELETE FROM legalizacion.revision WHERE id = v_rev;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'revision', 'D', k);
        DELETE FROM legalizacion.inscripcion_referencia WHERE id = v_iref;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_referencia', 'D', k);
        DELETE FROM legalizacion.inscripcion_experiencia WHERE id = v_iexp;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_experiencia', 'D', k);
        DELETE FROM legalizacion.inscripcion_formacion WHERE id = v_iform;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_formacion', 'D', k);
        DELETE FROM legalizacion.inscripcion_actividad WHERE id = v_iact;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_actividad', 'D', k);
        DELETE FROM legalizacion.inscripcion_objetivo WHERE id = v_iobj;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_objetivo', 'D', k);
        DELETE FROM legalizacion.inscripcion_datos WHERE inscripcion_id = v_ins;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion_datos', 'D', k);
        DELETE FROM legalizacion.tutor_practica WHERE id = v_tutor;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'tutor_practica', 'D', k);
        DELETE FROM legalizacion.objetivo_practica WHERE id = v_obj;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'objetivo_practica', 'D', k);
        DELETE FROM legalizacion.actividad_practica WHERE id = v_act;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'actividad_practica', 'D', k);

        -- La práctica ya solo está referida por la inscripción: no se puede
        -- eliminar mientras la tenga.
        integridad := integridad || pg_temp.fila('practica', 'Eliminar una práctica que tiene inscripciones',
            'rechazada por fk_inscripcion_practica',
            pg_temp.intentar(format($s$DELETE FROM legalizacion.practica WHERE id = %s$s$, v_prac)));

        -- Se retira el contacto de la inscripción para poder eliminarlo; la
        -- institución queda referida solo por la inscripción.
        UPDATE legalizacion.inscripcion SET contacto_id = NULL WHERE id = v_ins;
        DELETE FROM legalizacion.contacto_institucion WHERE id = v_cont;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'contacto_institucion', 'D', k);
        integridad := integridad || pg_temp.fila('institucion', 'Eliminar una institución que tiene inscripciones',
            'rechazada por fk_inscripcion_institucion',
            pg_temp.intentar(format($s$DELETE FROM legalizacion.institucion WHERE id = %s$s$, v_inst)));

        DELETE FROM legalizacion.inscripcion WHERE id = v_ins;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'inscripcion', 'D', k);
        DELETE FROM legalizacion.practica WHERE id = v_prac;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'practica', 'D', k);
        DELETE FROM legalizacion.institucion WHERE id = v_inst;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'institucion', 'D', k);
        DELETE FROM legalizacion.semestre WHERE id = v_sem;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'semestre', 'D', k);
        DELETE FROM legalizacion.plantilla_formato WHERE id = v_plant;
        GET DIAGNOSTICS k = ROW_COUNT;  conteo := pg_temp.anotar(conteo, 'plantilla_formato', 'D', k);

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

SELECT * FROM pg_temp.pruebas_crud_legalizacion();
