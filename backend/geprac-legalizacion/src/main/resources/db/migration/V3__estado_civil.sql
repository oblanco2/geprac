-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · MS-02 Legalización de Prácticas
-- Amplía a 25 caracteres el estado civil que la inscripción copia del
-- registro del estudiante (CU-05). La opción más larga del formulario de
-- MS-01, «Unión marital de hecho», tiene 22 y no cabía en los 20 del
-- esquema inicial: la inscripción de quien la escogiera no se podía crear.
--
-- Autor: Darien Asdrwal Pesca Ojeda · modelo de datos y MS-02
-- ══════════════════════════════════════════════════════════════════

ALTER TABLE legalizacion.inscripcion_datos ALTER COLUMN estado_civil TYPE varchar(25);
