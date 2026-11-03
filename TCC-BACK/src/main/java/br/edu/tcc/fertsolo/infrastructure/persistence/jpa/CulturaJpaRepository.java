package br.edu.tcc.fertsolo.infrastructure.persistence.jpa;

import br.edu.tcc.fertsolo.infrastructure.persistence.entity.CulturaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CulturaJpaRepository extends JpaRepository<CulturaJpaEntity, Long> {

    List<CulturaJpaEntity> findAllByOrderById();
}
