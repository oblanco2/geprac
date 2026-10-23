package com.udi.geprac.legalizacion.dto;

import java.util.List;

/**
 * Lo que presenta la preparación del semestre (CU-04, paso 2; P-16): el
 * semestre abierto o, si no hay, el último que se cerró, y las prácticas del
 * programa con el tutor designado en él. Sin semestres registrados, el
 * semestre viene vacío y las prácticas, sin tutor. La lista de cuentas con su
 * rol la consulta el cliente web en MS-01.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record PreparacionDto(SemestreDto semestre, List<DesignacionDto> practicas) { }
