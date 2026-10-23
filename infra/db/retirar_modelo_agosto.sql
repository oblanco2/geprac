-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · Retiro del modelo preliminar del primer avance
-- Elimina del esquema public de la base de MS-01 las once tablas del
-- modelo de agosto y el historial de Flyway que las registró. Desde el
-- segundo avance MS-01 trabaja en el esquema identidad, con su propio
-- historial de Flyway.
--
-- Se ejecuta una sola vez, en el SQL Editor de Supabase, cuando MS-01
-- ya arranca sobre el esquema identidad.
--
-- Autor: Darien Asdrwal Pesca Ojeda · modelo de datos
-- ══════════════════════════════════════════════════════════════════

DROP TABLE IF EXISTS
    public.asignacion,
    public.estudiante_grupo,
    public.grupo,
    public.practica,
    public.convenio,
    public.institucion,
    public.usuario_rol,
    public.usuario,
    public.rol,
    public.programa,
    public.auditoria,
    public.flyway_schema_history;

-- Comprobación: debe listar solo el esquema identidad, con sus seis
-- tablas y el historial de Flyway (flyway_schema_history).
SELECT table_schema AS esquema, table_name AS tabla
FROM information_schema.tables
WHERE table_schema IN ('public', 'identidad')
ORDER BY 1, 2;
