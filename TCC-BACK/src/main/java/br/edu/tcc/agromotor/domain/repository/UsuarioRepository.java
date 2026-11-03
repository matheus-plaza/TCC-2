package br.edu.tcc.agromotor.domain.repository;

import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.Usuario;

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

    void vincular(Long agronomoId, Long produtorId);

    void desvincular(Long agronomoId, Long produtorId);

    Set<Long> produtoresDoAgronomo(Long agronomoId);

    Set<Long> agronomosDoProdutor(Long produtorId);
}
