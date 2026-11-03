package br.edu.tcc.fertsolo.application.dto;

import java.time.Instant;

public record TokenResponse(String token, String tipo, Instant expiraEm, UsuarioResponse usuario) {
}
