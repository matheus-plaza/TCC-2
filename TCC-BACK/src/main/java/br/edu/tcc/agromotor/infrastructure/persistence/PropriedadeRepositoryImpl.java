package br.edu.tcc.agromotor.infrastructure.persistence;

import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.Propriedade;
import br.edu.tcc.agromotor.domain.repository.PropriedadeRepository;
import br.edu.tcc.agromotor.infrastructure.persistence.entity.PropriedadeJpaEntity;
import br.edu.tcc.agromotor.infrastructure.persistence.jpa.PropriedadeJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PropriedadeRepositoryImpl implements PropriedadeRepository {

    private final PropriedadeJpaRepository jpa;

    public PropriedadeRepositoryImpl(PropriedadeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Propriedade salvar(Propriedade propriedade) {
        return jpa.save(PropriedadeJpaEntity.de(propriedade)).paraDominio();
    }

    @Override
    public Optional<Propriedade> buscarPorId(Long id) {
        return jpa.findById(id).map(PropriedadeJpaEntity::paraDominio);
    }

    @Override
    public List<Propriedade> listar(EscopoProdutores escopo) {
        if (!escopo.irrestrito() && escopo.produtorIds().isEmpty()) {
            return List.of();
        }
        List<PropriedadeJpaEntity> encontradas = escopo.irrestrito()
                ? jpa.findAllByOrderByNome()
                : jpa.findByProdutorIdInOrderByNome(escopo.produtorIds());
        return encontradas.stream().map(PropriedadeJpaEntity::paraDominio).toList();
    }

    @Override
    public boolean existeParaProdutor(Long produtorId) {
        return jpa.existsByProdutorId(produtorId);
    }

    @Override
    public void excluir(Long id) {
        jpa.deleteById(id);
    }
}
