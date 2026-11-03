package br.edu.tcc.agromotor.infrastructure.persistence;

import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.Usuario;
import br.edu.tcc.agromotor.domain.repository.UsuarioRepository;
import br.edu.tcc.agromotor.infrastructure.persistence.entity.UsuarioJpaEntity;
import br.edu.tcc.agromotor.infrastructure.persistence.entity.VinculoJpaEntity;
import br.edu.tcc.agromotor.infrastructure.persistence.jpa.UsuarioJpaRepository;
import br.edu.tcc.agromotor.infrastructure.persistence.jpa.VinculoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final UsuarioJpaRepository usuarios;
    private final VinculoJpaRepository vinculos;

    public UsuarioRepositoryImpl(UsuarioJpaRepository usuarios, VinculoJpaRepository vinculos) {
        this.usuarios = usuarios;
        this.vinculos = vinculos;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return usuarios.save(UsuarioJpaEntity.de(usuario)).paraDominio();
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarios.findById(id).map(UsuarioJpaEntity::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarios.findByEmail(email).map(UsuarioJpaEntity::paraDominio);
    }

    @Override
    public boolean existeEmail(String email) {
        return usuarios.existsByEmail(email);
    }

    @Override
    public boolean existePerfil(PerfilUsuario perfil) {
        return usuarios.existsByPerfil(perfil);
    }

    @Override
    public List<Usuario> listar(PerfilUsuario perfil) {
        List<UsuarioJpaEntity> encontrados = perfil == null
                ? usuarios.findAllByOrderByNome()
                : usuarios.findByPerfilOrderByNome(perfil);
        return encontrados.stream().map(UsuarioJpaEntity::paraDominio).toList();
    }

    @Override
    public List<Usuario> listarPorIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return usuarios.findByIdInOrderByNome(ids).stream().map(UsuarioJpaEntity::paraDominio).toList();
    }

    @Override
    public void excluir(Long id) {
        usuarios.deleteById(id);
    }

    @Override
    public void vincular(Long agronomoId, Long produtorId) {
        vinculos.save(new VinculoJpaEntity(agronomoId, produtorId));
    }

    @Override
    public void desvincular(Long agronomoId, Long produtorId) {
        vinculos.deleteById(new VinculoJpaEntity.Chave(agronomoId, produtorId));
    }

    @Override
    public Set<Long> produtoresDoAgronomo(Long agronomoId) {
        return vinculos.findByAgronomoId(agronomoId).stream().map(VinculoJpaEntity::produtorId).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> agronomosDoProdutor(Long produtorId) {
        return vinculos.findByProdutorId(produtorId).stream().map(VinculoJpaEntity::agronomoId).collect(Collectors.toSet());
    }
}
