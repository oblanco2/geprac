package com.udi.geprac.academico.service;

import com.udi.geprac.academico.dto.ProgramaDto;
import com.udi.geprac.academico.repository.ProgramaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Consulta de los programas académicos.
 *
 * @author Oscar Iván Blanco Díaz
 */
@Service
public class ProgramaService {

    private final ProgramaRepository repositorio;

    public ProgramaService(ProgramaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Los programas, ordenados por código. */
    @Transactional(readOnly = true)
    public List<ProgramaDto> listar() {
        return repositorio.findAll(Sort.by("codigo")).stream()
            .map(ProgramaDto::de)
            .toList();
    }
}
