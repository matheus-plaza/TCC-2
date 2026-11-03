package br.edu.tcc.fertsolo.infrastructure.persistence;

import br.edu.tcc.fertsolo.domain.model.Cultura;
import br.edu.tcc.fertsolo.domain.repository.CulturaRepository;
import br.edu.tcc.fertsolo.infrastructure.persistence.entity.CulturaJpaEntity;
import br.edu.tcc.fertsolo.infrastructure.persistence.jpa.CulturaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CulturaRepositoryImpl implements CulturaRepository {

    private final CulturaJpaRepository jpa;

    public CulturaRepositoryImpl(CulturaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Cultura> listar() {
        return jpa.findAllByOrderById().stream().map(CulturaJpaEntity::paraDominio).toList();
    }

    @Override
    public Optional<Cultura> buscarPorId(Long id) {
        return jpa.findById(id).map(CulturaJpaEntity::paraDominio);
    }
}
