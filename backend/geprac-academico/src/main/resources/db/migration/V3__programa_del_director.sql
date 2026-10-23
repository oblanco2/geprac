-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · MS-01 Identidad y Perfil Académico
-- Programa que dirige cada director. CU-02, CU-07 y CU-09 operan sobre
-- «su programa»: el catálogo, la bandeja de aval y el historial se
-- acotan al programa del director que ingresa. Nulo para los demás
-- roles.
--
-- Autor: Darien Asdrwal Pesca Ojeda · modelo de datos
-- ══════════════════════════════════════════════════════════════════

ALTER TABLE identidad.usuario
    ADD COLUMN codigo_programa varchar(10),
    ADD CONSTRAINT fk_usuario_programa FOREIGN KEY (codigo_programa) REFERENCES identidad.programa (codigo);

COMMENT ON COLUMN identidad.usuario.codigo_programa IS 'Programa que dirige el usuario cuando su rol es DIRECTOR. Nulo para los demás roles';
