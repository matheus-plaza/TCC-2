package br.edu.tcc.agromotor.domain.repository;

import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.FiltroRecomendacoes;
import br.edu.tcc.agromotor.domain.model.Recomendacao;

import java.util.List;
import java.util.Optional;

public interface RecomendacaoRepository {

    Recomendacao salvar(Recomendacao recomendacao);

    Optional<Recomendacao> buscarPorId(Long id);

    /** Recomendações visíveis no escopo, da mais recente para a mais antiga. */
    List<Recomendacao> listar(EscopoProdutores escopo, FiltroRecomendacoes filtro);

    /** Última recomendação adotada no talhão: define o crédito/débito da safra seguinte. */
    Optional<Recomendacao> ultimaAdotadaDoTalhao(Long talhaoId);

    boolean existeParaLaudo(Long laudoId);

    void excluir(Long id);
}
