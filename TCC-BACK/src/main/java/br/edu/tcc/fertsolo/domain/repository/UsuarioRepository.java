package br.edu.tcc.fertsolo.domain.repository;

import br.edu.tcc.fertsolo.domain.model.PerfilUsuario;
import br.edu.tcc.fertsolo.domain.model.Usuario;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UsuarioRepository {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existeEmail(String email);

    boolean existePerfil(PerfilUsuario perfil);

    List<Usuario> listar(PerfilUsuario perfil);

    List<Usuario> listarPorIds(Collection<Long> ids);

    void excluir(Long id);

    /** Exclui o produtor com propriedades, talhões, laudos, recomendações, safras e vínculos. */
    void excluirProdutorComDados(Long produtorId);

    /** Exclui o agrônomo e seus vínculos; as recomendações dele ficam com os produtores, sem o autor. */
    void excluirAgronomo(Long agronomoId);

    void vincular(Long agronomoId, Long produtorId);

    void desvincular(Long agronomoId, Long produtorId);

    Set<Long> produtoresDoAgronomo(Long agronomoId);

    Set<Long> agronomosDoProdutor(Long produtorId);
}
