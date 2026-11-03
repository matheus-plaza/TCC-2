package br.edu.tcc.agromotor.domain.repository;

import br.edu.tcc.agromotor.domain.model.Cultura;

import java.util.List;
import java.util.Optional;

public interface CulturaRepository {

    List<Cultura> listar();

    Optional<Cultura> buscarPorId(Long id);
}
