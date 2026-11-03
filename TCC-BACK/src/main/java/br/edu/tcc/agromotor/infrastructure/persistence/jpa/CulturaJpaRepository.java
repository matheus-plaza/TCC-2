package br.edu.tcc.agromotor.infrastructure.persistence.jpa;

import br.edu.tcc.agromotor.infrastructure.persistence.entity.CulturaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CulturaJpaRepository extends JpaRepository<CulturaJpaEntity, Long> {

    List<CulturaJpaEntity> findAllByOrderById();
}
