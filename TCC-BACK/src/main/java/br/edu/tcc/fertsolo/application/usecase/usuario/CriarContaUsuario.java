package br.edu.tcc.fertsolo.application.usecase.usuario;

import br.edu.tcc.fertsolo.application.dto.UsuarioRequest;
import br.edu.tcc.fertsolo.application.exception.ConflitoException;
import br.edu.tcc.fertsolo.application.port.CifradorSenha;
import br.edu.tcc.fertsolo.application.usecase.UseCase;
import br.edu.tcc.fertsolo.domain.model.Usuario;
import br.edu.tcc.fertsolo.domain.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Locale;

/** Regra comum de criação de conta: e-mail único e senha cifrada com BCrypt. */
@UseCase
public class CriarContaUsuario {

    private final UsuarioRepository usuarios;
    private final CifradorSenha cifrador;

    public CriarContaUsuario(UsuarioRepository usuarios, CifradorSenha cifrador) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
    }

    public Usuario executar(UsuarioRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existeEmail(email)) {
            throw new ConflitoException("Já existe uma conta com este e-mail");
        }
        return usuarios.salvar(new Usuario(null, request.nome().trim(), email, cifrador.cifrar(request.senha()),
                request.perfil(), vazioParaNulo(request.registroProfissional()), LocalDateTime.now()));
    }

    static String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
