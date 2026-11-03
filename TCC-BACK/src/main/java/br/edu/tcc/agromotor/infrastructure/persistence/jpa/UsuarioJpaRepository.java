package br.edu.tcc.agromotor.infrastructure.persistence.jpa;

import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.infrastructure.persistence.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {

    Optional<UsuarioJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPerfil(PerfilUsuario perfil);

    List<UsuarioJpaEntity> findByPerfilOrderByNome(PerfilUsuario perfil);

    List<UsuarioJpaEntity> findAllByOrderByNome();

    List<UsuarioJpaEntity> findByIdInOrderByNome(Iterable<Long> ids);
}
