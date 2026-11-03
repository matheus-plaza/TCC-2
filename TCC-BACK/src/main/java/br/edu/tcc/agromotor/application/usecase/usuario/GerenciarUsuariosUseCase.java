package br.edu.tcc.agromotor.application.usecase.usuario;

import br.edu.tcc.agromotor.application.dto.UsuarioAtualizacaoRequest;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.dto.UsuarioRequest;
import br.edu.tcc.agromotor.application.dto.UsuarioResponse;
import br.edu.tcc.agromotor.application.exception.AcessoNegadoException;
import br.edu.tcc.agromotor.application.exception.ConflitoException;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.application.port.CifradorSenha;
import br.edu.tcc.agromotor.application.usecase.ControleAcesso;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.Usuario;
import br.edu.tcc.agromotor.domain.repository.PropriedadeRepository;
import br.edu.tcc.agromotor.domain.repository.UsuarioRepository;

import java.util.List;
import java.util.Locale;

/**
 * O administrador gerencia todas as contas. O agrônomo cadastra seus clientes como
 * produtores, ficando vinculado a eles, e vê apenas esses produtores.
 */
@UseCase
public class GerenciarUsuariosUseCase {

    private final UsuarioRepository usuarios;
    private final PropriedadeRepository propriedades;
    private final CriarContaUsuario criarConta;
    private final CifradorSenha cifrador;
    private final ControleAcesso acesso;

    public GerenciarUsuariosUseCase(UsuarioRepository usuarios, PropriedadeRepository propriedades,
                                    CriarContaUsuario criarConta, CifradorSenha cifrador, ControleAcesso acesso) {
        this.usuarios = usuarios;
        this.propriedades = propriedades;
        this.criarConta = criarConta;
        this.cifrador = cifrador;
        this.acesso = acesso;
    }

    public UsuarioResponse meuPerfil(UsuarioAutenticado ator) {
        return UsuarioResponse.de(buscar(ator.id()));
    }

    public List<UsuarioResponse> listar(UsuarioAutenticado ator, PerfilUsuario perfil) {
        acesso.exigirPerfil(ator, PerfilUsuario.ADMIN, PerfilUsuario.AGRONOMO);
        if (ator.perfil() == PerfilUsuario.AGRONOMO) {
            return usuarios.listarPorIds(usuarios.produtoresDoAgronomo(ator.id())).stream()
                    .map(UsuarioResponse::de)
                    .toList();
        }
        return usuarios.listar(perfil).stream().map(UsuarioResponse::de).toList();
    }

    public UsuarioResponse criar(UsuarioAutenticado ator, UsuarioRequest request) {
        acesso.exigirPerfil(ator, PerfilUsuario.ADMIN, PerfilUsuario.AGRONOMO);
        if (ator.perfil() == PerfilUsuario.AGRONOMO && request.perfil() != PerfilUsuario.PRODUTOR) {
            throw new AcessoNegadoException("O agrônomo pode cadastrar apenas produtores");
        }
        Usuario criado = criarConta.executar(request);
        if (ator.perfil() == PerfilUsuario.AGRONOMO) {
            usuarios.vincular(ator.id(), criado.id());
        }
        return UsuarioResponse.de(criado);
    }

    public UsuarioResponse atualizar(UsuarioAutenticado ator, Long id, UsuarioAtualizacaoRequest request) {
        if (!id.equals(ator.id())) {
            acesso.exigirPerfil(ator, PerfilUsuario.ADMIN);
        }
        Usuario atual = buscar(id);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (!email.equals(atual.email()) && usuarios.existeEmail(email)) {
            throw new ConflitoException("Já existe uma conta com este e-mail");
        }
        Usuario atualizado = atual.comDados(request.nome().trim(), email,
                CriarContaUsuario.vazioParaNulo(request.registroProfissional()));
        if (request.senha() != null && !request.senha().isBlank()) {
            atualizado = atualizado.comSenha(cifrador.cifrar(request.senha()));
        }
        return UsuarioResponse.de(usuarios.salvar(atualizado));
    }

    public void excluir(UsuarioAutenticado ator, Long id) {
        acesso.exigirPerfil(ator, PerfilUsuario.ADMIN);
        if (id.equals(ator.id())) {
            throw new ConflitoException("Você não pode excluir a própria conta");
        }
        buscar(id);
        if (propriedades.existeParaProdutor(id)) {
            throw new ConflitoException("Este produtor possui propriedades cadastradas");
        }
        usuarios.excluir(id);
    }

    private Usuario buscar(Long id) {
        return usuarios.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }
}
