package br.edu.tcc.agromotor.domain.repository;

import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.Propriedade;

import java.util.List;
import java.util.Optional;

public interface PropriedadeRepository {

    Propriedade salvar(Propriedade propriedade);

    Optional<Propriedade> buscarPorId(Long id);

    List<Propriedade> listar(EscopoProdutores escopo);

    boolean existeParaProdutor(Long produtorId);

    void excluir(Long id);
}
