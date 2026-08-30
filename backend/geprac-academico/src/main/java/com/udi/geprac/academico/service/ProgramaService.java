package com.udi.geprac.academico.service;

import com.udi.geprac.academico.domain.Programa;
import com.udi.geprac.academico.repository.ProgramaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProgramaService {

    private final ProgramaRepository repositorio;

    public ProgramaService(ProgramaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Programa> listar() {
        return repositorio.findAll();
    }

    @Transactional
    public Programa crear(Programa programa) {
        if (repositorio.existsByCodigoIgnoreCase(programa.getCodigo())) {
            throw new IllegalArgumentException(
                "Ya existe un programa con el código " + programa.getCodigo());
        }
        return repositorio.save(programa);
    }
}