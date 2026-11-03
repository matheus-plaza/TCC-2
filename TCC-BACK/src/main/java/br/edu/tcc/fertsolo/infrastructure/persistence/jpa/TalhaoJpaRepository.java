package br.edu.tcc.fertsolo.infrastructure.persistence.jpa;

import br.edu.tcc.fertsolo.infrastructure.persistence.entity.TalhaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TalhaoJpaRepository extends JpaRepository<TalhaoJpaEntity, Long> {

    boolean existsByPropriedadeId(Long propriedadeId);

    boolean existsByPropriedadeIdAndIdentificacao(Long propriedadeId, String identificacao);

    boolean existsByPropriedadeIdAndIdentificacaoAndIdNot(Long propriedadeId, String identificacao, Long id);
}
