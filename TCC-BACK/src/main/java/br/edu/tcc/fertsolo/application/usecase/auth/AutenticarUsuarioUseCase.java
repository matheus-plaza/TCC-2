package br.edu.tcc.fertsolo.application.usecase.auth;

import br.edu.tcc.fertsolo.application.dto.LoginRequest;
import br.edu.tcc.fertsolo.application.dto.TokenResponse;
import br.edu.tcc.fertsolo.application.dto.UsuarioResponse;
import br.edu.tcc.fertsolo.application.exception.CredenciaisInvalidasException;
import br.edu.tcc.fertsolo.application.port.CifradorSenha;
import br.edu.tcc.fertsolo.application.port.GeradorToken;
import br.edu.tcc.fertsolo.application.usecase.UseCase;
import br.edu.tcc.fertsolo.domain.model.Usuario;
import br.edu.tcc.fertsolo.domain.repository.UsuarioRepository;

import java.util.Locale;

/** RF01: login com e-mail e senha, devolvendo um JWT com o perfil do usuário. */
@UseCase
public class AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarios;
    private final CifradorSenha cifrador;
    private final GeradorToken geradorToken;

    public AutenticarUsuarioUseCase(UsuarioRepository usuarios, CifradorSenha cifrador, GeradorToken geradorToken) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.geradorToken = geradorToken;
    }

    public TokenResponse executar(LoginRequest request) {
        Usuario usuario = usuarios.buscarPorEmail(request.email().trim().toLowerCase(Locale.ROOT))
                .filter(u -> cifrador.confere(request.senha(), u.senhaHash()))
                .orElseThrow(CredenciaisInvalidasException::new);
        return emitirToken(usuario);
    }

    public TokenResponse emitirToken(Usuario usuario) {
        GeradorToken.TokenGerado token = geradorToken.gerar(usuario);
        return new TokenResponse(token.token(), "Bearer", token.expiraEm(), UsuarioResponse.de(usuario));
    }
}
