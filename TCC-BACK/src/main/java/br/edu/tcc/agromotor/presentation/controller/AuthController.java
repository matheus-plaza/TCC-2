package br.edu.tcc.agromotor.presentation.controller;

import br.edu.tcc.agromotor.application.dto.LoginRequest;
import br.edu.tcc.agromotor.application.dto.TokenResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.dto.UsuarioRequest;
import br.edu.tcc.agromotor.application.dto.UsuarioResponse;
import br.edu.tcc.agromotor.application.usecase.auth.AutenticarUsuarioUseCase;
import br.edu.tcc.agromotor.application.usecase.auth.RegistrarUsuarioUseCase;
import br.edu.tcc.agromotor.application.usecase.usuario.GerenciarUsuariosUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** RF01: autenticação com JWT. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticar;
    private final RegistrarUsuarioUseCase registrar;
    private final GerenciarUsuariosUseCase usuarios;

    public AuthController(AutenticarUsuarioUseCase autenticar, RegistrarUsuarioUseCase registrar,
                          GerenciarUsuariosUseCase usuarios) {
        this.autenticar = autenticar;
        this.registrar = registrar;
        this.usuarios = usuarios;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return autenticar.executar(request);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse registro(@Valid @RequestBody UsuarioRequest request) {
        return registrar.executar(request);
    }

    @GetMapping("/me")
    public UsuarioResponse me(UsuarioAutenticado ator) {
        return usuarios.meuPerfil(ator);
    }
}
