package br.edu.tcc.fertsolo.domain.repository;

import br.edu.tcc.fertsolo.domain.model.EscopoProdutores;
import br.edu.tcc.fertsolo.domain.model.Propriedade;

import java.util.List;
import java.util.Optional;

public interface PropriedadeRepository {

    Propriedade salvar(Propriedade propriedade);

    Optional<Propriedade> buscarPorId(Long id);

    List<Propriedade> listar(EscopoProdutores escopo);

    boolean existeParaProdutor(Long produtorId);

    void excluir(Long id);
}
