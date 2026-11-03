package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        String registroProfissional,
        LocalDateTime dataCriacao) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.id(), u.nome(), u.email(), u.perfil(), u.registroProfissional(), u.dataCriacao());
    }
}
