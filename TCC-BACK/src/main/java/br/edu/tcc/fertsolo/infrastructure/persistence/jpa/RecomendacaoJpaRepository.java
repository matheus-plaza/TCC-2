package br.edu.tcc.fertsolo.infrastructure.persistence.jpa;

import br.edu.tcc.fertsolo.infrastructure.persistence.entity.RecomendacaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecomendacaoJpaRepository extends JpaRepository<RecomendacaoJpaEntity, Long> {

    boolean existsByAnaliseId(Long analiseId);
}
