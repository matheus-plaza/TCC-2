package br.edu.tcc.fertsolo.infrastructure.persistence;

import br.edu.tcc.fertsolo.domain.model.PerfilUsuario;
import br.edu.tcc.fertsolo.domain.model.Usuario;
import br.edu.tcc.fertsolo.domain.repository.UsuarioRepository;
import br.edu.tcc.fertsolo.infrastructure.persistence.entity.UsuarioJpaEntity;
import br.edu.tcc.fertsolo.infrastructure.persistence.entity.VinculoJpaEntity;
import br.edu.tcc.fertsolo.infrastructure.persistence.jpa.UsuarioJpaRepository;
import br.edu.tcc.fertsolo.infrastructure.persistence.jpa.VinculoJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager em;

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
    public void excluirProdutorComDados(Long produtorId) {
        String analises = "SELECT a.id FROM analises_solo a JOIN talhoes t ON t.id = a.talhao_id"
                + " JOIN propriedades p ON p.id = t.propriedade_id WHERE p.produtor_id = ?1";
        String recomendacoes = "SELECT r.id FROM recomendacoes r WHERE r.analise_id IN (" + analises + ")";
        String talhoes = "SELECT t.id FROM talhoes t JOIN propriedades p ON p.id = t.propriedade_id WHERE p.produtor_id = ?1";
        executarEliminacao(produtorId,
                "DELETE FROM registros_safra WHERE recomendacao_id IN (" + recomendacoes + ")",
                "DELETE FROM recomendacoes WHERE id IN (" + recomendacoes + ")",
                "DELETE FROM analises_solo WHERE id IN (" + analises + ")",
                "DELETE FROM talhoes WHERE id IN (" + talhoes + ")",
                "DELETE FROM propriedades WHERE produtor_id = ?1",
                "DELETE FROM agronomo_produtor WHERE produtor_id = ?1",
                "DELETE FROM usuarios WHERE id = ?1");
    }

    @Override
    public void excluirAgronomo(Long agronomoId) {
        executarEliminacao(agronomoId,
                "UPDATE recomendacoes SET gerado_por_id = NULL WHERE gerado_por_id = ?1",
                "UPDATE recomendacoes SET adotado_por_id = NULL WHERE adotado_por_id = ?1",
                "DELETE FROM agronomo_produtor WHERE agronomo_id = ?1",
                "DELETE FROM usuarios WHERE id = ?1");
    }

    /** O gatilho das recomendações adotadas só aceita estes comandos com a marca desta transação. */
    private void executarEliminacao(Long id, String... comandos) {
        em.flush();
        em.createNativeQuery("SELECT set_config('fertsolo.eliminacao_titular', 'on', true)").getSingleResult();
        for (String comando : comandos) {
            em.createNativeQuery(comando).setParameter(1, id).executeUpdate();
        }
        em.createNativeQuery("SELECT set_config('fertsolo.eliminacao_titular', 'off', true)").getSingleResult();
        em.clear();
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
