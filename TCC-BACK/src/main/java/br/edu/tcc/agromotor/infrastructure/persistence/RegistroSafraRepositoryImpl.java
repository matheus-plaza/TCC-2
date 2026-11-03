package br.edu.tcc.agromotor.infrastructure.persistence;

import br.edu.tcc.agromotor.domain.model.RegistroSafra;
import br.edu.tcc.agromotor.domain.repository.RegistroSafraRepository;
import br.edu.tcc.agromotor.infrastructure.persistence.entity.RegistroSafraJpaEntity;
import br.edu.tcc.agromotor.infrastructure.persistence.jpa.RegistroSafraJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class RegistroSafraRepositoryImpl implements RegistroSafraRepository {

    private final RegistroSafraJpaRepository jpa;

    public RegistroSafraRepositoryImpl(RegistroSafraJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public RegistroSafra salvar(RegistroSafra registro) {
        Long idExistente = jpa.findByRecomendacaoId(registro.recomendacaoId()).map(RegistroSafraJpaEntity::id).orElse(null);
        return jpa.save(RegistroSafraJpaEntity.de(registro, idExistente)).paraDominio();
    }

    @Override
    public Optional<RegistroSafra> buscarPorRecomendacao(Long recomendacaoId) {
        return jpa.findByRecomendacaoId(recomendacaoId).map(RegistroSafraJpaEntity::paraDominio);
    }

    @Override
    public Map<Long, RegistroSafra> porRecomendacoes(Collection<Long> recomendacaoIds) {
        if (recomendacaoIds.isEmpty()) {
            return Map.of();
        }
        return jpa.findByRecomendacaoIdIn(recomendacaoIds).stream()
                .map(RegistroSafraJpaEntity::paraDominio)
                .collect(Collectors.toMap(RegistroSafra::recomendacaoId, Function.identity()));
    }

    @Override
    public void excluirPorRecomendacao(Long recomendacaoId) {
        jpa.findByRecomendacaoId(recomendacaoId).ifPresent(jpa::delete);
    }
}
