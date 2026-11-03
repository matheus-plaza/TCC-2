package br.edu.tcc.agromotor.domain.repository;

import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.Talhao;

import java.util.List;
import java.util.Optional;

public interface TalhaoRepository {

    Talhao salvar(Talhao talhao);

    Optional<ContextoTalhao> contexto(Long talhaoId);

    /** Talhões visíveis no escopo; {@code propriedadeId} nulo não restringe. */
    List<ContextoTalhao> listarContextos(EscopoProdutores escopo, Long propriedadeId);

    boolean existeParaPropriedade(Long propriedadeId);

    /** Identificação já usada na propriedade; {@code talhaoIgnorado} é o próprio talhão, na edição. */
    boolean existeIdentificacao(Long propriedadeId, String identificacao, Long talhaoIgnorado);

    void excluir(Long id);
}
