package br.edu.tcc.fertsolo.application.port;

import br.edu.tcc.fertsolo.domain.model.Usuario;

import java.time.Instant;

public interface GeradorToken {

    TokenGerado gerar(Usuario usuario);

    record TokenGerado(String token, Instant expiraEm) {
    }
}
