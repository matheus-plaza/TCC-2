package br.edu.tcc.fertsolo.application.dto;

import br.edu.tcc.fertsolo.domain.model.PerfilUsuario;

/** Identidade extraída do JWT e repassada aos casos de uso. */
public record UsuarioAutenticado(Long id, PerfilUsuario perfil) {
}
