-- ══════════════════════════════════════════════════════════════════
-- GEPRAC · MS-02 Legalización de Prácticas
-- Pone en vigencia la versión 1 de las plantillas de los cuatro formatos
-- que emite el software (CU-08): PR-01, PR-02, PR-04 y PR-05. Los
-- archivos están en los recursos del servicio, carpeta plantillas, y se
-- versionan aparte del código (RNF-09): un cambio en un formato oficial se
-- resuelve con un archivo nuevo y una fila nueva puesta en vigencia, sin
-- modificar el software.
--
-- Autor: Darien Asdrwal Pesca Ojeda · modelo de datos y MS-02
-- ══════════════════════════════════════════════════════════════════

INSERT INTO legalizacion.plantilla_formato (tipo, version, vigente, archivo, creada_en)
SELECT v.tipo, '1', true, v.archivo, localtimestamp
  FROM (VALUES ('PR-01', 'plantillas/PR-01_v1.txt'),
               ('PR-02', 'plantillas/PR-02_v1.txt'),
               ('PR-04', 'plantillas/PR-04_v1.txt'),
               ('PR-05', 'plantillas/PR-05_v1.txt')) AS v (tipo, archivo)
 WHERE NOT EXISTS (SELECT 1 FROM legalizacion.plantilla_formato p WHERE p.tipo = v.tipo AND p.vigente);
