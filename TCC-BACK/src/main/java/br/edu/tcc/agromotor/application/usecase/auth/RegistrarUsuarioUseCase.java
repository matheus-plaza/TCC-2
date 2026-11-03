package br.edu.tcc.agromotor.application.usecase.auth;

import br.edu.tcc.agromotor.application.dto.TokenResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioRequest;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.application.usecase.usuario.CriarContaUsuario;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.Usuario;

/** Registro público: cria contas de produtor ou agrônomo. Administrador só por outro administrador. */
@UseCase
public class RegistrarUsuarioUseCase {

    private final CriarContaUsuario criarConta;
    private final AutenticarUsuarioUseCase autenticar;

    public RegistrarUsuarioUseCase(CriarContaUsuario criarConta, AutenticarUsuarioUseCase autenticar) {
        this.criarConta = criarConta;
        this.autenticar = autenticar;
    }

    public TokenResponse executar(UsuarioRequest request) {
        if (request.perfil() == PerfilUsuario.ADMIN) {
            throw new RegraNegocioException("Não é possível se registrar como administrador");
        }
        Usuario criado = criarConta.executar(request);
        return autenticar.emitirToken(criado);
    }
}
