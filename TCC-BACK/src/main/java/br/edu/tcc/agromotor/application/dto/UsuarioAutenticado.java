package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.PerfilUsuario;

/** Identidade extraída do JWT e repassada aos casos de uso. */
public record UsuarioAutenticado(Long id, PerfilUsuario perfil) {
}
