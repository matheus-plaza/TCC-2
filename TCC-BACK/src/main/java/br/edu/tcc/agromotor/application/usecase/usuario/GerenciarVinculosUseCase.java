package br.edu.tcc.agromotor.application.usecase.usuario;

import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.dto.UsuarioResponse;
import br.edu.tcc.agromotor.application.dto.VinculoRequest;
import br.edu.tcc.agromotor.application.dto.VinculoResponse;
import br.edu.tcc.agromotor.application.exception.AcessoNegadoException;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.Usuario;
import br.edu.tcc.agromotor.domain.repository.UsuarioRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Vínculo agrônomo-produtor (RNF05). O acesso aos dados nasce do consentimento: o
 * produtor autoriza o agrônomo pelo e-mail, ou o administrador faz o vínculo. Um
 * agrônomo não consegue se vincular sozinho a um produtor que não cadastrou.
 */
@UseCase
public class GerenciarVinculosUseCase {

    private final UsuarioRepository usuarios;

    public GerenciarVinculosUseCase(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public List<VinculoResponse> listar(UsuarioAutenticado ator) {
        return switch (ator.perfil()) {
            case PRODUTOR -> vinculosDoProdutor(buscar(ator.id()));
            case AGRONOMO -> vinculosDoAgronomo(buscar(ator.id()));
            case ADMIN -> usuarios.listar(PerfilUsuario.AGRONOMO).stream()
                    .flatMap(a -> vinculosDoAgronomo(a).stream())
                    .toList();
        };
    }

    public VinculoResponse vincular(UsuarioAutenticado ator, VinculoRequest request) {
        return switch (ator.perfil()) {
            case PRODUTOR -> criar(agronomoPorEmail(request.agronomoEmail()), buscar(ator.id()));
            case ADMIN -> criar(buscarComPerfil(request.agronomoId(), PerfilUsuario.AGRONOMO),
                    buscarComPerfil(request.produtorId(), PerfilUsuario.PRODUTOR));
            case AGRONOMO -> throw new AcessoNegadoException(
                    "O vínculo é autorizado pelo produtor ou pelo administrador");
        };
    }

    public void desvincular(UsuarioAutenticado ator, Long agronomoId, Long produtorId) {
        boolean participa = ator.perfil() == PerfilUsuario.ADMIN || ator.id().equals(agronomoId) || ator.id().equals(produtorId);
        if (!participa) {
            throw new AcessoNegadoException("Você não participa deste vínculo");
        }
        usuarios.desvincular(agronomoId, produtorId);
    }

    private VinculoResponse criar(Usuario agronomo, Usuario produtor) {
        usuarios.vincular(agronomo.id(), produtor.id());
        return new VinculoResponse(UsuarioResponse.de(agronomo), UsuarioResponse.de(produtor));
    }

    private List<VinculoResponse> vinculosDoProdutor(Usuario produtor) {
        return usuarios.listarPorIds(usuarios.agronomosDoProdutor(produtor.id())).stream()
                .sorted(Comparator.comparing(Usuario::nome))
                .map(a -> new VinculoResponse(UsuarioResponse.de(a), UsuarioResponse.de(produtor)))
                .toList();
    }

    private List<VinculoResponse> vinculosDoAgronomo(Usuario agronomo) {
        return usuarios.listarPorIds(usuarios.produtoresDoAgronomo(agronomo.id())).stream()
                .sorted(Comparator.comparing(Usuario::nome))
                .map(p -> new VinculoResponse(UsuarioResponse.de(agronomo), UsuarioResponse.de(p)))
                .toList();
    }

    private Usuario agronomoPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RegraNegocioException("Informe o e-mail do agrônomo");
        }
        return usuarios.buscarPorEmail(email.trim().toLowerCase(Locale.ROOT))
                .filter(u -> u.perfil() == PerfilUsuario.AGRONOMO)
                .orElseThrow(() -> new RegraNegocioException("Não há agrônomo cadastrado com este e-mail"));
    }

    private Usuario buscarComPerfil(Long id, PerfilUsuario perfil) {
        if (id == null) {
            throw new RegraNegocioException("Informe o agrônomo e o produtor do vínculo");
        }
        Usuario usuario = buscar(id);
        if (usuario.perfil() != perfil) {
            throw new RegraNegocioException("%s não tem o perfil %s".formatted(usuario.nome(), perfil));
        }
        return usuario;
    }

    private Usuario buscar(Long id) {
        return usuarios.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }
}
