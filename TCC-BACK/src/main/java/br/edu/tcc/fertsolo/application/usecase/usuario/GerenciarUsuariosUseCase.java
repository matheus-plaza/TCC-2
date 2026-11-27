package br.edu.tcc.fertsolo.application.usecase.usuario;

import br.edu.tcc.fertsolo.application.dto.UsuarioAtualizacaoRequest;
import br.edu.tcc.fertsolo.application.dto.UsuarioAutenticado;
import br.edu.tcc.fertsolo.application.dto.UsuarioRequest;
import br.edu.tcc.fertsolo.application.dto.UsuarioResponse;
import br.edu.tcc.fertsolo.application.exception.AcessoNegadoException;
import br.edu.tcc.fertsolo.application.exception.ConflitoException;
import br.edu.tcc.fertsolo.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.fertsolo.application.port.CifradorSenha;
import br.edu.tcc.fertsolo.application.port.RegistroAuditoria;
import br.edu.tcc.fertsolo.application.usecase.ControleAcesso;
import br.edu.tcc.fertsolo.application.usecase.UseCase;
import br.edu.tcc.fertsolo.domain.model.PerfilUsuario;
import br.edu.tcc.fertsolo.domain.model.Usuario;
import br.edu.tcc.fertsolo.domain.repository.UsuarioRepository;

import java.util.List;
import java.util.Locale;

/**
 * O administrador gerencia todas as contas, vendo apenas os dados de cadastro. O agrônomo
 * cadastra seus clientes como produtores, ficando vinculado a eles, e vê apenas esses
 * produtores. Qualquer usuário pode excluir a própria conta.
 */
@UseCase
public class GerenciarUsuariosUseCase {

    private final UsuarioRepository usuarios;
    private final CriarContaUsuario criarConta;
    private final CifradorSenha cifrador;
    private final ControleAcesso acesso;
    private final RegistroAuditoria auditoria;

    public GerenciarUsuariosUseCase(UsuarioRepository usuarios, CriarContaUsuario criarConta, CifradorSenha cifrador,
                                    ControleAcesso acesso, RegistroAuditoria auditoria) {
        this.usuarios = usuarios;
        this.criarConta = criarConta;
        this.cifrador = cifrador;
        this.acesso = acesso;
        this.auditoria = auditoria;
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
        registrarSeAdmin(ator, RegistroAuditoria.CRIACAO_CONTA, criado.id());
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
        UsuarioResponse resposta = UsuarioResponse.de(usuarios.salvar(atualizado));
        registrarSeAdmin(ator, RegistroAuditoria.EDICAO_CONTA, id);
        return resposta;
    }

    public void excluir(UsuarioAutenticado ator, Long id) {
        if (id.equals(ator.id())) {
            excluirMinhaConta(ator);
            return;
        }
        acesso.exigirPerfil(ator, PerfilUsuario.ADMIN);
        eliminar(buscar(id));
        auditoria.registrar(ator.id(), RegistroAuditoria.EXCLUSAO_CONTA, id);
    }

    /** Eliminação a pedido do titular: a conta e os dados ligados a ela, sem volta. */
    public void excluirMinhaConta(UsuarioAutenticado ator) {
        eliminar(buscar(ator.id()));
        auditoria.registrar(ator.id(), RegistroAuditoria.AUTOEXCLUSAO_CONTA, ator.id());
    }

    private void eliminar(Usuario usuario) {
        switch (usuario.perfil()) {
            case PRODUTOR -> usuarios.excluirProdutorComDados(usuario.id());
            case AGRONOMO -> usuarios.excluirAgronomo(usuario.id());
            case ADMIN -> {
                if (usuarios.listar(PerfilUsuario.ADMIN).size() <= 1) {
                    throw new ConflitoException("O único administrador não pode excluir a própria conta");
                }
                usuarios.excluir(usuario.id());
            }
        }
    }

    private void registrarSeAdmin(UsuarioAutenticado ator, String acao, Long alvoId) {
        if (ator.perfil() == PerfilUsuario.ADMIN) {
            auditoria.registrar(ator.id(), acao, alvoId);
        }
    }

    private Usuario buscar(Long id) {
        return usuarios.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }
}
