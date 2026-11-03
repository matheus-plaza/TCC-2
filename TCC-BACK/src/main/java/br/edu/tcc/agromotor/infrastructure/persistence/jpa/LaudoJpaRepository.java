package br.edu.tcc.agromotor.infrastructure.persistence.jpa;

import br.edu.tcc.agromotor.infrastructure.persistence.entity.LaudoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaudoJpaRepository extends JpaRepository<LaudoJpaEntity, Long> {

    boolean existsByTalhaoId(Long talhaoId);
}
