package br.edu.tcc.agromotor.infrastructure.persistence.jpa;

import br.edu.tcc.agromotor.infrastructure.persistence.entity.RegistroSafraJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RegistroSafraJpaRepository extends JpaRepository<RegistroSafraJpaEntity, Long> {

    Optional<RegistroSafraJpaEntity> findByRecomendacaoId(Long recomendacaoId);

    List<RegistroSafraJpaEntity> findByRecomendacaoIdIn(Collection<Long> recomendacaoIds);
}
