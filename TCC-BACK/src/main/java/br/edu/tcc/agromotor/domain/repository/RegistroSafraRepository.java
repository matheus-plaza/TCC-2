package br.edu.tcc.agromotor.domain.repository;

import br.edu.tcc.agromotor.domain.model.RegistroSafra;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface RegistroSafraRepository {

    /** Cria o registro da recomendação ou substitui o que já existe. */
    RegistroSafra salvar(RegistroSafra registro);

    Optional<RegistroSafra> buscarPorRecomendacao(Long recomendacaoId);

    /** Registros das recomendações informadas, indexados pelo id da recomendação. */
    Map<Long, RegistroSafra> porRecomendacoes(Collection<Long> recomendacaoIds);

    void excluirPorRecomendacao(Long recomendacaoId);
}
