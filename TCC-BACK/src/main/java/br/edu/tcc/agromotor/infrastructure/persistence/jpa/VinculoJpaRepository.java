package br.edu.tcc.agromotor.infrastructure.persistence.jpa;

import br.edu.tcc.agromotor.infrastructure.persistence.entity.VinculoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VinculoJpaRepository extends JpaRepository<VinculoJpaEntity, VinculoJpaEntity.Chave> {

    List<VinculoJpaEntity> findByAgronomoId(Long agronomoId);

    List<VinculoJpaEntity> findByProdutorId(Long produtorId);
}
