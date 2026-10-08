-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · MS-01 Identidad y Perfil Académico
-- Datos de la cuenta en el token de acceso. Supabase Auth llama esta
-- función cada vez que emite un token (hook «Customize Access Token»)
-- y ella añade los claims que encuentre en el esquema identidad:
--   rol            DIRECTOR, TUTOR o ESTUDIANTE
--   programa       el que dirige el director o en el que estudia el
--                  estudiante
--   estudiante_id  el identificador del perfil del estudiante
-- Con ellos cada microservicio autoriza la petición sin consultar al
-- otro. Si la cuenta no tiene alguno de esos datos, el claim no va; si
-- algo falla, el token sale sin ellos y el ingreso no se interrumpe.
--
-- Después de desplegar esta migración hay que activar el hook en
-- Supabase: Authentication → Hooks → Customize Access Token (JWT)
-- Claims → Postgres → esquema identidad, función claims_del_token.
--
-- Autor: José Fernando Rincón Barrios · integración de la autenticación
-- ══════════════════════════════════════════════════════════════════

CREATE FUNCTION identidad.claims_del_token(event jsonb)
RETURNS jsonb
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_rol        text;
    v_programa   text;
    v_estudiante bigint;
BEGIN
    SELECT u.rol, coalesce(u.codigo_programa, e.codigo_programa), e.id
      INTO v_rol, v_programa, v_estudiante
      FROM identidad.usuario u
      LEFT JOIN identidad.estudiante e ON e.usuario_id = u.id
     WHERE u.id = (event ->> 'user_id')::uuid;

    RETURN jsonb_set(event, '{claims}', (event -> 'claims') || jsonb_strip_nulls(jsonb_build_object(
        'rol', v_rol,
        'programa', v_programa,
        'estudiante_id', v_estudiante)));
EXCEPTION WHEN OTHERS THEN
    RETURN event;
END;
$$;

COMMENT ON FUNCTION identidad.claims_del_token(jsonb) IS
    'Hook de Supabase Auth: añade al token el rol, el programa y el identificador del estudiante';

GRANT USAGE ON SCHEMA identidad TO supabase_auth_admin;
GRANT SELECT ON identidad.usuario, identidad.estudiante TO supabase_auth_admin;
GRANT EXECUTE ON FUNCTION identidad.claims_del_token(jsonb) TO supabase_auth_admin;
REVOKE EXECUTE ON FUNCTION identidad.claims_del_token(jsonb) FROM PUBLIC, anon, authenticated;
