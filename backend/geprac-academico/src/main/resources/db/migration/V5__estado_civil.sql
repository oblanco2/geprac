-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · MS-01 Identidad y Perfil Académico
-- Amplía a 25 caracteres el estado civil del estudiante. La opción más
-- larga de la lista de P-03, «Unión marital de hecho», tiene 22 y no
-- cabía en los 20 del esquema inicial: el perfil de quien la escogiera
-- no se podía guardar (CU-01). La copia que guarda la inscripción en
-- MS-02 se amplió igual, en su migración V3.
--
-- Autor: Oscar Iván Blanco Díaz · MS-01
-- ══════════════════════════════════════════════════════════════════

ALTER TABLE identidad.estudiante ALTER COLUMN estado_civil TYPE varchar(25);
