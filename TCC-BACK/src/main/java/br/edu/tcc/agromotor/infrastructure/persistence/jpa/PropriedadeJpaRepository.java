package br.edu.tcc.agromotor.infrastructure.persistence.jpa;

import br.edu.tcc.agromotor.infrastructure.persistence.entity.PropriedadeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PropriedadeJpaRepository extends JpaRepository<PropriedadeJpaEntity, Long> {

    List<PropriedadeJpaEntity> findAllByOrderByNome();

    List<PropriedadeJpaEntity> findByProdutorIdInOrderByNome(Collection<Long> produtorIds);

    boolean existsByProdutorId(Long produtorId);
}
